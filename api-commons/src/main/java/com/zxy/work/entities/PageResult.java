package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义分页查询结果类
 * @param <T> 泛型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PageResult<T> implements Serializable {
    private long totalItems;
    private int pageSize;
    private int totalPages;
    private int currentPage;
    private List<T> items;

    public PageResult(long totalItems, int pageSize, int currentPage, List<T> items) {
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.items = items;
    }

    // 根据总数据条数和每页显示条数计算总页数
    public void setTotalPages() {
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }
}
