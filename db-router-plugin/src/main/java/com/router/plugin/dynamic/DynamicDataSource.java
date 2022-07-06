package com.router.plugin.dynamic;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.router.plugin.model.DBContextHolder;

/**
 * 功能描述
 *
 * @since 2022-04-27
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 动态赋值数据源
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DBContextHolder.getDbKey();
    }

    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }
}
