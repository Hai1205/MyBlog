package com.example.statsservice.services.apis;

import com.example.rediscommon.services.RedisService;
import com.example.rediscommon.services.RateLimiterService;
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
    private final RateLimiterService rateLimiterService;

    @Value("${LOGO_PATH}")
    private String logoPath;

    public StatsApi(
            UserFeignClient userFeignClient,
            BlogFeignClient blogFeignClient,
            RedisService redisService,
            RateLimiterService rateLimiterService) {
        this.userFeignClient = userFeignClient;
        this.blogFeignClient = blogFeignClient;
        this.redisService = redisService;
        this.rateLimiterService = rateLimiterService;
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
     * Compute dashboard statistics from gRPC calls (optimized with parallel
     * execution)
     */
    private DashboardStatsDto computeDashboardStats() {
        logger.info("Fetching dashboard statistics with parallel execution...");

        // Get this month's date range
        YearMonth currentMonth = YearMonth.now();
        Instant startOfMonth = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        String startDateStr = startOfMonth.toString();
        String endDateStr = endOfMonth.toString();

        // Execute all Feign calls in parallel
        CompletableFuture<Response> userStatsFuture = CompletableFuture
                .supplyAsync(() -> userFeignClient.getUserStats());

        CompletableFuture<Response> totalBlogsFuture = CompletableFuture
                .supplyAsync(() -> blogFeignClient.getTotalBlogs());

        CompletableFuture<Response> publicBlogsFuture = CompletableFuture
                .supplyAsync(() -> blogFeignClient.getBlogsByVisibility(true));

        CompletableFuture<Response> privateBlogsFuture = CompletableFuture
                .supplyAsync(() -> blogFeignClient.getBlogsByVisibility(false));

        CompletableFuture<Response> usersThisMonthFuture = CompletableFuture
                .supplyAsync(() -> userFeignClient.getUsersCreatedInRange(startDateStr, endDateStr));

        CompletableFuture<Response> blogsThisMonthFuture = CompletableFuture
                .supplyAsync(() -> blogFeignClient.getBlogsCreatedInRange(startDateStr, endDateStr));

        CompletableFuture<List<ActivityDto>> recentActivitiesFuture = CompletableFuture
                .supplyAsync(() -> handleGetRecentActivities());

        // Wait for all futures to complete and get results
        Response userStatsResponse = userStatsFuture.join();
        Response totalBlogsResponse = totalBlogsFuture.join();
        Response publicBlogsResponse = publicBlogsFuture.join();
        Response privateBlogsResponse = privateBlogsFuture.join();
        Response usersThisMonthResponse = usersThisMonthFuture.join();
        Response blogsThisMonthResponse = blogsThisMonthFuture.join();
        List<ActivityDto> recentActivities = recentActivitiesFuture.join();

        // Build dashboard stats
        DashboardStatsDto stats = DashboardStatsDto.builder()
                .totalUsers(toLong(userStatsResponse.getAdditionalData().get("totalUsers")))
                .activeUsers(toLong(userStatsResponse.getAdditionalData().get("activeUsers")))
                .pendingUsers(toLong(userStatsResponse.getAdditionalData().get("pendingUsers")))
                .bannedUsers(toLong(userStatsResponse.getAdditionalData().get("bannedUsers")))
                .usersCreatedThisMonth(toLong(usersThisMonthResponse.getAdditionalData().get("count")))
                .totalBlogs(toLong(totalBlogsResponse.getAdditionalData().get("total")))
                .publicBlogs(toLong(publicBlogsResponse.getAdditionalData().get("count")))
                .privateBlogs(toLong(privateBlogsResponse.getAdditionalData().get("count")))
                .blogsCreatedThisMonth(toLong(blogsThisMonthResponse.getAdditionalData().get("count")))
                .recentActivities(recentActivities)
                .build();

        logger.info("Dashboard statistics fetched successfully");
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
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("stats:getDashboardStats", 90, 60)) {
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            DashboardStatsDto stats = getDashboardStatsFromCacheOrCompute();

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Dashboard statistics retrieved successfully");
            response.setDashboardStats(stats);
            return response;
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats: {}", e.getMessage(), e);
            return buildErrorResponse(500, "Failed to fetch dashboard statistics");
        }
    }

    public Response getStatsReport() {
        Response response = new Response();

        try {
            long startTime = System.currentTimeMillis();

            // Rate limiting: 90 req/min for GET APIs
            if (!rateLimiterService.isAllowed("stats:getStatsReport", 90, 60)) {
                response.setStatusCode(429);
                response.setMessage("Rate limit exceeded. Please try again later.");
                return response;
            }

            byte[] report = handleGetStatsReport();

            long endTime = System.currentTimeMillis();
            logger.info("Completed request in {} ms", endTime - startTime);

            response.setStatusCode(200);
            response.setMessage("Stats report retrieved successfully");
            response.setStatsReport(report);
            return response;
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats: {}", e.getMessage(), e);
            return buildErrorResponse(500, "Failed to fetch dashboard statistics");
        }
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

    /**
     * Safely convert Number to Long
     */
    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            logger.warn("Cannot convert {} to Long, returning 0", value);
            return 0L;
        }
    }
}
