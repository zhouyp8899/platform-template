package com.zzl.platform.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzl.platform.auth.dto.MenuAddRequest;
import com.zzl.platform.auth.dto.MenuEditRequest;
import com.zzl.platform.auth.entity.SysMenu;
import com.zzl.platform.auth.mapper.SysMenuMapper;
import com.zzl.platform.auth.mapper.SysRoleMenuMapper;
import com.zzl.platform.auth.service.MenuService;
import com.zzl.platform.auth.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务实现
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public MenuServiceImpl(SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<MenuVO> treeAllMenus() {
        // 1. 查询所有菜单
        List<MenuVO> allMenus = menuMapper.selectAllMenus();

        // 2. 补充描述字段
        fillMenuDesc(allMenus);

        // 3. 构建树形结构
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<MenuVO> treeUserMenus(Long userId) {
        // 1. 查询用户可见菜单
        List<MenuVO> userMenus = menuMapper.selectMenusByUserId(userId);

        // 2. 补充描述字段
        fillMenuDesc(userMenus);

        // 3. 构建树形结构
        return buildMenuTree(userMenus, 0L);
    }

    @Override
    public MenuVO getMenuById(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new RuntimeException("菜单不存在不存在");
        }
        return convertToMenuVO(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMenu(MenuAddRequest request, Long operator) {
        // 1. 检查父级菜单是否存在
        if (request.getParentId() != null && request.getParentId() != 0) {
            SysMenu parentMenu = menuMapper.selectById(request.getParentId());
            if (parentMenu == null) {
                throw new RuntimeException("父级菜单不存在不存在");
            }
        }

        // 2. 构建菜单实体
        SysMenu menu = new SysMenu();
        menu.setMenuName(request.getMenuName());
        menu.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        menu.setMenuType(StringUtils.hasText(request.getMenuType()) ?
                com.zzl.platform.auth.enums.MenuType.of(request.getMenuType()) :
                com.zzl.platform.auth.enums.MenuType.MENU);
        menu.setMenuIcon(request.getMenuIcon());
        menu.setMenuPath(request.getMenuPath());
        menu.setComponent(request.getComponent());
        menu.setRedirect(request.getRedirect());
        menu.setIsCache(request.getIsCache() != null ? request.getIsCache() : 1);
        menu.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : 1);
        menu.setIsExternal(request.getIsExternal() != null ? request.getIsExternal() : 0);
        menu.setMenuSort(request.getMenuSort() != null ? request.getMenuSort() : 0);
        menu.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        menu.setRemark(request.getRemark());
        menu.setCreateBy(operator);

        // 3. 保存菜单
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editMenu(MenuEditRequest request, Long operator) {
        // 1. 查询菜单
        SysMenu menu = menuMapper.selectById(request.getId());
        if (menu == null) {
            throw new RuntimeException("菜单不存在不存在");
        }

        // 2. 检查是否将菜单设置为自己的子菜单（避免循环）
        if (request.getParentId() != null && request.getParentId().equals(request.getId())) {
            throw new RuntimeException("不能将菜单设置为自己的父级");
        }

        // 3. 检查父级菜单是否存在
        if (request.getParentId() != null && request.getParentId() != 0) {
            SysMenu parentMenu = menuMapper.selectById(request.getParentId());
            if (parentMenu == null) {
                throw new RuntimeException("父级菜单不存在不存在");
            }
        }

        // 4. 更新菜单信息
        menu.setMenuName(request.getMenuName());
        menu.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        menu.setMenuType(StringUtils.hasText(request.getMenuType()) ?
                com.zzl.platform.auth.enums.MenuType.of(request.getMenuType()) :
                com.zzl.platform.auth.enums.MenuType.MENU);
        menu.setMenuIcon(request.getMenuIcon());
        menu.setMenuPath(request.getMenuPath());
        menu.setComponent(request.getComponent());
        menu.setRedirect(request.getRedirect());
        menu.setIsCache(request.getIsCache() != null ? request.getIsCache() : 1);
        menu.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : 1);
        menu.setIsExternal(request.getIsExternal() != null ? request.getIsExternal() : 0);
        menu.setMenuSort(request.getMenuSort() != null ? request.getMenuSort() : 0);
        menu.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        menu.setRemark(request.getRemark());
        menu.setUpdateBy(operator);

        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId, Long operator) {
        // 1. 检查菜单是否存在
        if (menuMapper.selectById(menuId) == null) {
            throw new RuntimeException("菜单不存在不存在");
        }

        // 2. 检查是否有子菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, menuId);
        long childCount = menuMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw new RuntimeException("该菜单下有子菜单，无法删除");
        }

        // 3. 删除菜单
        menuMapper.deleteById(menuId);
        roleMenuMapper.deleteByMenuId(menuId);
    }

    // ==================== 私有方法 ====================

    /**
     * 为Mapper直接查询的MenuVO补充描述字段
     */
    private void fillMenuDesc(List<MenuVO> menus) {
        for (MenuVO menu : menus) {
            if (menu.getMenuType() != null) {
                menu.setMenuTypeDesc(menu.getMenuType().getDesc());
            }
            if (menu.getStatus() != null) {
                menu.setStatusDesc(menu.getStatus() == 1 ? "正常" : "禁用");
            }
        }
    }

    /**
     * 构建菜单树
     *
     * @param menus    所有菜单列表
     * @param parentId 父级菜单ID
     * @return 菜单树
     */
    private List<MenuVO> buildMenuTree(List<MenuVO> menus, Long parentId) {
        List<MenuVO> tree = new ArrayList<>();

        for (MenuVO menu : menus) {
            if (menu.getParentId() != null && menu.getParentId().equals(parentId)) {
                // 查找子菜单
                List<MenuVO> children = buildMenuTree(menus, menu.getId());
                menu.setChildren(children);
                tree.add(menu);
            }
        }

        // 按排序字段排序
        tree.sort((a, b) -> {
            Integer sortA = a.getMenuSort() != null ? a.getMenuSort() : 0;
            Integer sortB = b.getMenuSort() != null ? b.getMenuSort() : 0;
            return sortA.compareTo(sortB);
        });

        return tree;
    }

    /**
     * 转换为MenuVO
     */
    private MenuVO convertToMenuVO(SysMenu menu) {
        MenuVO menuVO = new MenuVO();
        menuVO.setId(menu.getId());
        menuVO.setMenuName(menu.getMenuName());
        menuVO.setParentId(menu.getParentId());
        menuVO.setMenuType(menu.getMenuType());
        menuVO.setMenuTypeDesc(menu.getMenuType() != null ? menu.getMenuType().getDesc() : null);
        menuVO.setMenuIcon(menu.getMenuIcon());
        menuVO.setMenuPath(menu.getMenuPath());
        menuVO.setComponent(menu.getComponent());
        menuVO.setRedirect(menu.getRedirect());
        menuVO.setIsCache(menu.getIsCache());
        menuVO.setIsVisible(menu.getIsVisible());
        menuVO.setIsExternal(menu.getIsExternal());
        menuVO.setMenuSort(menu.getMenuSort());
        menuVO.setStatus(menu.getStatus());
        menuVO.setStatusDesc(menu.getStatus() != null ? (menu.getStatus() == 1 ? "正常" : "禁用") : null);
        menuVO.setRemark(menu.getRemark());
        menuVO.setCreateTime(menu.getCreateTime());
        menuVO.setUpdateTime(menu.getUpdateTime());

        // 查询权限编码
        List<String> permissionCodes = menuMapper.selectPermissionCodesByMenuId(menu.getId());
        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            menuVO.setPermissionCode(String.join(",", permissionCodes));
        }

        return menuVO;
    }
}
