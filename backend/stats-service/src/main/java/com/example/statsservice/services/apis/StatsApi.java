package com.example.statsservice.services.apis;

import com.example.rediscommon.services.RedisService;
import com.example.rediscommon.services.ApiResponseHandler;
import com.example.rediscommon.utils.CacheKeyBuilder;
import com.example.statsservice.dtos.ActivityDto;
import com.example.statsservice.dtos.DashboardStatsDto;
import com.example.statsservice.services.feigns.UserFeignClient;
import com.example.statsservice.services.feigns.BlogFeignClient;
import com.example.statsservice.dtos.responses.Response;

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
import java.util.concurrent.TimeUnit;

@Service
public class StatsApi extends BaseApi {

    private final UserFeignClient userFeignClient;
    private final BlogFeignClient blogFeignClient;
    private final RedisService redisService;
    private final ApiResponseHandler<Response> responseHandler;
    private final CacheKeyBuilder cacheKeys;

    @Value("${LOGO_PATH}")
    private String logoPath;

    public StatsApi(
            UserFeignClient userFeignClient,
            BlogFeignClient blogFeignClient,
            RedisService redisService,
            ApiResponseHandler<Response> responseHandler) {
        this.userFeignClient = userFeignClient;
        this.blogFeignClient = blogFeignClient;
        this.redisService = redisService;
        this.responseHandler = responseHandler;
        this.cacheKeys = CacheKeyBuilder.forService("stats");
    }

    /**
     * Get dashboard statistics from cache or compute if not available
     */
    private DashboardStatsDto getDashboardStatsFromCacheOrCompute() {
        String cacheKey = "dashboard_stats";

        // Try to get from cache first
        if (redisService.hasKey(cacheKey)) {
            logger.info("Retrieving dashboard statistics from Redis cache");
            try {
                return (DashboardStatsDto) redisService.get(cacheKey);
            } catch (Exception e) {
                logger.warn("Failed to retrieve dashboard stats from cache, computing fresh data: {}", e.getMessage());
            }
        }

        // Compute fresh data
        logger.info("Computing fresh dashboard statistics");
        DashboardStatsDto stats = computeDashboardStats();

        // Cache the result for 10 minutes
        try {
            redisService.set(cacheKey, stats, 10, TimeUnit.MINUTES);
            logger.info("Dashboard statistics cached in Redis for 10 minutes");
        } catch (Exception e) {
            logger.warn("Failed to cache dashboard stats: {}", e.getMessage());
        }

        return stats;
    }

