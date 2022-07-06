package com.router.plugin.strategy.Impl;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.router.plugin.model.DBContextHolder;
import com.router.plugin.model.DBRouterPluginConfig;
import com.router.plugin.strategy.IDBRouterStrategy;

/**
 * 功能描述
 *
 * @since 2022-04-27
 */
public class DBRouterStrategyFibonacci implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyFibonacci.class);

    private DBRouterPluginConfig dbRouterPluginConfig;

    private int[] dbArr = null;

    private int[] tbArr = null;

    /** 斐波那契散列增量，逻辑：黄金分割点：(√5 - 1) / 2 = 0.6180339887，Math.pow(2, 32) * 0.6180339887 = 0x61c88647 */
    private static final int HASH_INCREMENT = 0x61c88647;

    public DBRouterStrategyFibonacci(DBRouterPluginConfig dbRouterPluginConfig) {
        this.dbRouterPluginConfig = dbRouterPluginConfig;
    }

    private void initArr() {
        dbArr = new int[dbRouterPluginConfig.getDbCount().intValue()];
        tbArr = new int[dbRouterPluginConfig.getTbCount().intValue()];
        Integer dbCount = dbRouterPluginConfig.getDbCount();
        Integer tbCount = dbRouterPluginConfig.getTbCount();
        for (int i = 0; i < dbCount; i ++) {
            dbArr[i] = i + 1;
        }
        for (int i = 0; i < tbCount; i ++) {
            tbArr[i] = i + 1;
        }
        assert null != dbArr;
        assert null != tbArr;
    }

    private int hashIdx(int val, int length) {
        int hashCode = val * HASH_INCREMENT + HASH_INCREMENT;
        return hashCode & (length - 1);
    }

    private int hash(String key, int size) {
        int idx = (size - 1) & (key.hashCode() ^ (key.hashCode() >>> 16));
        return idx;
    }

    @Override
    public void doRouter(String key) {
        //斐波那契算法
//        initArr();
//        int dbInx = dbArr[hashIdx(key.hashCode(), dbArr.length)];
//        int tbInx = tbArr[hashIdx(key.hashCode(), tbArr.length)];
        // 扰动函数 hash算法
//        int size = dbRouterPluginConfig.getDbCount() * dbRouterPluginConfig.getTbCount();
//        int idx = (size - 1) & (key.hashCode() ^ (key.hashCode() >>> 16));
        // 库表索引
        int dbIdx = (dbRouterPluginConfig.getDbCount() - 1) & (key.hashCode() ^ (key.hashCode() >>> 16));
        int tbIdx = (dbRouterPluginConfig.getTbCount() - 1) & (key.hashCode() ^ (key.hashCode() >>> 16));
        String dbIndex = String.format("%02d", dbIdx + 1);
        String tbIndex = String.format("%03d", tbIdx + 1);
        DBContextHolder.setDbKey(dbIndex);
        DBContextHolder.setTbKey(tbIndex);
        logger.info("数据库路由dbIndex:" + dbIndex + ",tbIndex:" + tbIndex);
    }

    @Override
    public void clear() {
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }
}
