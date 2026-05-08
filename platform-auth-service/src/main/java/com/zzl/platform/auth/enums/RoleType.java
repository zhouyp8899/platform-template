package com.zzl.platform.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 角色类型枚举
 */
@Getter
public enum RoleType {

    /**
     * 系统角色
     */
    SYSTEM("SYSTEM", "系统角色"),

    /**
     * 业务角色
     */
    BUSINESS("BUSINESS", "业务角色");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    RoleType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RoleType of(String code) {
        for (RoleType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
