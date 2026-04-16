package com.zzl.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求DTO
 */
@Data
public class RefreshTokenRequest {

    /**
     * 刷新Token
     */
    @NotBlank(message = "刷新Token不能为空")
    private String refreshToken;

    /**
     * Token类型
     */
    private String tokenType;
}
