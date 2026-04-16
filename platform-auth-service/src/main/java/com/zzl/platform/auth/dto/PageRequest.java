package com.zzl.platform.auth.dto;

import lombok.Data;

/**
 * 分页请求DTO
 */
@Data
public class PageRequest<T> {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 查询参数
     */
    private T param;
}
