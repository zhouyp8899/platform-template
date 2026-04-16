package com.zzl.platform.auth.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页响应VO
 */
@Data
public class PageResponse<T> {

    /**
     * 总数
     */
    private Long total;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    public PageResponse() {
    }

    public PageResponse(Long total, List<T> list, Integer pageNum, Integer pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (int) ((total + pageSize - 1) / pageSize);
    }
}
