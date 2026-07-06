package com.aidsp.platform.core.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页响应。
 *
 * @param <T> 列表元素类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表。 */
    private List<T> list;

    /** 总记录数。 */
    private long total;

    /** 当前页（1-based）。 */
    private long page;

    /** 每页大小。 */
    private long size;

    /** 总页数。 */
    private long pages;

    /** 构造空分页（size=0）。 */
    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .list(Collections.emptyList())
                .total(0L)
                .page(1L)
                .size(0L)
                .pages(0L)
                .build();
    }

    /**
     * 从全量列表中切片构成分页响应。
     */
    public static <T> PageResponse<T> of(List<T> all, long page, long size) {
        if (all == null) {
            all = Collections.emptyList();
        }
        if (size <= 0) {
            size = 20L;
        }
        if (page <= 0) {
            page = 1L;
        }
        long total = all.size();
        long pages = (total + size - 1) / size;
        long from = Math.min((page - 1) * size, total);
        long to = Math.min(from + size, total);
        return PageResponse.<T>builder()
                .list(all.subList((int) from, (int) to))
                .total(total)
                .page(page)
                .size(size)
                .pages(pages)
                .build();
    }
}
