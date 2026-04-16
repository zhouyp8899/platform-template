package com.zzl.platform.auth.service;

import com.zzl.platform.auth.vo.DepartmentVO;

import java.util.List;

/**
 * 部门服务
 */
public interface DepartmentService {

    /**
     * 查询所有部门树
     *
     * @return 部门树
     */
    List<DepartmentVO> treeAllDepartments();

    /**
     * 根据ID查询部门详情
     *
     * @param deptId 部门ID
     * @return 部门详情
     */
    DepartmentVO getDepartmentById(Long deptId);

    /**
     * 新增部门
     *
     * @param request  新增请求
     * @param operator 操作人ID
     * @return 部门ID
     */
    Long addDepartment(com.zzl.platform.auth.dto.DepartmentAddRequest request, Long operator);

    /**
     * 编辑部门
     *
     * @param request  编辑请求
     * @param operator 操作人ID
     */
    void editDepartment(com.zzl.platform.auth.dto.DepartmentEditRequest request, Long operator);

    /**
     * 删除部门
     *
     * @param deptId   部门ID
     * @param operator 操作人ID
     */
    void deleteDepartment(Long deptId, Long operator);
}
