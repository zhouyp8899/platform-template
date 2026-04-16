package com.zzl.platform.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 资源类型枚举
 */
@Getter
public enum ResourceType {

    /**
     * 接口
     */
    API("api", "接口"),

    /**
     * 菜单
     */
    MENU("menu", "菜单"),

    /**
     * 按钮
     */
    BUTTON("button", "按钮");

    @EnumValue
    private final String code;

    @JsonValue
    private final String desc;

    ResourceType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ResourceType of(String code) {
        for (ResourceType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
