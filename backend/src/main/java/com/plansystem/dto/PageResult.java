package com.plansystem.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified paginated response wrapper.
 *
 * @param <T> record type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long page;
    private long pageSize;

    /**
     * Convert MyBatis-Plus IPage to PageResult.
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    /**
     * Build from list and pagination info.
     */
    public static <T> PageResult<T> of(List<T> records, long total, long page, long pageSize) {
        return new PageResult<>(records, total, page, pageSize);
    }
}
