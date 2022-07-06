package com.router.plugin.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import com.router.plugin.dynamic.DynamicDataSource;
import com.router.plugin.dynamic.DynamicMybatisPlugin;
import com.router.plugin.handler.DBRouterJoinPoint;
import com.router.plugin.model.DBRouterPluginConfig;
import com.router.plugin.strategy.IDBRouterStrategy;
import com.router.plugin.strategy.Impl.DBRouterStrategyFibonacci;
import com.router.plugin.untils.PropertyUtil;

/**
 * 功能描述
 *
 * @since 2022-04-27
 */
@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {

    /**
     * 分库数量
     */
    private Integer dbCount = 0;

    /**
     * 分表数量
     */
    private Integer tbCount = 0;

    /**
     * 路由key
     */
    private String routerKey;

    /**
     * 分库集合
     */
    private String dataSourceList = "";

    /**
     * 数据源配置组
     */
    private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceMap;

    @Bean
    public IDBRouterStrategy idbRouterStrategy(DBRouterPluginConfig dbRouterPluginConfig) {
        return new DBRouterStrategyFibonacci(dbRouterPluginConfig);
    }

    @Bean(name = "db-router-point")
    @ConditionalOnMissingBean
    public DBRouterJoinPoint point(DBRouterPluginConfig dbRouterPluginConfig, IDBRouterStrategy idbRouterStrategy) {
        return new DBRouterJoinPoint(dbRouterPluginConfig, idbRouterStrategy);
    }

    @Bean
    public DBRouterPluginConfig dbRouterPluginConfig() {
        return new DBRouterPluginConfig(dbCount, tbCount, routerKey);
    }

    @Bean
    public Interceptor mybatisPlugin() {
        return new DynamicMybatisPlugin();
    }

    @Bean
    public DataSource dataSource() {
        //赋值分库分表数据源
        Map<Object, Object> targetDataSource = new HashMap<>();
        for (String dbInfo : dataSourceMap.keySet()) {
            Map<String, Object> objMap = dataSourceMap.get(dbInfo);
            targetDataSource.put(dbInfo, new DriverManagerDataSource(objMap.get("url").toString()
                    , objMap.get("username").toString()
                    , objMap.get("password").toString()));
        }
        DynamicDataSource dynamicDataSource = new DynamicDataSource(
                new DriverManagerDataSource(defaultDataSourceMap.get("url").toString()
                ,defaultDataSourceMap.get("username").toString(),
                defaultDataSourceMap.get("password").toString()), targetDataSource);
        return dynamicDataSource;
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    /**
     * 获取到application.yml的所有配置，进行赋值
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        //读取多数据源配置前缀
        String prefix = "db-router-plugin.datasource.";
        //读取分库数量
        dbCount = Integer.parseInt(environment.getProperty(prefix + "dbCount"));
        //读取分表数量
        tbCount = Integer.parseInt(environment.getProperty(prefix + "tbCount"));
        //读取分表数量
        routerKey = environment.getProperty(prefix + "routerKey");
        //读取分库数据源list
        dataSourceList = environment.getProperty(prefix + "list");
        //如果为空，则抛出异常
        assert StringUtils.isNotEmpty(dataSourceList);
        //获取数据源列表集合
        String[] dataSourceArr = dataSourceList.split(",");
        //遍历数据源列表集合并赋值
        for (String dataSource : dataSourceArr) {
            //获取配置
            Map<String, Object> dataSourcePros = PropertyUtil.handle(environment, prefix + dataSource, Map.class);
            //放入分库集合
            dataSourceMap.put(dataSource, dataSourcePros);
        }
        //获取默认配置的数据源 db00
        String defaultDataSource = environment.getProperty(prefix + "default");
        defaultDataSourceMap = PropertyUtil.handle(environment, prefix + defaultDataSource, Map.class);
    }
}
