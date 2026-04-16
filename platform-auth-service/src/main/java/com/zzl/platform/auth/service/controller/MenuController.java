package com.zzl.platform.auth.service.controller;

import com.zzl.platform.auth.aspect.RequiresPermission;
import com.zzl.platform.auth.dto.MenuAddRequest;
import com.zzl.platform.auth.dto.MenuEditRequest;
import com.zzl.platform.auth.service.MenuService;
import com.zzl.platform.auth.vo.MenuVO;
import com.zzl.platform.auth.vo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 查询所有菜单树
     */
    @GetMapping("/tree/all")
    @RequiresPermission(value = "system:menu:list", desc = "菜单查询")
    public ResponseResult<List<MenuVO>> treeAllMenus() {
        try {
            List<MenuVO> menus = menuService.treeAllMenus();
            return ResponseResult.success(menus);
        } catch (Exception e) {
            log.error("Tree all menus error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 查询当前用户可见菜单树
     */
    @GetMapping("/tree")
    @RequiresPermission(value = "system:menu:list", desc = "菜单查询")
    public ResponseResult<List<MenuVO>> treeUserMenus(@RequestHeader("X-User-Id") Long userId) {
        try {
            List<MenuVO> menus = menuService.treeUserMenus(userId);
            return ResponseResult.success(menus);
        } catch (Exception e) {
            log.error("Tree user menus error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 根据ID查询菜单详情
     */
    @GetMapping("/{id}")
    @RequiresPermission(value = "system:menu:get", desc = "菜单详情")
    public ResponseResult<MenuVO> getMenuById(@PathVariable Long id) {
        try {
            MenuVO menuVO = menuService.getMenuById(id);
            return ResponseResult.success(menuVO);
        } catch (Exception e) {
            log.error("Get menu by id error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 新增菜单
     */
    @PostMapping("/add")
    @RequiresPermission(value = "system:menu:add", desc = "菜单新增")
    public ResponseResult<Long> addMenu(@RequestBody MenuAddRequest request,
                                        @RequestHeader("X-User-Id") Long operatorId) {
        try {
            Long menuId = menuService.addMenu(request, operatorId);
            return ResponseResult.success("菜单新增成功", menuId);
        } catch (Exception e) {
            log.error("Add menu error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 编辑菜单
     */
    @PutMapping("/edit")
    @RequiresPermission(value = "system:menu:edit", desc = "菜单编辑")
    public ResponseResult<Void> editMenu(@RequestBody MenuEditRequest request,
                                         @RequestHeader("X-User-Id") Long operatorId) {
        try {
            menuService.editMenu(request, operatorId);
            return ResponseResult.success("菜单编辑成功", null);
        } catch (Exception e) {
            log.error("Edit menu error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(value = "system:menu:delete", desc = "菜单删除")
    public ResponseResult<Void> deleteMenu(@PathVariable Long id,
                                           @RequestHeader("X-User-Id") Long operatorId) {
        try {
            menuService.deleteMenu(id, operatorId);
            return ResponseResult.success("菜单删除成功", null);
        } catch (Exception e) {
            log.error("Delete menu error", e);
            return ResponseResult.error(e.getMessage());
        }
    }
}
