package com.zzl.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.platform.auth.entity.SysUser;
import com.zzl.platform.auth.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户详情（包含角色、部门信息）
     */
    @Select("<script>" +
            "SELECT " +
            "  u.id, u.username, u.real_name, u.nick_name, u.phone, u.email, u.avatar, u.gender, " +
            "  u.dept_id, d.dept_name, u.user_type, u.data_scope, u.status, " +
            "  u.last_login_time, u.last_login_ip, u.remark, u.create_time, u.update_time " +
            "FROM t_sys_user u " +
            "LEFT JOIN t_sys_department d ON u.dept_id = d.id " +
            "WHERE u.id = #{userId} AND u.deleted = 0 " +
            "</script>")
    UserVO selectUserDetailById(@Param("userId") Long userId);

    /**
     * 查询用户的角色编码列表
     */
    @Select("SELECT r.role_code " +
            "FROM t_sys_user_role ur " +
            "INNER JOIN t_sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的角色ID列表
     */
    @Select("SELECT ur.role_id " +
            "FROM t_sys_user_role ur " +
            "INNER JOIN t_sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code " +
            "FROM t_sys_user_role ur " +
            "INNER JOIN t_sys_role_permission rp ON ur.role_id = rp.role_id " +
            "INNER JOIN t_sys_permission p ON rp.permission_id = p.id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 AND p.deleted = 0")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户是否存在
     */
    @Select("SELECT COUNT(1) FROM t_sys_user WHERE id = #{userId} AND deleted = 0")
    int countById(@Param("userId") Long userId);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM t_sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM t_sys_user WHERE phone = #{phone} AND deleted = 0")
    SysUser selectByPhone(@Param("phone") String phone);
}
