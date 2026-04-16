package com.zzl.platform.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统部门实体
 */
@Data
@TableName("t_sys_department")
public class SysDepartment {

    /**
     * 部门ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 父级部门ID
     */
    private Long parentId;

    /**
     * 部门层级
     */
    private Integer deptLevel;

    /**
     * 部门路径
     */
    private String deptPath;

    /**
     * 显示顺序
     */
    private Integer deptSort;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 负责人电话
     */
    private String leaderPhone;

    /**
     * 部门邮箱
     */
    private String email;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
