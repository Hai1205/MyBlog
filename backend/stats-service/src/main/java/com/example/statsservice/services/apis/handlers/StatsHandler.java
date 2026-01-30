package com.example.statsservice.services.apis.handlers;

import com.example.rediscommon.services.RedisCacheService;
import com.example.rediscommon.services.RedisService;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.example.statsservice.dtos.ActivityDto;
import com.example.statsservice.dtos.DashboardStatsDto;
import com.example.statsservice.services.feigns.UserFeignClient;
import com.example.statsservice.services.feigns.BlogFeignClient;
import com.example.statsservice.dtos.responses.Response;
import com.example.statsservice.dtos.responses.views.BlogView;
import com.example.statsservice.dtos.responses.views.UserView;
import com.example.statsservice.exceptions.OurException;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StatsHandler {

    private final UserFeignClient userFeignClient;
    private final BlogFeignClient blogFeignClient;
    private final RedisCacheService cacheService;
    private final CacheKeyBuilder cacheKeys;

    @Value("${LOGO_PATH}")
    private String logoPath;

    public StatsHandler(
            UserFeignClient userFeignClient,
            BlogFeignClient blogFeignClient,
            RedisCacheService cacheService,
            RedisService redisService) {
        this.userFeignClient = userFeignClient;
        this.blogFeignClient = blogFeignClient;
        this.cacheService = cacheService;
        this.cacheKeys = CacheKeyBuilder.forService("stats");
    }

    public DashboardStatsDto handleGetDashboardStats() {
        try {
            log.info("Starting handleGetDashboardStats");

            String cacheKey = cacheKeys.forMethod("handleGetDashboardStats");
            DashboardStatsDto stats = cacheService.getCacheData(cacheKey, DashboardStatsDto.class);

            if (stats == null) {
                log.debug("Cache miss for handleGetDashboardStats, fetching from services");
                // Get this month's date range
                YearMonth currentMonth = YearMonth.now();
                Instant startOfMonth = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                Instant endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
                log.debug("Current month range: {} to {}", startOfMonth, endOfMonth);

                // Execute only 2 Feign calls in parallel (instead of 7+)
                log.debug("Calling Feign clients for users and blogs");
                CompletableFuture<Response> allUsersFuture = CompletableFuture
                        .supplyAsync(() -> userFeignClient.getAllUsers());

                CompletableFuture<Response> allBlogsFuture = CompletableFuture
                        .supplyAsync(() -> blogFeignClient.getAllBlogs());

                // Wait for all futures to complete
                Response allUsersResponse = allUsersFuture.join();
                Response allBlogsResponse = allBlogsFuture.join();

                List<UserView> allUsers = allUsersResponse.getUserViews();
                List<BlogView> allBlogs = allBlogsResponse.getBlogViews();
                log.debug("Fetched {} users and {} blogs", allUsers.size(), allBlogs != null ? allBlogs.size() : 0);

                List<ActivityDto> recentActivities = handleGetRecentActivities(allUsers, allBlogs);
                log.debug("Generated {} recent activities", recentActivities.size());

                // Filter users locally
                long totalUsers = allUsers.size();
                long activeUsers = allUsers.stream()
                        .filter(u -> "active".equalsIgnoreCase(String.valueOf(u.getStatus())))
                        .count();
                long pendingUsers = allUsers.stream()
                        .filter(u -> "pending".equalsIgnoreCase(String.valueOf(u.getStatus())))
                        .count();
                long bannedUsers = allUsers.stream()
                        .filter(u -> "banned".equalsIgnoreCase(String.valueOf(u.getStatus())))
                        .count();
                long usersThisMonth = allUsers.stream()
                        .filter(u -> {
                            try {
                                Instant createdAt = u.getCreatedAt();
                                if (createdAt == null)
                                    return false;

                                return !createdAt.isBefore(startOfMonth)
                                        && !createdAt.isAfter(endOfMonth);
                            } catch (Exception e) {
                                log.warn("Failed to parse createdAt: {}", u.getCreatedAt(), e);
                                return false;
                            }
                        })
                        .count();
                log.debug("User stats: total={}, active={}, pending={}, banned={}, thisMonth={}",
                        totalUsers, activeUsers, pendingUsers, bannedUsers, usersThisMonth);

                // Filter blogs locally
                long totalBlogs = allBlogs != null ? allBlogs.size() : 0;
                long publicBlogs = allBlogs != null ? allBlogs.stream()
                        .filter(b -> Boolean.TRUE.equals(b.getIsVisibility()))
                        .count() : 0;
                long privateBlogs = allBlogs != null ? allBlogs.stream()
                        .filter(b -> !Boolean.TRUE.equals(b.getIsVisibility()))
                        .count() : 0;
                long blogsThisMonth = allBlogs != null ? allBlogs.stream()
                        .filter(b -> {
                            try {
                                String createdAtStr = b.getCreatedAt().toString();
                                if (createdAtStr == null)
                                    return false;

                                // Try to parse as Instant first (for blogs using Instant)
                                Instant createdAtInstant = Instant.parse(createdAtStr);
                                return !createdAtInstant.isBefore(startOfMonth)
                                        && !createdAtInstant.isAfter(endOfMonth);
                            } catch (Exception e) {
                                log.warn("Failed to parse blog createdAt: {}", b.getCreatedAt(), e);
                                return false;
                            }
                        })
                        .count() : 0;
                log.debug("Blog stats: total={}, public={}, private={}, thisMonth={}",
                        totalBlogs, publicBlogs, privateBlogs, blogsThisMonth);

                // Build dashboard stats
                stats = DashboardStatsDto.builder()
                        .totalUsers(totalUsers)
                        .activeUsers(activeUsers)
                        .pendingUsers(pendingUsers)
                        .bannedUsers(bannedUsers)
                        .usersCreatedThisMonth(usersThisMonth)
                        .totalBlogs(totalBlogs)
                        .publicBlogs(publicBlogs)
                        .privateBlogs(privateBlogs)
                        .blogsCreatedThisMonth(blogsThisMonth)
                        .recentActivities(recentActivities)
                        .build();

                cacheService.setCacheData(cacheKey, stats);
                log.debug("Dashboard stats built and cached");
            }
            log.info("Completed handleGetDashboardStats");

            return stats;
        } catch (OurException e) {
            log.warn("OurException in handleGetDashboardStats: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleGetDashboardStats: {}", e.getMessage(), e);
            throw e;
        }
    }

    public byte[] handleGetStatsReport() throws Exception {
        try {
            log.info("Starting handleGetStatsReport");
            String cacheKey = cacheKeys.forMethod("handleGetStatsReport");
            byte[] pdfBytes = cacheService.getCacheData(cacheKey, byte[].class);

            if (pdfBytes == null) {
                log.debug("Cache miss for handleGetStatsReport, generating report");
                DashboardStatsDto dashboardStats = handleGetDashboardStats();
                log.debug("Dashboard stats retrieved for report");

                // Load the report template
                ClassPathResource templateResource = new ClassPathResource("reports/dashboard-report.jrxml");
                InputStream reportStream = templateResource.getInputStream();
                JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                log.debug("Report template loaded and compiled");

                // Set report parameters - Cast long to Integer for JasperReports compatibility
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("LOGO_PATH", logoPath);
                parameters.put("REPORT_DATE",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                parameters.put("TOTAL_USERS", (int) dashboardStats.getTotalUsers());
                parameters.put("ACTIVE_USERS", (int) dashboardStats.getActiveUsers());
                parameters.put("PENDING_USERS", (int) dashboardStats.getPendingUsers());
                parameters.put("BANNED_USERS", (int) dashboardStats.getBannedUsers());
                parameters.put("USERS_CREATED_THIS_MONTH", (int) dashboardStats.getUsersCreatedThisMonth());
                parameters.put("TOTAL_BLOGS", (int) dashboardStats.getTotalBlogs());
                parameters.put("PUBLIC_BLOGS", (int) dashboardStats.getPublicBlogs());
                parameters.put("PRIVATE_BLOGS", (int) dashboardStats.getPrivateBlogs());
                parameters.put("BLOGS_CREATED_THIS_MONTH", (int) dashboardStats.getBlogsCreatedThisMonth());
                log.debug("Report parameters set");

                // Create JRBeanCollectionDataSource from activities
                JRDataSource dataSource = new JRBeanCollectionDataSource(
                        dashboardStats.getRecentActivities() != null ? dashboardStats.getRecentActivities()
                                : Collections.emptyList());
                log.debug("Data source created with {} activities",
                        dashboardStats.getRecentActivities() != null ? dashboardStats.getRecentActivities().size() : 0);

                // Compile and fill report
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                log.debug("Report filled");

                pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
                log.debug("Report exported to PDF, size: {} bytes", pdfBytes.length);

                cacheService.setCacheData(cacheKey, pdfBytes);
                log.debug("PDF cached");
            }

            log.info("Completed handleGetStatsReport");

            return pdfBytes;
        } catch (OurException e) {
            log.warn("OurException in handleGetStatsReport: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleGetStatsReport: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ActivityDto> handleGetRecentActivities(List<UserView> allUsers, List<BlogView> allBlogs) {
        try {
            log.info("Starting handleGetRecentActivities with {} users and {} blogs",
                    allUsers.size(), allBlogs != null ? allBlogs.size() : 0);

            List<ActivityDto> activities = new ArrayList<>();

            // Add user registration activities
            for (UserView user : allUsers) {
                activities.add(ActivityDto.builder()
                        .id("user-" + user.getId().toString())
                        .type("user_registered")
                        .description("New user registered: " + user.getUsername())
                        .timestamp(user.getCreatedAt().toString())
                        .userId(user.getId().toString())
                        .build());
            }
            log.debug("Added {} user registration activities", allUsers.size());

            // Add Blog creation activities
            if (allBlogs != null) {
                for (BlogView blog : allBlogs) {
                    activities.add(ActivityDto.builder()
                            .id("blog-" + blog.getId().toString())
                            .type("blog_created")
                            .description("New blog created: " + blog.getTitle())
                            .timestamp(blog.getCreatedAt().toString())
                            .userId(blog.getAuthorId() != null ? blog.getAuthorId().toString() : null)
                            .build());
                }
                log.debug("Added {} blog creation activities", allBlogs.size());
            }

            // Sort by timestamp descending and limit to 10
            activities.sort((a, b) -> {
                try {
                    Instant timeA = Instant.parse(a.getTimestamp());
                    Instant timeB = Instant.parse(b.getTimestamp());
                    return timeB.compareTo(timeA);
                } catch (Exception e) {
                    return 0;
                }
            });

            int limit = Math.min(10, activities.size());
            List<ActivityDto> result = new ArrayList<>(activities.subList(0, limit));

            log.info("Completed handleGetRecentActivities with {} activities", result.size());

            return result;
        } catch (OurException e) {
            log.warn("OurException in handleGetRecentActivities: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in handleGetRecentActivities: {}", e.getMessage(), e);
            throw e;
        }
    }
}
