package com.zzl.platform.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改状态请求DTO
 */
@Data
public class ChangeStatusRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
