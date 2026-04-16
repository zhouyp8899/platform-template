package com.zzl.platform.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
public enum UserType {

    /**
     * H5用户
     */
    H5("H5", "H5用户"),

    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员");

    @EnumValue
    private final String code;

    @JsonValue
    private final String desc;

    UserType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserType of(String code) {
        for (UserType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
