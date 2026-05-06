package com.zzl.platform.common.core.util;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 数据脱敏工具类
 * 提供常用敏感信息的脱敏处理
 */
public class DesensitizationUtils {

    private DesensitizationUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 手机号脱敏
     * 显示前三位和后四位，如: 138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return phone;
        }
        try {
            return DesensitizedUtil.mobilePhone(phone);
        } catch (Exception e) {
            return mask(phone, 3, 4);
        }
    }

    /**
     * 身份证脱敏
     * 显示前六位和后四位，如: 110101**********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String desensitizeIdCard(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return idCard;
        }
        try {
            return DesensitizedUtil.idCardNum(idCard, 6, 4);
        } catch (Exception e) {
            return mask(idCard, 6, 4);
        }
    }

    /**
     * 银行卡号脱敏
     * 显示前六位和后四位，如: 622202**********1234
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String desensitizeBankCard(String bankCard) {
        if (StrUtil.isBlank(bankCard)) {
            return bankCard;
        }
        try {
            return DesensitizedUtil.bankCard(bankCard);
        } catch (Exception e) {
            return mask(bankCard, 6, 4);
        }
    }

    /**
     * 邮箱脱敏
     * 显示@前的前两位和@后的域名，如: te***@163.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String desensitizeEmail(String email) {
        if (StrUtil.isBlank(email)) {
            return email;
        }
        try {
            return DesensitizedUtil.email(email);
        } catch (Exception e) {
            int atIndex = email.indexOf('@');
            if (atIndex > 2) {
                return email.substring(0, 2) + "***" + email.substring(atIndex);
            }
            return mask(email, 2, 1);
        }
    }

    /**
     * 姓名脱敏
     * 显示第一个字和最后一个字，如: 张*三
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String desensitizeName(String name) {
        if (StrUtil.isBlank(name)) {
            return name;
        }
        try {
            return DesensitizedUtil.chineseName(name);
        } catch (Exception e) {
            if (name.length() == 1) {
                return name;
            } else if (name.length() == 2) {
                return name.charAt(0) + "*";
            } else {
                return name.charAt(0) + "*" + name.charAt(name.length() - 1);
            }
        }
    }

    /**
     * 地址脱敏
     * 显示前N位，如: 北京市***
     *
     * @param address      地址
     * @param prefixLength 前缀显示长度
     * @return 脱敏后的地址
     */
    public static String desensitizeAddress(String address, int prefixLength) {
        if (StrUtil.isBlank(address)) {
            return address;
        }
        if (address.length() <= prefixLength) {
            return address;
        }
        return address.substring(0, prefixLength) + "***";
    }

    /**
     * 密码脱敏
     * 全部显示为*
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String desensitizePassword(String password) {
        if (StrUtil.isBlank(password)) {
            return password;
        }
        return "******";
    }

    /**
     * 通用脱敏方法
     *
     * @param value        原始值
     * @param prefixLength 前缀保留长度
     * @param suffixLength 后缀保留长度
     * @return 脱敏后的值
     */
    public static String mask(String value, int prefixLength, int suffixLength) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        int length = value.length();
        if (length <= prefixLength + suffixLength) {
            return StrUtil.repeat('*', length);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(value, 0, prefixLength);
        sb.append(StrUtil.repeat('*', Math.max(1, length - prefixLength - suffixLength)));
        sb.append(value.substring(length - suffixLength));
        return sb.toString();
    }

    /**
     * 脱敏Map中的敏感字段
     *
     * @param data   Map数据
     * @param fields 需要脱敏的字段名
     * @return 脱敏后的数据副本
     */
    public static java.util.Map<String, Object> desensitizeMap(java.util.Map<String, Object> data, String... fields) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>(data);
        for (String field : fields) {
            if (result.containsKey(field)) {
                Object value = result.get(field);
                if (value instanceof String) {
                    result.put(field, mask((String) value, 3, 4));
                }
            }
        }
        return result;
    }
}
