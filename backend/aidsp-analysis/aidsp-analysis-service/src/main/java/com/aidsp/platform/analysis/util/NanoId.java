package com.aidsp.platform.analysis.util;

import java.security.SecureRandom;

/**
 * 轻量级 NanoId 生成器。
 * <p>使用 64 字符 URL 安全字母表生成指定长度的随机 ID。
 * <p>等价于 Hutool 的 {@code IdUtil.nanoId(size)}，避免引入额外依赖。
 */
public final class NanoId {

    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    private NanoId() {
    }

    /**
     * 生成指定长度的 NanoId。
     *
     * @param size 长度
     * @return NanoId 字符串
     */
    public static String nanoId(int size) {
        char[] buf = new char[size];
        for (int i = 0; i < size; i++) {
            buf[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(buf);
    }
}
