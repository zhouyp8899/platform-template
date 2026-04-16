package com.zzl.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.platform.auth.entity.SysPermission;
import com.zzl.platform.auth.vo.PermissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 查询权限详情（包含菜单信息）
     */
    @Select("<script>" +
            "SELECT " +
            "  p.id, p.permission_code, p.permission_name, p.resource_type, p.resource_path, " +
            "  p.http_method, p.menu_id, m.menu_name, p.permission_group, p.description, " +
            "  p.status, p.sort, p.create_time, p.update_time " +
            "FROM t_sys_permission p " +
            "LEFT JOIN t_sys_menu m ON p.menu_id = m.id " +
            "WHERE p.id = #{permissionId} AND p.deleted = 0 " +
            "</script>")
    PermissionVO selectPermissionDetailById(@Param("permissionId") Long permissionId);

    /**
     * 查询所有权限
     */
    @Select("<script>" +
            "SELECT " +
            "  p.id, p.permission_code, p.permission_name, p.resource_type, p.resource_path, " +
            "  p.http_method, p.menu_id, m.menu_name, p.permission_group, p.description, " +
            "  p.status, p.sort, p.create_time, p.update_time " +
            "FROM t_sys_permission p " +
            "LEFT JOIN t_sys_menu m ON p.menu_id = m.id " +
            "WHERE p.deleted = 0 " +
            "ORDER BY p.sort " +
            "</script>")
    List<PermissionVO> selectAllPermissions();

    /**
     * 根据权限编码查询权限
     */
    @Select("SELECT * FROM t_sys_permission WHERE permission_code = #{permissionCode} AND deleted = 0")
    SysPermission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 查询权限分组列表
     */
    @Select("SELECT DISTINCT permission_group FROM t_sys_permission WHERE deleted permission_group IS NOT NULL ORDER BY permission_group")
    List<String> selectPermissionGroups();
}
