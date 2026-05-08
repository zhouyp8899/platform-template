package com.zzl.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.platform.auth.entity.SysMenu;
import com.zzl.platform.auth.vo.MenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询所有菜单树
     */
    @Select("<script>" +
            "SELECT " +
            "  m.id, m.menu_name, m.parent_id, m.menu_type, m.menu_icon, m.menu_path, " +
            "  m.component, m.redirect, m.is_cache, m.is_visible, m.is_external, " +
            "  m.menu_sort, m.status, m.remark, m.create_time, m.update_time " +
            "FROM t_sys_menu m " +
            "WHERE m.deleted = 0 " +
            "ORDER BY m.parent_id, m.menu_sort " +
            "</script>")
    List<MenuVO> selectAllMenus();

    /**
     * 查询用户可见菜单树（通过角色-菜单直接关联）
     */
    @Select("<script>" +
            "SELECT DISTINCT " +
            "  m.id, m.menu_name, m.parent_id, m.menu_type, m.menu_icon, m.menu_path, " +
            "  m.component, m.redirect, m.is_cache, m.is_visible, m.is_external, " +
            "  m.menu_sort, m.status, m.remark, m.create_time, m.update_time " +
            "FROM t_sys_menu m " +
            "INNER JOIN t_sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN t_sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.deleted = 0 " +
            "ORDER BY m.parent_id, m.menu_sort " +
            "</script>")
    List<MenuVO> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据父级ID查询菜单列表
     */
    @Select("SELECT * FROM t_sys_menu WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY menu_sort")
    List<SysMenu> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询菜单关联的权限编码
     */
    @Select("SELECT p.permission_code " +
            "FROM t_sys_menu_permission mp " +
            "INNER JOIN t_sys_permission p ON mp.permission_id = p.id " +
            "WHERE mp.menu_id = #{menuId} AND p.status = 1 AND p.deleted = 0")
    List<String> selectPermissionCodesByMenuId(@Param("menuId") Long menuId);
}
