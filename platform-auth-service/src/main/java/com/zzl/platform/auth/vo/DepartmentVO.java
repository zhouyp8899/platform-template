package com.zzl.platform.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门VO
 */
@Data
public class DepartmentVO {

    /**
     * 部门ID
     */
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
     * 状态: 1-正常, 2-禁用
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 用户数量
     */
    private Long userCount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子部门
     */
    private List<DepartmentVO> children;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
