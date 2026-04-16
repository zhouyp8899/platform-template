package com.zzl.platform.auth.service;

import com.zzl.platform.auth.dto.GrantPermissionsRequest;
import com.zzl.platform.auth.entity.SysRole;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.RoleVO;

import java.util.List;

/**
 * 角色服务
 */
public interface RoleService {

    /**
     * 分页查询角色列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param keyword  关键词
     * @return 分页响应
     */
    PageResponse<RoleVO> pageRole(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 查询所有角色列表（用于下拉框）
     *
     * @return 角色列表
     */
    List<RoleVO> listAllRoles();

    /**
     * 根据ID查询角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleVO getRoleById(Long roleId);

    /**
     * 新增角色
     *
     * @param request  新增请求
     * @param operator 操作人ID
     * @return 角色ID
     */
    Long addRole(com.zzl.platform.auth.dto.RoleAddRequest request, Long operator);

    /**
     * 编辑角色
     *
     * @param request  编辑请求
     * @param operator 操作人ID
     */
    void editRole(com.zzl.platform.auth.dto.RoleEditRequest request, Long operator);

    /**
     * 删除角色
     *
     * @param roleId   角色ID
     * @param operator 操作人ID
     */
    void deleteRole(Long roleId, Long operator);

    /**
     * 为角色分配权限
     *
     * @param request  分配权限请求
     * @param operator 操作人ID
     */
    void grantPermissions(GrantPermissionsRequest request, Long operator);

    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissions(Long roleId);

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色实体
     */
    SysRole getRoleByRoleCode(String roleCode);
}
