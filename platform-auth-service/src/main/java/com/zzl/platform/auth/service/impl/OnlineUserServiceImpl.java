package com.zzl.platform.auth.service.impl;

import com.zzl.platform.auth.entity.SysOnlineUser;
import com.zzl.platform.auth.mapper.SysOnlineUserMapper;
import com.zzl.platform.auth.service.OnlineUserService;
import com.zzl.platform.auth.vo.OnlineUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 在线用户服务实现
 */
@Slf4j
@Service
public class OnlineUserServiceImpl implements OnlineUserService {

    private final SysOnlineUserMapper onlineUserMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public OnlineUserServiceImpl(SysOnlineUserMapper onlineUserMapper,
                                 RedisTemplate<String, Object> redisTemplate) {
        this.onlineUserMapper = onlineUserMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOnlineUser(SysOnlineUser onlineUser) {
        // 1. 保存到数据库
        onlineUserMapper.insert(onlineUser);

        // 2. 保存到Redis（用于快速查询）
        String redisKey = "auth:online:user:" + onlineUser.getId();
        redisTemplate.opsForValue().set(redisKey, onlineUser,
                calculateExpireTime(onlineUser), TimeUnit.SECONDS);

        log.info("Add online user: userId={}, sessionId={}", onlineUser.getUserId(), onlineUser.getId());
    }

    @Override
    public List<OnlineUserVO> listOnlineUsers() {
        // 1. 从数据库查询
        List<SysOnlineUser> onlineUsers = onlineUserMapper.selectList(null);

        // 2. 转换为VO
        List<OnlineUserVO> result = new ArrayList<>();
        for (SysOnlineUser user : onlineUsers) {
            OnlineUserVO vo = new OnlineUserVO();
            BeanUtils.copyProperties(user, vo);
            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void kickOutUser(Long sessionId) {
        // 1. 更新数据库状态
        onlineUserMapper.kickOut(sessionId);

        // 2. 删除Redis缓存
        String redisKey = "auth:online:user:" + sessionId;
        redisTemplate.delete(redisKey);

        log.info("Kick out user: sessionId={}", sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void kickOutUserAll(Long userId) {
        // 1. 查询用户的所有在线会话
        List<SysOnlineUser> sessions = onlineUserMapper.selectByUserId(userId);

        // 2. 逐个踢出
        for (SysOnlineUser session : sessions) {
            kickOutUser(session.getId());
        }

        log.info("Kick out all sessions for user: userId={}, sessionCount={}", userId, sessions.size());
    }

    @Override
    public List<SysOnlineUser> getUserSessions(Long userId) {
        return onlineUserMapper.selectList(null).stream()
                .filter(session -> userId.equals(session.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void deleteExpiredUsers() {
        int deleted = onlineUserMapper.deleteExpired();
        if (deleted > 0) {
            log.info("Delete expired online users: count={}", deleted);
        }
    }

    /**
     * 计算过期时间（秒）
     */
    private long calculateExpireTime(SysOnlineUser onlineUser) {
        if (onlineUser.getExpireTime() == null) {
            return 7200; // 默认2小时
        }
        LocalDateTime now = LocalDateTime.now();
        if (onlineUser.getExpireTime().isBefore(now)) {
            return 0;
        }
        return java.time.Duration.between(now, onlineUser.getExpireTime()).getSeconds();
    }
}
