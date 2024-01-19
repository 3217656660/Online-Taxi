package com.zxy.work.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码加密工具
 */
public final class  PasswordEncoder {
    private static final int WORKLOAD = 8; // 加密迭代次数

    /**
     * 对密码进行加密
     *
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String encode(String password) {
        // 生成随机盐值
        String salt = BCrypt.gensalt(WORKLOAD);

        // 使用盐值和密码进行哈希计算
        return BCrypt.hashpw(password, salt);
    }

    /**
     * 校验密码是否匹配
     *
     * @param password       待校验的明文密码
     * @param hashedPassword 数据库中存储的加密密码
     * @return 如果密码匹配，返回 true；否则返回 false。
     */
    public static boolean matches(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}