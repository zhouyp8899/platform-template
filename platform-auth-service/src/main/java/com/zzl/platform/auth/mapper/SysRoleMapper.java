package com.zzl.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.platform.auth.entity.SysRole;
import com.zzl.platform.auth.vo.RoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询角色详情（包含权限信息）
     */
    @Select("<script>" +
            "SELECT " +
            "  r.id, r.role_code, r.role_name, r.role_type, r.data_scope, r.description, " +
            "  r.is_system, r.status, r.sort, r.create_time, r.update_time " +
            "FROM t_sys_role r " +
            "WHERE r.id = #{roleId} AND r.deleted = 0 " +
            "</script>")
    RoleVO selectRoleDetailById(@Param("roleId") Long roleId);

    /**
     * 查询角色的权限ID列表
     */
    @Select("SELECT rp.permission_id " +
            "FROM t_sys_role_permission rp " +
            "INNER JOIN t_sys_permission p ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1 AND p.deleted = 0")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询角色的权限编码列表
     */
    @Select("SELECT p.permission_code " +
            "FROM t_sys_role_permission rp " +
            "INNER JOIN t_sys_permission p ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1 AND p.deleted = 0")
    List<String> selectPermissionCodesByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询角色的用户数量
     */
    @Select("SELECT COUNT(1) FROM t_sys_user_role WHERE role_id = #{roleId}")
    long selectUserCountByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询所有角色（用于下拉框）
     */
    @Select("SELECT id, role_code, role_name, status FROM t_sys_role WHERE deleted = 0 ORDER BY sort")
    List<RoleVO> selectAllRoles();

    /**
     * 根据角色编码查询角色
     */
    @Select("SELECT * FROM t_sys_role WHERE role_code = #{roleCode} AND deleted = 0")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);
}
