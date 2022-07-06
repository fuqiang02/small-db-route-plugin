package com.router.plugin.model;

/**
 * 功能描述
 *
 * @since 2022-04-27
 */
public class DBContextHolder {

    /**
     * 数据库key
     */
    private static final ThreadLocal<String> dbKey = new ThreadLocal<String>();

    /**
     * 表key
     */
    private static final ThreadLocal<String> tbKey = new ThreadLocal<String>();

    public static void setDbKey(String db) {
        dbKey.set(db);
    }

    public static String getDbKey() {
        return dbKey.get();
    }

    public static void setTbKey(String tb) {
        tbKey.set(tb);
    }

    public static String getTbKey() {
        return tbKey.get();
    }

    public static void clearDBKey() {
        dbKey.remove();
    }

    public static void clearTBKey() {
        tbKey.remove();
    }
}
