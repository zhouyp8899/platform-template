package com.zzl.platform.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.platform.auth.dto.GrantPermissionsRequest;
import com.zzl.platform.auth.dto.RoleAddRequest;
import com.zzl.platform.auth.dto.RoleEditRequest;
import com.zzl.platform.auth.entity.SysRole;
import com.zzl.platform.auth.entity.SysRolePermission;
import com.zzl.platform.auth.mapper.SysRoleMapper;
import com.zzl.platform.auth.mapper.SysRolePermissionMapper;
import com.zzl.platform.auth.service.RoleService;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.RoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public RoleServiceImpl(SysRoleMapper roleMapper,
                           SysRolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public PageResponse<RoleVO> pageRole(Integer pageNum, Integer pageSize, String keyword) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword));
        }
        wrapper.orderByAsc(SysRole::getSort).orderByDesc(SysRole::getCreateTime);

        IPage<SysRole> rolePage = roleMapper.selectPage(page, wrapper);

        List<RoleVO> roleVOList = rolePage.getRecords().stream()
                .map(this::convertToRoleVO)
                .collect(Collectors.toList());

        return new PageResponse<>(rolePage.getTotal(), roleVOList, pageNum, pageSize);
    }

    @Override
    public List<RoleVO> listAllRoles() {
        return roleMapper.selectAllRoles();
    }

    @Override
    public RoleVO getRoleById(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在不存在");
        }
        return convertToRoleVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRole(RoleAddRequest request, Long operator) {
        // 检查角色编码是否存在
        SysRole existRole = roleMapper.selectByRoleCode(request.getRoleCode());
        if (existRole != null) {
            throw new RuntimeException("角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setRoleType(StringUtils.hasText(request.getRoleType()) ?
                com.zzl.platform.auth.enums.RoleType.of(request.getRoleType()) :
                com.zzl.platform.auth.enums.RoleType.BUSINESS);
        role.setDataScope(StringUtils.hasText(request.getDataScope()) ?
                com.zzl.platform.auth.enums.DataScopeType.of(request.getDataScope()) :
                com.zzl.platform.auth.enums.DataScopeType.CUSTOM);
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        role.setSort(request.getSort() != null ? request.getSort() : 0);
        role.setCreateBy(operator);

        roleMapper.insert(role);

        // 分配权限
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            grantPermissionsToRole(role.getId(), request.getPermissionIds(), operator);
        }

        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editRole(RoleEditRequest request, Long operator) {
        SysRole role = roleMapper.selectById(request.getId());
        if (role == null) {
            throw new RuntimeException("角色不存在不存在");
        }

        // 检查是否为系统内置角色
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new RuntimeException("系统内置角色不能修改");
        }

        // 检查角色编码是否存在（排除自己）
        SysRole existRole = roleMapper.selectByRoleCode(request.getRoleCode());
        if (existRole != null && !existRole.getId().equals(request.getId())) {
            throw new RuntimeException("角色编码已存在");
        }

        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setRoleType(StringUtils.hasText(request.getRoleType()) ?
                com.zzl.platform.auth.enums.RoleType.of(request.getRoleType()) :
                com.zzl.platform.auth.enums.RoleType.BUSINESS);
        role.setDataScope(StringUtils.hasText(request.getDataScope()) ?
                com.zzl.platform.auth.enums.DataScopeType.of(request.getDataScope()) :
                com.zzl.platform.auth.enums.DataScopeType.CUSTOM);
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        role.setSort(request.getSort() != null ? request.getSort() : 0);
        role.setUpdateBy(operator);

        roleMapper.updateById(role);

        // 重新分配权限
        if (request.getPermissionIds() != null) {
            grantPermissionsToRole(role.getId(), request.getPermissionIds(), operator);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId, Long operator) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在不存在");
        }

        // 检查是否为系统内置角色
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new RuntimeException("系统内置角色不能删除");
        }

        // 检查是否有用户使用该角色
        long userCount = roleMapper.selectUserCountByRoleId(roleId);
        if (userCount > 0) {
            throw new RuntimeException("该角色下有用户，无法删除");
        }

        roleMapper.deleteById(roleId);
        rolePermissionMapper.deleteByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPermissions(GrantPermissionsRequest request, Long operator) {
        grantPermissionsToRole(request.getRoleId(), request.getPermissionIds(), operator);
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public SysRole getRoleByRoleCode(String roleCode) {
        return roleMapper.selectByRoleCode(roleCode);
    }

    // ==================== 私有方法 ====================

    /**
     * 为角色分配权限
     */
    private void grantPermissionsToRole(Long roleId, List<Long> permissionIds, Long operator) {
        // 删除原有权限
        rolePermissionMapper.deleteByRoleId(roleId);

        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> rolePermissions = new ArrayList<>();
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreateTime(LocalDateTime.now());
                rolePermission.setCreateBy(operator);
                rolePermissions.add(rolePermission);
            }
            rolePermissionMapper.batchInsert(rolePermissions);
        }
    }

    /**
     * 转换为RoleVO
     */
    private RoleVO convertToRoleVO(SysRole role) {
        RoleVO roleVO = new RoleVO();
        roleVO.setId(role.getId());
        roleVO.setRoleCode(role.getRoleCode());
        roleVO.setRoleName(role.getRoleName());
        roleVO.setRoleType(role.getRoleType());
        roleVO.setDataScope(role.getDataScope());
        roleVO.setDescription(role.getDescription());
        roleVO.setIsSystem(role.getIsSystem());
        roleVO.setStatus(role.getStatus());
        roleVO.setSort(role.getSort());
        roleVO.setCreateTime(role.getCreateTime());
        roleVO.setUpdateTime(role.getUpdateTime());

        // 查询权限信息
        List<Long> permissionIds = roleMapper.selectPermissionIdsByRoleId(role.getId());
        roleVO.setPermissionIds(permissionIds);

        List<String> permissionCodes = roleMapper.selectPermissionCodesByRoleId(role.getId());
        roleVO.setPermissionCodes(permissionCodes);

        // 查询用户数量
        long userCount = roleMapper.selectUserCountByRoleId(role.getId());
        roleVO.setUserCount(userCount);

        return roleVO;
    }
}
