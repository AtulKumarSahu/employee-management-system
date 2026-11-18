package com.ems.employeemanagement.dto.response;

import java.util.List;

/**
 * Pagination response wrapper for paginated list endpoints.
 *
 * <p>Wraps the content list with pagination metadata so frontend
 * clients know how many pages exist and can render pagination controls.
 *
 * <p>Example JSON output:
 * <pre>
 * {
 *   "content": [...],
 *   "pageNumber": 0,
 *   "pageSize": 10,
 *   "totalElements": 125,
 *   "totalPages": 13,
 *   "last": false,
 *   "first": true
 * }
 * </pre>
 *
 * @param <T> the DTO type in the content list
 */
public record PaginationResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last,
        boolean first
) {
    /**
     * Convenience factory from a Spring Data {@link org.springframework.data.domain.Page}.
     * Keeps controllers clean — just call PaginationResponse.from(page).
     *
     * @param page the Spring Data Page result
     * @param <T>  the content type
     * @return a populated PaginationResponse
     */
    public static <T> PaginationResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return new PaginationResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }
}

