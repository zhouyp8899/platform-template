package com.zzl.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门新增请求DTO
 */
@Data
public class DepartmentAddRequest {

    /**
     * 部门编码
     */
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50")
    private String deptCode;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100")
    private String deptName;

    /**
     * 父级部门ID
     */
    private Long parentId;

    /**
     * 显示顺序
     */
    private Integer deptSort;

    /**
     * 负责人
     */
    @Size(max = 50, message = "负责人长度不能超过50")
    private String leader;

    /**
     * 负责人电话
     */
    @Size(max = 20, message = "负责人电话长度不能超过20")
    private String leaderPhone;

    /**
     * 部门邮箱
     */
    @Size(max = 100, message = "部门邮箱长度不能超过100")
    private String email;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
