package com.zzl.platform.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzl.platform.auth.dto.DepartmentAddRequest;
import com.zzl.platform.auth.dto.DepartmentEditRequest;
import com.zzl.platform.auth.entity.SysDepartment;
import com.zzl.platform.auth.mapper.SysDepartmentMapper;
import com.zzl.platform.auth.service.DepartmentService;
import com.zzl.platform.auth.vo.DepartmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门服务实现
 */
@Slf4j
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final SysDepartmentMapper departmentMapper;

    public DepartmentServiceImpl(SysDepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<DepartmentVO> treeAllDepartments() {
        // 1. 查询所有部门
        List<DepartmentVO> allDepts = departmentMapper.selectAllDepartments();

        // 2. 构建树形结构
        return buildDeptTree(allDepts, 0L);
    }

    @Override
    public DepartmentVO getDepartmentById(Long deptId) {
        SysDepartment department = departmentMapper.selectById(deptId);
        if (department == null) {
            throw new RuntimeException("部门不存在不存在");
        }
        return convertToDepartmentVO(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDepartment(DepartmentAddRequest request, Long operator) {
        // 1. 检查部门编码是否存在
        SysDepartment existDept = departmentMapper.selectByDeptCode(request.getDeptCode());
        if (existDept != null) {
            throw new RuntimeException("部门编码已存在");
        }

        // 2. 检查父级部门是否存在
        Integer deptLevel = 1;
        String deptPath = "/" + request.getDeptCode();
        if (request.getParentId() != null && request.getParentId() != 0) {
            SysDepartment parentDept = departmentMapper.selectById(request.getParentId());
            if (parentDept == null) {
                throw new RuntimeException("父级部门不存在不存在");
            }
            deptLevel = parentDept.getDeptLevel() + 1;
            deptPath = parentDept.getDeptPath() + "/" + request.getDeptCode();
        }

        // 3. 构建部门实体
        SysDepartment department = new SysDepartment();
        department.setDeptCode(request.getDeptCode());
        department.setDeptName(request.getDeptName());
        department.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        department.setDeptLevel(deptLevel);
        department.setDeptPath(deptPath);
        department.setDeptSort(request.getDeptSort() != null ? request.getDeptSort() : 0);
        department.setLeader(request.getLeader());
        department.setLeaderPhone(request.getLeaderPhone());
        department.setEmail(request.getEmail());
        department.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        department.setRemark(request.getRemark());
        department.setCreateBy(operator);

        // 4. 保存部门
        departmentMapper.insert(department);
        return department.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editDepartment(DepartmentEditRequest request, Long operator) {
        // 1. 查询部门
        SysDepartment department = departmentMapper.selectById(request.getId());
        if (department == null) {
            throw new RuntimeException("部门不存在不存在");
        }

        // 2. 检查是否将部门设置为自己的子部门（避免循环）
        if (request.getParentId() != null && request.getParentId().equals(request.getId())) {
            throw new RuntimeException("不能将部门设置为自己的父级");
        }

        // 3. 检查部门编码是否存在（排除自己）
        SysDepartment existDept = departmentMapper.selectByDeptCode(request.getDeptCode());
        if (existDept != null && !existDept.getId().equals(request.getId())) {
            throw new RuntimeException("部门编码已存在");
        }

        // 4. 检查父级部门是否存在
        Integer deptLevel = department.getDeptLevel();
        String deptPath = department.getDeptPath();
        if (request.getParentId() != null && request.getParentId() != 0) {
            SysDepartment parentDept = departmentMapper.selectById(request.getParentId());
            if (parentDept == null) {
                throw new RuntimeException("父级部门不存在不存在");
            }
            deptLevel = parentDept.getDeptLevel() + 1;
            deptPath = parentDept.getDeptPath() + "/" + request.getDeptCode();
        } else {
            deptPath = "/" + request.getDeptCode();
        }

        // 5. 更新部门信息
        department.setDeptCode(request.getDeptCode());
        department.setDeptName(request.getDeptName());
        department.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        department.setDeptLevel(deptLevel);
        department.setDeptPath(deptPath);
        department.setDeptSort(request.getDeptSort() != null ? request.getDeptSort() : 0);
        department.setLeader(request.getLeader());
        department.setLeaderPhone(request.getLeaderPhone());
        department.setEmail(request.getEmail());
        department.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        department.setRemark(request.getRemark());
        department.setUpdateBy(operator);

        departmentMapper.updateById(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long deptId, Long operator) {
        // 1. 检查部门是否存在
        if (departmentMapper.selectById(deptId) == null) {
            throw new RuntimeException("部门不存在不存在");
        }

        // 2. 检查是否有子部门
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getParentId, deptId);
        long childCount = departmentMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw new RuntimeException("该部门下有子部门，无法删除");
        }

        // 3. 检查是否有用户关联
        long userCount = departmentMapper.selectUserCountByDeptId(deptId);
        if (userCount > 0) {
            throw new RuntimeException("该部门下有用户，无法删除");
        }

        // 4. 删除部门
        departmentMapper.deleteById(deptId);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建部门树
     *
     * @param depts    所有部门列表
     * @param parentId 父级部门ID
     * @return 部门树
     */
    private List<DepartmentVO> buildDeptTree(List<DepartmentVO> depts, Long parentId) {
        List<DepartmentVO> tree = new ArrayList<>();

        for (DepartmentVO dept : depts) {
            if (dept.getParentId() != null && dept.getParentId().equals(parentId)) {
                // 查找子部门
                List<DepartmentVO> children = buildDeptTree(depts, dept.getId());
                dept.setChildren(children);
                tree.add(dept);
            }
        }

        // 按排序字段排序
        tree.sort((a, b) -> {
            Integer sortA = a.getDeptSort() != null ? a.getDeptSort() : 0;
            Integer sortB = b.getDeptSort() != null ? b.getDeptSort() : 0;
            return sortA.compareTo(sortB);
        });

        return tree;
    }

    /**
     * 转换为DepartmentVO
     */
    private DepartmentVO convertToDepartmentVO(SysDepartment department) {
        DepartmentVO deptVO = new DepartmentVO();
        deptVO.setId(department.getId());
        deptVO.setDeptCode(department.getDeptCode());
        deptVO.setDeptName(department.getDeptName());
        deptVO.setParentId(department.getParentId());
        deptVO.setDeptLevel(department.getDeptLevel());
        deptVO.setDeptPath(department.getDeptPath());
        deptVO.setDeptSort(department.getDeptSort());
        deptVO.setLeader(department.getLeader());
        deptVO.setLeaderPhone(department.getLeaderPhone());
        deptVO.setEmail(department.getEmail());
        deptVO.setStatus(department.getStatus());
        deptVO.setStatusDesc(department.getStatus() != null ? (department.getStatus() == 1 ? "正常" : "禁用") : null);
        deptVO.setRemark(department.getRemark());
        deptVO.setCreateTime(department.getCreateTime());
        deptVO.setUpdateTime(department.getUpdateTime());

        // 查询用户数量
        long userCount = departmentMapper.selectUserCountByDeptId(department.getId());
        deptVO.setUserCount(userCount);

        return deptVO;
    }
}
