package com.zzl.platform.auth.service;

import com.zzl.platform.auth.dto.*;
import com.zzl.platform.auth.entity.SysUser;
import com.zzl.platform.auth.vo.LoginResponse;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.UserVO;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 管理员登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse adminLogin(LoginRequest request);

    /**
     * H5手机号登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse h5PhoneLogin(PhoneLoginRequest request);

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @param tokenType    Token类型
     * @return 新Token
     */
    LoginResponse refreshToken(String refreshToken, String tokenType);

    /**
     * 登出
     *
     * @param userId    用户ID
     * @param token     Token
     * @param tokenType Token类型
     */
    void logout(Long userId, String token, String tokenType);

    /**
     * 发送验证码
     *
     * @param request 发送验证码请求
     */
    void sendCode(SendCodeRequest request);

    /**
     * 分页查询用户列表
     *
     * @param request 分页请求
     * @return 分页响应
     */
    PageResponse<UserVO> pageUser(PageRequest<UserQueryRequest> request);

    /**
     * 根据ID查询用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserVO getUserById(Long userId);

    /**
     * 新增用户
     *
     * @param request  新增请求
     * @param operator 操作人ID
     * @return 用户ID
     */
    Long addUser(UserAddRequest request, Long operator);

    /**
     * 编辑用户
     *
     * @param request  编辑请求
     * @param operator 操作人ID
     */
    void editUser(UserEditRequest request, Long operator);

    /**
     * 删除用户
     *
     * @param userId   用户ID
     * @param operator 操作人ID
     */
    void deleteUser(Long userId, Long operator);

    /**
     * 批量删除用户
     *
     * @param userIds  用户ID列表
     * @param operator 操作人ID
     */
    void batchDeleteUsers(java.util.List<Long> userIds, Long operator);

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param operator 操作人ID
     */
    void resetPassword(Long userId, Long operator);

    /**
     * 修改用户状态
     *
     * @param userId   用户ID
     * @param status   状态
     * @param operator 操作人ID
     */
    void changeUserStatus(Long userId, Integer status, Long operator);

    /**
     * 为用户分配角色
     *
     * @param request  分配角色请求
     * @param operator 操作人ID
     */
    void grantRoles(GrantRolesRequest request, Long operator);

    /**
     * 获取用户的角色列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    java.util.List<Long> getUserRoles(Long userId);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 修改当前用户密码
     *
     * @param userId  用户ID
     * @param request 修改密码请求
     */
    void changeCurrentUserPassword(Long userId, ChangePasswordRequest request);

    /**
     * 修改当前用户信息
     *
     * @param userId   用户ID
     * @param request  修改信息请求
     * @param operator 操作人ID
     */
    void editCurrentUser(Long userId, UserEditRequest request, Long operator);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    SysUser getUserByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    SysUser getUserByPhone(String phone);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean checkPhoneExists(String phone);

    /**
     * 获取用户权限编码列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    java.util.Set<String> getUserPermissions(Long userId);

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId         用户ID
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userId   用户ID
     * @param roleCode 角色编码
     * @return 是否拥有角色
     */
    boolean hasRole(Long userId, String roleCode);
}
