package com.zzl.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.platform.auth.entity.SysOnlineUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 在线用户Mapper
 */
@Mapper
public interface SysOnlineUserMapper extends BaseMapper<SysOnlineUser> {

    /**
     * 查询用户的所有在线会话
     */
    @Select("SELECT * FROM t_sys_online_user WHERE user_id = #{userId} AND status = 1")
    List<SysOnlineUser> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据会话ID更新状态（踢出）
     */
    @Delete("UPDATE t_sys_online_user SET status = 2 WHERE id = #{id}")
    int kickOut(@Param("id") Long id);

    /**
     * 踢出用户的所有会话
     */
    @Delete("UPDATE t_sys_online_user SET status = 2 WHERE user_id = #{userId}")
    int kickOutUser(@Param("userId") Long userId);

    /**
     * 删除过期的在线会话
     */
    @Delete("DELETE FROM t_sys_online_user WHERE expire_time < NOW()")
    int deleteExpired();
}
