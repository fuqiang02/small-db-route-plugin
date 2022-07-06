package com.router.plugin.model;

/**
 * 功能描述
 *
 * @since 2022-04-27
 */
public class DBRouterPluginConfig {

    /**
     * 分库数量配置
     */
    private Integer dbCount;

    /**
     * 分表数量配置
     */
    private Integer tbCount;

    /**
     * 路由key
     */
    private String routerKye;

    /**
     * 默认数据源
     */
    private String defaultDb;

    /**
     * 分库集合
     */
    private String list;

    public DBRouterPluginConfig(Integer dbCount, Integer tbCount, String routerKye) {
        this.dbCount = dbCount;
        this.tbCount = tbCount;
        this.routerKye = routerKye;
    }

    public Integer getDbCount() {
        return dbCount;
    }

    public void setDbCount(Integer dbCount) {
        this.dbCount = dbCount;
    }

    public Integer getTbCount() {
        return tbCount;
    }

    public void setTbCount(Integer tbCount) {
        this.tbCount = tbCount;
    }

    public String getDefaultDb() {
        return defaultDb;
    }

    public void setDefaultDb(String defaultDb) {
        this.defaultDb = defaultDb;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getRouterKye() {
        return routerKye;
    }

    public void setRouterKye(String routerKye) {
        this.routerKye = routerKye;
    }
}
