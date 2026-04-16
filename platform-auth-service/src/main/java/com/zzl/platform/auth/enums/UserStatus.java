package com.zzl.platform.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {

    /**
     * *正常
     */
    NORMAL(1, "正常"),

    /**
     * 禁用
     */
    DISABLED(2, "禁用"),

    /**
     * 锁定
     */
    LOCKED(3, "锁定");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus of(Integer code) {
        for (UserStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
