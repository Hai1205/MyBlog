package com.example.statsservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    // User statistics
    private long totalUsers;
    private long activeUsers;
    private long pendingUsers;
    private long bannedUsers;
    private long usersCreatedThisMonth;

    // Blog statistics
    private long totalBlogs;
    private long publicBlogs;
    private long privateBlogs;
    private long blogsCreatedThisMonth;
    
    // Recent activities
    private List<ActivityDto> recentActivities;
}
