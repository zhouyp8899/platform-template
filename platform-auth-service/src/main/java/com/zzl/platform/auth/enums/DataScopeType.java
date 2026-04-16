package com.zzl.platform.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 数据权限范围枚举
 */
@Getter
public enum DataScopeType {

    /**
     * 全部数据
     */
    ALL("ALL", "全部数据"),

    /**
     * 本部门及子部门数据
     */
    DEPT("DEPT", "本部门及子部门"),

    /**
     * 仅本部门数据
     */
    DEPT_ONLY("DEPT_ONLY", "仅本部门"),

    /**
     * 仅本人数据
     */
    SELF("SELF", "仅本人"),

    /**
     * 自定义部门数据
     */
    CUSTOM("CUSTOM", "自定义部门");

    @EnumValue
    private final String code;

    @JsonValue
    private final String desc;

    DataScopeType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DataScopeType of(String code) {
        for (DataScopeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