    /**
     * Compute dashboard statistics (optimized - fetch bulk data and filter locally)
     */
    private DashboardStatsDto computeDashboardStats() {
        logger.info("Fetching dashboard statistics with optimized approach...");

        // Get this month's date range
        YearMonth currentMonth = YearMonth.now();
        Instant startOfMonth = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        // Execute only 3 Feign calls in parallel (instead of 7+)
        CompletableFuture<Response> allUsersFuture = CompletableFuture
                .supplyAsync(() -> userFeignClient.getAllUsers());

        CompletableFuture<Response> allBlogsFuture = CompletableFuture
                .supplyAsync(() -> blogFeignClient.getAllBlogs());

        CompletableFuture<List<ActivityDto>> recentActivitiesFuture = CompletableFuture
                .supplyAsync(() -> handleGetRecentActivities());

        // Wait for all futures to complete
        Response allUsersResponse = allUsersFuture.join();
        Response allBlogsResponse = allBlogsFuture.join();
        List<ActivityDto> recentActivities = recentActivitiesFuture.join();

        // Filter users locally
        List<Map<String, Object>> allUsers = allUsersResponse.getUsers();
        long totalUsers = allUsers != null ? allUsers.size() : 0;
        long activeUsers = allUsers != null ? allUsers.stream()
                .filter(u -> "ACTIVE".equals(u.get("status")))
                .count() : 0;
        long pendingUsers = allUsers != null ? allUsers.stream()
                .filter(u -> "PENDING".equals(u.get("status")))
                .count() : 0;
        long bannedUsers = allUsers != null ? allUsers.stream()
                .filter(u -> "BANNED".equals(u.get("status")))
                .count() : 0;
        long usersThisMonth = allUsers != null ? allUsers.stream()
                .filter(u -> {
                    try {
                        Instant createdAt = Instant.parse((String) u.get("createdAt"));
                        return !createdAt.isBefore(startOfMonth) && !createdAt.isAfter(endOfMonth);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count() : 0;

        // Filter blogs locally
        List<Map<String, Object>> allBlogs = allBlogsResponse.getBlogs();
        long totalBlogs = allBlogs != null ? allBlogs.size() : 0;
        long publicBlogs = allBlogs != null ? allBlogs.stream()
                .filter(b -> Boolean.TRUE.equals(b.get("isVisibility")))
                .count() : 0;
        long privateBlogs = allBlogs != null ? allBlogs.stream()
                .filter(b -> !Boolean.TRUE.equals(b.get("isVisibility")))
                .count() : 0;
        long blogsThisMonth = allBlogs != null ? allBlogs.stream()
                .filter(b -> {
                    try {
                        Instant createdAt = Instant.parse((String) b.get("createdAt"));
                        return !createdAt.isBefore(startOfMonth) && !createdAt.isAfter(endOfMonth);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count() : 0;

        // Build dashboard stats
        DashboardStatsDto stats = DashboardStatsDto.builder()
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

        logger.info("Dashboard statistics computed successfully (optimized approach)");
        return stats;
    }

    public byte[] handleGetStatsReport() throws Exception {
        logger.info("Generating dashboard statistics report...");

        // Fetch dashboard stats from your service
        DashboardStatsDto dashboardStats = getDashboardStatsFromCacheOrCompute();

        // Load the report template
        ClassPathResource templateResource = new ClassPathResource("reports/dashboard-report.jrxml");
        InputStream reportStream = templateResource.getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // Set report parameters - Cast long to Integer for JasperReports compatibility
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("LOGO_PATH", logoPath);
        parameters.put("REPORT_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        parameters.put("TOTAL_USERS", (int) dashboardStats.getTotalUsers());
        parameters.put("ACTIVE_USERS", (int) dashboardStats.getActiveUsers());
        parameters.put("PENDING_USERS", (int) dashboardStats.getPendingUsers());
        parameters.put("BANNED_USERS", (int) dashboardStats.getBannedUsers());
        parameters.put("USERS_CREATED_THIS_MONTH", (int) dashboardStats.getUsersCreatedThisMonth());
        parameters.put("TOTAL_BlogS", (int) dashboardStats.getTotalBlogs());
        parameters.put("PUBLIC_BlogS", (int) dashboardStats.getPublicBlogs());
        parameters.put("PRIVATE_BlogS", (int) dashboardStats.getPrivateBlogs());
        parameters.put("BlogS_CREATED_THIS_MONTH", (int) dashboardStats.getBlogsCreatedThisMonth());

        // Create JRBeanCollectionDataSource from activities
        JRDataSource dataSource = new JRBeanCollectionDataSource(
                dashboardStats.getRecentActivities() != null ? dashboardStats.getRecentActivities()
                        : Collections.emptyList());

        // Compile and fill report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Export to PDF
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        logger.info("Dashboard statistics report generated successfully");
        return pdfBytes;
    }

    public Response getDashboardStats() {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethod("getDashboardStats"),
                90,
                this::getDashboardStatsFromCacheOrCompute,
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setDashboardStats,
                "Dashboard statistics retrieved successfully",
                200);
    }

    public Response getStatsReport() {
        return responseHandler.executeWithResponse(
                cacheKeys.forMethod("getStatsReport"),
                90,
                () -> {
                    try {
                        return handleGetStatsReport();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
                    }
                },
                Response::new,
                Response::setStatusCode,
                Response::setMessage,
                Response::setStatsReport,
                "Stats report retrieved successfully",
                200);
    }

    private List<ActivityDto> handleGetRecentActivities() {
        List<ActivityDto> activities = new ArrayList<>();

        try {
            // Get recent users
            Response recentUsersResponse = userFeignClient.getRecentUsers(5);

            // Add user registration activities
            for (Map<String, Object> user : recentUsersResponse.getUsers()) {
                activities.add(ActivityDto.builder()
                        .id("user-" + user.get("id").toString())
                        .type("user_registered")
                        .description("New user registered: " + (String) user.get("username"))
                        .timestamp(Instant.now().toString())
                        .userId(user.get("id").toString())
                        .build());
            }

            // Get recent Blogs
            Response recentBlogsResponse = blogFeignClient.getRecentBlogs(5);

            // Add Blog creation activities
            for (Map<String, Object> blog : recentBlogsResponse.getBlogs()) {
                activities.add(ActivityDto.builder()
                        .id("blog-" + blog.get("id").toString())
                        .type("blog_created")
                        .description("New Blog created: " + (String) blog.get("title"))
                        .timestamp((String) blog.get("createdAt"))
                        .userId(blog.get("userId").toString())
                        .build());
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

            // Convert subList to ArrayList to avoid Redis serialization issues
            int limit = Math.min(10, activities.size());
            return new ArrayList<>(activities.subList(0, limit));
        } catch (Exception e) {
            logger.warn("Error fetching recent activities: {}", e.getMessage());
            return activities; // Return partial results
        }
    }
}
