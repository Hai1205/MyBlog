package com.example.userservice.dtos.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagination {
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public Pagination(long totalItems, int totalPages, int currentPage) {
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }
}