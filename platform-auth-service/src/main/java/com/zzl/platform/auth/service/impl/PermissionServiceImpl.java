package com.zzl.platform.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.platform.auth.dto.PermissionAddRequest;
import com.zzl.platform.auth.dto.PermissionEditRequest;
import com.zzl.platform.auth.entity.SysPermission;
import com.zzl.platform.auth.mapper.SysPermissionMapper;
import com.zzl.platform.auth.service.PermissionService;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.PermissionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper permissionMapper;

    public PermissionServiceImpl(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public PageResponse<PermissionVO> pagePermission(Integer pageNum, Integer pageSize, String keyword) {
        Page<SysPermission> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysPermission::getPermissionCode, keyword)
                    .or().like(SysPermission::getPermissionName, keyword)
                    .or().like(SysPermission::getPermissionGroup, keyword));
        }
        wrapper.orderByAsc(SysPermission::getSort).orderByDesc(SysPermission::getCreateTime);

        IPage<SysPermission> permissionPage = permissionMapper.selectPage(page, wrapper);

        List<PermissionVO> permissionVOList = permissionPage.getRecords().stream()
                .map(this::convertToPermissionVO)
                .collect(Collectors.toList());

        return new PageResponse<>(permissionPage.getTotal(), permissionVOList, pageNum, pageSize);
    }

    @Override
    public List<PermissionVO> listAllPermissions() {
        return permissionMapper.selectAllPermissions();
    }

    @Override
    public List<String> listPermissionGroups() {
        return permissionMapper.selectPermissionGroups();
    }

    @Override
    public PermissionVO getPermissionById(Long permissionId) {
        SysPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new RuntimeException("权限不存在不存在");
        }
        return convertToPermissionVO(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addPermission(PermissionAddRequest request, Long operator) {
        // 检查权限编码是否存在
        SysPermission existPermission = permissionMapper.selectByPermissionCode(request.getPermissionCode());
        if (existPermission != null) {
            throw new RuntimeException("权限编码已存在");
        }

        SysPermission permission = new SysPermission();
        permission.setPermissionCode(request.getPermissionCode());
        permission.setPermissionName(request.getPermissionName());
        permission.setResourceType(StringUtils.hasText(request.getResourceType()) ?
                com.zzl.platform.auth.enums.ResourceType.of(request.getResourceType()) :
                com.zzl.platform.auth.enums.ResourceType.API);
        permission.setResourcePath(request.getResourcePath());
        permission.setHttpMethod(request.getHttpMethod());
        permission.setMenuId(request.getMenuId());
        permission.setPermissionGroup(request.getPermissionGroup());
        permission.setDescription(request.getDescription());
        permission.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        permission.setSort(request.getSort() != null ? request.getSort() : 0);
        permission.setCreateBy(operator);

        permissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPermission(PermissionEditRequest request, Long operator) {
        SysPermission permission = permissionMapper.selectById(request.getId());
        if (permission == null) {
            throw new RuntimeException("权限不存在不存在");
        }

        // 检查权限编码是否存在（排除自己）
        SysPermission existPermission = permissionMapper.selectByPermissionCode(request.getPermissionCode());
        if (existPermission != null && !existPermission.getId().equals(request.getId())) {
            throw new RuntimeException("权限编码已存在");
        }

        permission.setPermissionCode(request.getPermissionCode());
        permission.setPermissionName(request.getPermissionName());
        permission.setResourceType(StringUtils.hasText(request.getResourceType()) ?
                com.zzl.platform.auth.enums.ResourceType.of(request.getResourceType()) :
                com.zzl.platform.auth.enums.ResourceType.API);
        permission.setResourcePath(request.getResourcePath());
        permission.setHttpMethod(request.getHttpMethod());
        permission.setMenuId(request.getMenuId());
        permission.setPermissionGroup(request.getPermissionGroup());
        permission.setDescription(request.getDescription());
        permission.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        permission.setSort(request.getSort() != null ? request.getSort() : 0);
        permission.setUpdateBy(operator);

        permissionMapper.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long permissionId, Long operator) {
        SysPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new RuntimeException("权限不存在不存在");
        }

        permissionMapper.deleteById(permissionId);
    }

    @Override
    public SysPermission getPermissionByPermissionCode(String permissionCode) {
        return permissionMapper.selectByPermissionCode(permissionCode);
    }

    /**
     * 转换为PermissionVO
     */
    private PermissionVO convertToPermissionVO(SysPermission permission) {
        PermissionVO permissionVO = new PermissionVO();
        permissionVO.setId(permission.getId());
        permissionVO.setPermissionCode(permission.getPermissionCode());
        permissionVO.setPermissionName(permission.getPermissionName());
        permissionVO.setResourceType(permission.getResourceType());
        permissionVO.setResourcePath(permission.getResourcePath());
        permissionVO.setHttpMethod(permission.getHttpMethod());
        permissionVO.setMenuId(permission.getMenuId());
        permissionVO.setPermissionGroup(permission.getPermissionGroup());
        permissionVO.setDescription(permission.getDescription());
        permissionVO.setStatus(permission.getStatus());
        permissionVO.setSort(permission.getSort());
        permissionVO.setCreateTime(permission.getCreateTime());
        permissionVO.setUpdateTime(permission.getUpdateTime());

        return permissionVO;
    }
}
