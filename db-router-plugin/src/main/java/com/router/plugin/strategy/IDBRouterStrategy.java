package com.router.plugin.strategy;

/**
 * 功能描述
 *
 * @since 2022-04-27
 */
public interface IDBRouterStrategy {

    /**
     * 路由策略：默认：斐波那契算法
     */
    void doRouter(String key);

    /**
     * 清除缓存的db和tb信息
     */
    void clear();

}
