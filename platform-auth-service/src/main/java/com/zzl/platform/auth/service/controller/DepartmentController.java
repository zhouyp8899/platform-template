package com.zzl.platform.auth.service.controller;

import com.zzl.platform.auth.aspect.RequiresPermission;
import com.zzl.platform.auth.dto.DepartmentAddRequest;
import com.zzl.platform.auth.dto.DepartmentEditRequest;
import com.zzl.platform.auth.service.DepartmentService;
import com.zzl.platform.auth.vo.DepartmentVO;
import com.zzl.platform.auth.vo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system/dept")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 查询部门树
     */
    @GetMapping("/tree")
    @RequiresPermission(value = "system:dept:list", desc = "部门查询")
    public ResponseResult<List<DepartmentVO>> getDepartmentTree() {
        try {
            List<DepartmentVO> tree = departmentService.treeAllDepartments();
            return ResponseResult.success(tree);
        } catch (Exception e) {
            log.error("Get department tree error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 根据ID查询部门详情
     */
    @GetMapping("/{id}")
    @RequiresPermission(value = "system:dept:get", desc = "部门详情")
    public ResponseResult<DepartmentVO> getDepartmentById(@PathVariable Long id) {
        try {
            DepartmentVO department = departmentService.getDepartmentById(id);
            return ResponseResult.success(department);
        } catch (Exception e) {
            log.error("Get department by id error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 新增部门
     */
    @PostMapping("/add")
    @RequiresPermission(value = "system:dept:add", desc = "部门新增")
    public ResponseResult<Long> addDepartment(@RequestBody DepartmentAddRequest request,
                                              @RequestHeader("X-User-Id") Long operatorId) {
        try {
            Long deptId = departmentService.addDepartment(request, operatorId);
            return ResponseResult.success("部门新增成功", deptId);
        } catch (Exception e) {
            log.error("Add department error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 编辑部门
     */
    @PutMapping("/edit")
    @RequiresPermission(value = "system:dept:edit", desc = "部门编辑")
    public ResponseResult<Void> editDepartment(@RequestBody DepartmentEditRequest request,
                                               @RequestHeader("X-User-Id") Long operatorId) {
        try {
            departmentService.editDepartment(request, operatorId);
            return ResponseResult.success("部门编辑成功", null);
        } catch (Exception e) {
            log.error("Edit department error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(value = "system:dept:delete", desc = "部门删除")
    public ResponseResult<Void> deleteDepartment(@PathVariable Long id,
                                                 @RequestHeader("X-User-Id") Long operatorId) {
        try {
            departmentService.deleteDepartment(id, operatorId);
            return ResponseResult.success("部门删除成功", null);
        } catch (Exception e) {
            log.error("Delete department error", e);
            return ResponseResult.error(e.getMessage());
        }
    }
}
