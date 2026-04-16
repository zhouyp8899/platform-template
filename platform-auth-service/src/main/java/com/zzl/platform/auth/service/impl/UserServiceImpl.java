package com.zzl.platform.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.platform.auth.constants.AuthConstants;
import com.zzl.platform.auth.dto.*;
import com.zzl.platform.auth.entity.SysOnlineUser;
import com.zzl.platform.auth.entity.SysUser;
import com.zzl.platform.auth.entity.SysUserRole;
import com.zzl.platform.auth.enums.UserStatus;
import com.zzl.platform.auth.enums.UserType;
import com.zzl.platform.auth.mapper.SysUserMapper;
import com.zzl.platform.auth.mapper.SysUserRoleMapper;
import com.zzl.platform.auth.service.JwtService;
import com.zzl.platform.auth.service.OnlineUserService;
import com.zzl.platform.auth.service.UserService;
import com.zzl.platform.auth.vo.LoginResponse;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JwtService jwtService;
    private final OnlineUserService onlineUserService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(SysUserMapper userMapper,
                           SysUserRoleMapper userRoleMapper,
                           JwtService jwtService,
                           OnlineUserService onlineUserService,
                           RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.jwtService = jwtService;
        this.onlineUserService = onlineUserService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse adminLogin(LoginRequest request) {
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!UserType.ADMIN.equals(user.getUserType())) {
            throw new RuntimeException("用户类型错误");
        }

        if (UserStatus.LOCKED.equals(user.getStatus())) {
            throw new RuntimeException("账户已被锁定");
        }
        if (UserStatus.DISABLED.equals(user.getStatus())) {
            throw new RuntimeException("账户已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleLoginFail(user);
            throw new RuntimeException("用户名或密码错误");
        }

        clearLoginFail(user.getUsername());
        updateLoginInfo(user);

        Map<String, Object> extraClaims = new HashMap<>();
        List<String> roles = getUserRolesCodes(user.getId());
        Set<String> permissions = getUserPermissions(user.getId());
        extraClaims.put("roles", roles);
        extraClaims.put("permissions", permissions);

        String accessToken = jwtService.generateToken(
                user.getId(), user.getUsername(),
                AuthConstants.TOKEN_TYPE_ADMIN, extraClaims);

        String refreshToken = jwtService.generateRefreshToken(
                user.getId(), user.getUsername(),
                AuthConstants.TOKEN_TYPE_ADMIN);

        saveTokenToCache(user.getId(), accessToken, AuthConstants.TOKEN_TYPE_ADMIN);
        addOnlineUser(user, accessToken);

        return buildLoginResponse(user, accessToken, refreshToken, roles, permissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse h5PhoneLogin(PhoneLoginRequest request) {
        SysUser user = userMapper.selectByPhone(request.getPhone());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!UserType.H5.equals(user.getUserType())) {
            throw new RuntimeException("用户类型错误");
        }

        if (UserStatus.LOCKED.equals(user.getStatus())) {
            throw new RuntimeException("账户已被锁定");
        }

        if (!validateCode(request.getPhone(), request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        clearCode(request.getPhone());
        updateLoginInfo(user);

        Map<String, Object> extraClaims = new HashMap<>();
        List<String> roles = getUserRolesCodes(user.getId());
        Set<String> permissions = getUserPermissions(user.getId());
        extraClaims.put("roles", roles);
        extraClaims.put("permissions", permissions);

        String accessToken = jwtService.generateToken(
                user.getId(), user.getUsername(),
                AuthConstants.TOKEN_TYPE_H5, extraClaims);

        String refreshToken = jwtService.generateRefreshToken(
                user.getId(), user.getUsername(),
                AuthConstants.TOKEN_TYPE_H5);

        saveTokenToCache(user.getId(), accessToken, AuthConstants.TOKEN_TYPE_H5);
        addOnlineUser(user, accessToken);

        return buildLoginResponse(user, accessToken, refreshToken, roles, permissions);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken, String tokenType) {
        if (!jwtService.validateToken(refreshToken, tokenType)) {
            throw new RuntimeException("刷新Token无效或已过期");
        }

        Long userId = jwtService.getUserIdFromToken(refreshToken, tokenType);
        String username = jwtService.getUsernameFromToken(refreshToken, tokenType);

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> extraClaims = new HashMap<>();
        List<String> roles = getUserRolesCodes(user.getId());
        Set<String> permissions = getUserPermissions(user.getId());
        extraClaims.put("roles", roles);
        extraClaims.put("permissions", permissions);

        String accessToken = jwtService.generateToken(userId, username, tokenType, extraClaims);
        saveTokenToCache(userId, accessToken, tokenType);

        return buildLoginResponse(user, accessToken, refreshToken, roles, permissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long userId, String token, String tokenType) {
        String tokenKey = getTokenKey(userId, token, tokenType);
        redisTemplate.delete(tokenKey);
        onlineUserService.kickOutUserAll(userId);
        clearUserPermissionCache(userId);
        log.info("User logout: userId={}, tokenType={}", userId, tokenType);
    }

    @Override
    public void sendCode(SendCodeRequest request) {
        String phone = request.getPhone();
        String code = String.format("%06d", new Random().nextInt(1000000));

        Map<String, Object> codeData = new HashMap<>();
        codeData.put("code", code);
        codeData.put("useCount", 0);
        codeData.put("expireTime", System.currentTimeMillis() + AuthConstants.CODE_EXPIRE * 1000);

        String codeKey = AuthConstants.REDIS_CODE_KEY + phone;
        redisTemplate.opsForValue().set(codeKey, codeData, AuthConstants.CODE_EXPIRE, TimeUnit.SECONDS);

        log.info("Send code to phone: {}, code: {}", phone, code);
        if ("13800138000".equals(phone)) {
            log.warn("测试环境验证码: {}", code);
        }
    }

    @Override
    public PageResponse<UserVO> pageUser(PageRequest<UserQueryRequest> request) {
        Page<SysUser> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        UserQueryRequest param = request.getParam();
        if (param != null) {
            wrapper.like(StringUtils.hasText(param.getUsername()), SysUser::getUsername, param.getUsername())
                    .like(StringUtils.hasText(param.getRealName()), SysUser::getRealName, param.getRealName())
                    .like(StringUtils.hasText(param.getPhone()), SysUser::getPhone, param.getPhone())
                    .eq(param.getDeptId() != null, SysUser::getDeptId, param.getDeptId())
                    .eq(param.getStatus() != null, SysUser::getStatus, UserStatus.of(param.getStatus()))
                    .eq(StringUtils.hasText(param.getUserType()), SysUser::getUserType, UserType.of(param.getUserType()));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> userPage = userMapper.selectPage(page, wrapper);

        List<UserVO> userVOList = new ArrayList<>();
        for (SysUser user : userPage.getRecords()) {
            UserVO userVO = convertToUserVO(user);
            userVOList.add(userVO);
        }

        return new PageResponse<>(userPage.getTotal(), userVOList,
                request.getPageNum(), request.getPageSize());
    }

    @Override
    public UserVO getUserById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUser(UserAddRequest request, Long operator) {
        if (checkUsernameExists(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (request.getPhone() != null && checkPhoneExists(request.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setDeptId(request.getDeptId());
        user.setUserType(UserType.ADMIN);
        user.setStatus(UserStatus.NORMAL);
        user.setRemark(request.getRemark());
        user.setCreateBy(operator);

        userMapper.insert(user);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            grantRolesToUser(user.getId(), request.getRoleIds(), operator);
        }

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editUser(UserEditRequest request, Long operator) {
        SysUser user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在不存在");
        }

        if (!user.getUsername().equals(request.getUsername()) &&
                checkUsernameExists(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setDeptId(request.getDeptId());
        user.setRemark(request.getRemark());
        user.setUpdateBy(operator);

        userMapper.updateById(user);

        if (request.getRoleIds() != null) {
            grantRolesToUser(user.getId(), request.getRoleIds(), operator);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId, Long operator) {
        if (userMapper.countById(userId) == 0) {
            throw new RuntimeException("用户不存在不存在");
        }

        userMapper.deleteById(userId);
        userRoleMapper.deleteByUserId(userId);
        onlineUserService.kickOutUserAll(userId);
        clearUserPermissionCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds, Long operator) {
        for (Long userId : userIds) {
            deleteUser(userId, operator);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, Long operator) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在不存在");
        }

        user.setPassword(AuthConstants.DEFAULT_PASSWORD);
        user.setUpdateBy(operator);
        userMapper.updateById(user);
        onlineUserService.kickOutUserAll(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUserStatus(Long userId, Integer status, Long operator) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在不存在");
        }

        user.setStatus(UserStatus.of(status));
        user.setUpdateBy(operator);
        userMapper.updateById(user);

        if (UserStatus.DISABLED.equals(user.getStatus()) || UserStatus.LOCKED.equals(user.getStatus())) {
            onlineUserService.kickOutUserAll(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantRoles(GrantRolesRequest request, Long operator) {
        grantRolesToUser(request.getUserId(), request.getRoleIds(), operator);
        clearUserPermissionCache(request.getUserId());
    }

    @Override
    public List<Long> getUserRoles(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        return getUserById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeCurrentUserPassword(Long userId, ChangePasswordRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        onlineUserService.kickOutUserAll(userId);
    }

    @Override
    public void editCurrentUser(Long userId, UserEditRequest request, Long operator) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在不存在");
        }

        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUpdateBy(operator);
        userMapper.updateById(user);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public SysUser getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        String cacheKey = AuthConstants.REDIS_PERMISSION_KEY + userId;
        Set<String> cachedPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }

        List<String> permissionCodes = userMapper.selectPermissionCodesByUserId(userId);
        Set<String> permissions = new HashSet<>(permissionCodes);
        redisTemplate.opsForValue().set(cacheKey, permissions, AuthConstants.CACHE_PERMISSION_EXPIRE, TimeUnit.SECONDS);

        return permissions;
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (hasRole(userId, AuthConstants.SUPER_ADMIN_ROLE)) {
            return true;
        }

        Set<String> permissions = getUserPermissions(userId);
        return permissions.contains(permissionCode);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        return roles.contains(roleCode);
    }

    // ==================== 私有方法 ====================

    private void handleLoginFail(SysUser user) {
        String failKey = AuthConstants.REDIS_FAIL_COUNT_KEY + user.getUsername();
        Integer failCount = (Integer) redisTemplate.opsForValue().get(failKey);
        failCount = failCount == null ? 1 : failCount + 1;

        redisTemplate.opsForValue().set(failKey, failCount, AuthConstants.LOGIN_FAIL_LOCK_TIME, TimeUnit.SECONDS);

        user.setLoginFailCount(failCount);
        if (failCount >= AuthConstants.MAX_LOGIN_FAIL_COUNT) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockTime(LocalDateTime.now());
        }
        userMapper.updateById(user);

        if (UserStatus.LOCKED.equals(user.getStatus())) {
            throw new RuntimeException("登录失败次数过多，账户已被锁定");
        }
    }

    private void clearLoginFail(String username) {
        String failKey = AuthConstants.REDIS_FAIL_COUNT_KEY + username;
        redisTemplate.delete(failKey);
    }

    private boolean validateCode(String phone, String code) {
        String codeKey = AuthConstants.REDIS_CODE_KEY + phone;
        Map<String, Object> codeData = (Map<String, Object>) redisTemplate.opsForValue().get(codeKey);

        if (codeData == null) {
            return false;
        }

        String savedCode = (String) codeData.get("code");
        Integer useCount = (Integer) codeData.get("useCount");
        Long expireTime = (Long) codeData.get("expireTime");

        if (expireTime < System.currentTimeMillis()) {
            return false;
        }

        if (useCount >= 3) {
            return false;
        }

        if (!code.equals(savedCode)) {
            return false;
        }

        codeData.put("useCount", useCount + 1);
        redisTemplate.opsForValue().set(codeKey, codeData, AuthConstants.CODE_EXPIRE, TimeUnit.SECONDS);
        return true;
    }

    private void clearCode(String phone) {
        String codeKey = AuthConstants.REDIS_CODE_KEY + phone;
        redisTemplate.delete(codeKey);
    }

    private void updateLoginInfo(SysUser user) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);
    }

    private void saveTokenToCache(Long userId, String token, String tokenType) {
        String tokenKey = getTokenKey(userId, token, tokenType);
        redisTemplate.opsForValue().set(tokenKey, userId, jwtService.getTokenExpireTime(tokenType), TimeUnit.SECONDS);
    }

    private String getTokenKey(Long userId, String token, String tokenType) {
        String[] parts = token.split("\\.");
        String tokenSignature = parts.length >= 3 ? parts[2] : token;
        return AuthConstants.REDIS_TOKEN_KEY + tokenType + ":" + tokenSignature;
    }

    private void addOnlineUser(SysUser user, String token) {
        SysOnlineUser onlineUser = new SysOnlineUser();
        onlineUser.setUserId(user.getId());
        onlineUser.setUsername(user.getUsername());
        onlineUser.setRealName(user.getRealName());
        onlineUser.setLoginIp("127.0.0.1");
        onlineUser.setLoginTime(LocalDateTime.now());
        onlineUser.setExpireTime(LocalDateTime.now().plusSeconds((int) jwtService.getTokenExpireTime(AuthConstants.TOKEN_TYPE_ADMIN)));
        onlineUser.setStatus(1);
        onlineUserService.addOnlineUser(onlineUser);
    }

    private void grantRolesToUser(Long userId, List<Long> roleIds, Long operator) {
        userRoleMapper.deleteByUserId(userId);

        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                userRole.setCreateBy(operator);
                userRoles.add(userRole);
            }
            userRoleMapper.batchInsert(userRoles);
        }
    }

    private void clearUserPermissionCache(Long userId) {
        String cacheKey = AuthConstants.REDIS_PERMISSION_KEY + userId;
        redisTemplate.delete(cacheKey);
    }

    private List<String> getUserRolesCodes(Long userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }

    private UserVO convertToUserVO(SysUser user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        userVO.setNickName(user.getNickName());
        userVO.setPhone(user.getPhone());
        userVO.setEmail(user.getEmail());
        userVO.setAvatar(user.getAvatar());
        userVO.setGender(user.getGender());
        userVO.setDeptId(user.getDeptId());
        userVO.setUserType(user.getUserType());
        userVO.setDataScope(user.getDataScope());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setLastLoginIp(user.getLastLoginIp());
        userVO.setRemark(user.getRemark());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setUpdateTime(user.getUpdateTime());

        List<String> roleCodes = getUserRolesCodes(user.getId());
        userVO.setRoleCodes(roleCodes);

        return userVO;
    }

    private LoginResponse buildLoginResponse(SysUser user, String accessToken, String refreshToken,
                                             List<String> roles, Set<String> permissions) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpireIn(jwtService.getTokenExpireTime(AuthConstants.TOKEN_TYPE_ADMIN));

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setRoles(roles);
        userInfo.setPermissions(permissions);
        response.setUserInfo(userInfo);

        return response;
    }
}
