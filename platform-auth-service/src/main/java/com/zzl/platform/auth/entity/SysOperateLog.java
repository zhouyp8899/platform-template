package com.zzl.platform.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("t_sys_operate_log")
public class SysOperateLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作人类型
     */
    private String operatorType;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 请求URL
     */
    private String operUrl;

    /**
     * 主机地址
     */
    private String operIp;

    /**
     * 操作地点
     */
    private String operLocation;

    /**
     * 请求参数
     */
    private String operParam;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作状态
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 消耗时间（毫秒）
     */
    private Integer costTime;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;
}
