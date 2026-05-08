package com.zzl.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.platform.auth.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色菜单关联Mapper
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 删除角色的所有菜单关联
     */
    @Delete("DELETE FROM t_sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除菜单的所有角色关联
     */
    @Delete("DELETE FROM t_sys_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuId(@Param("menuId") Long menuId);

    /**
     * 批量插入角色菜单关联
     */
    int batchInsert(@Param("list") List<SysRoleMenu> list);

    /**
     * 查询角色关联的菜单ID列表
     */
    @Select("SELECT menu_id FROM t_sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
