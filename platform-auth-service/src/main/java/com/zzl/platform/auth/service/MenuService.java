package com.zzl.platform.auth.service;

import com.zzl.platform.auth.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务
 */
public interface MenuService {

    /**
     * 查询所有菜单树
     *
     * @return 菜单树
     */
    List<MenuVO> treeAllMenus();

    /**
     * 查询用户可见菜单树
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<MenuVO> treeUserMenus(Long userId);

    /**
     * 根据ID查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    MenuVO getMenuById(Long menuId);

    /**
     * 新增菜单
     *
     * @param request  新增请求
     * @param operator 操作人ID
     * @return 菜单ID
     */
    Long addMenu(com.zzl.platform.auth.dto.MenuAddRequest request, Long operator);

    /**
     * 编辑菜单
     *
     * @param request  编辑请求
     * @param operator 操作人ID
     */
    void editMenu(com.zzl.platform.auth.dto.MenuEditRequest request, Long operator);

    /**
     * 删除菜单
     *
     * @param menuId   菜单ID
     * @param operator 操作人ID
     */
    void deleteMenu(Long menuId, Long operator);
}
