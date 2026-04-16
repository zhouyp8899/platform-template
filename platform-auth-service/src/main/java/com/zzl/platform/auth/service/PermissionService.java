package com.zzl.platform.auth.service;

import com.zzl.platform.auth.dto.PermissionEditRequest;
import com.zzl.platform.auth.entity.SysPermission;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.PermissionVO;

import java.util.List;

/**
 * 权限服务
 */
public interface PermissionService {

    /**
     * 分页查询权限列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param keyword  关键词
     * @return 分页响应
     */
    PageResponse<PermissionVO> pagePermission(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 查询所有权限列表
     *
     * @return 权限列表
     */
    List<PermissionVO> listAllPermissions();

    /**
     * 查询权限分组列表
     *
     * @return 分组列表
     */
    List<String> listPermissionGroups();

    /**
     * 根据ID查询权限详情
     *
     * @param permissionId 权限ID
     * @return 权限详情
     */
    PermissionVO getPermissionById(Long permissionId);

    /**
     * 新增权限
     *
     * @param request  新增请求
     * @param operator 操作人ID
     * @return 权限ID
     */
    Long addPermission(com.zzl.platform.auth.dto.PermissionAddRequest request, Long operator);

    /**
     * 编辑权限
     *
     * @param request  编辑请求
     * @param operator 操作人ID
     */
    void editPermission(PermissionEditRequest request, Long operator);

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @param operator     操作人ID
     */
    void deletePermission(Long permissionId, Long operator);

    /**
     * 根据权限编码查询权限
     *
     * @param permissionCode 权限编码
     * @return 权限实体
     */
    SysPermission getPermissionByPermissionCode(String permissionCode);
}
