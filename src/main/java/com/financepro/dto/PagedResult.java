package com.financepro.dto;

import lombok.*;

import java.util.List;

/**
 * Lightweight, framework-agnostic pagination wrapper used to replace
 * Spring Data's {@code Page} after the JPA layer was removed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResult<T> {

    /** Items on the current page. */
    private List<T> content;

    /** Zero-based current page index. */
    private int currentPage;

    /** Page size that was requested. */
    private int pageSize;

    /** Total number of pages (≥ 1, or 0 when there are no items). */
    private int totalPages;

    /** Total number of items across all pages. */
    private long totalElements;

    public static <T> PagedResult<T> of(List<T> all, int page, int size) {
        int total = all.size();
        int totalPages = (size <= 0) ? 1 : (int) Math.ceil(total / (double) size);
        if (totalPages == 0) totalPages = 1;
        int from = Math.max(0, Math.min(page * size, total));
        int to   = Math.max(0, Math.min(from + size, total));
        return PagedResult.<T>builder()
                .content(all.subList(from, to))
                .currentPage(page)
                .pageSize(size)
                .totalPages(totalPages)
                .totalElements(total)
                .build();
    }
}
