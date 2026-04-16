package com.zzl.platform.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 菜单类型枚举
 */
@Getter
public enum MenuType {

    /**
     * 目录
     */
    DIRECTORY("M", "目录"),

    /**
     * 菜单
     */
    MENU("C", "菜单"),

    /**
     * 按钮
     */
    BUTTON("F", "按钮");

    @EnumValue
    private final String code;

    @JsonValue
    private final String desc;

    MenuType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MenuType of(String code) {
        for (MenuType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
