package com.router.plugin.dynamic;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import com.router.plugin.annotation.DBRouterStrategy;
import com.router.plugin.model.DBContextHolder;

/**
 * 功能描述
 *
 * @since 2022-04-27
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DynamicMybatisPlugin implements Interceptor {

    private Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取statementHandler
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        // 获取指定对象的元信息
        MetaObject metaObject = MetaObject.forObject(statementHandler
                , SystemMetaObject.DEFAULT_OBJECT_FACTORY
                , SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY
                , new DefaultReflectorFactory());
        // 然后就可以通过MetaObject获取对象的属性
        // 获取RoutingStatementHandler->PrepareStatementHandler->BaseStatementHandler中的mappedStatement
        // mappedStatement 包含了Sql的信息
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        Class<?> clazz = Class.forName(className);
        //获取到当前DAO执行的持久化方法，获取分库分表的配置
        DBRouterStrategy dbRouterStrategy = clazz.getAnnotation(DBRouterStrategy.class);
        //如果没有使用DBRouterStrategy注解，或者不需要分表，则直接执行方法
        if (null == dbRouterStrategy || !dbRouterStrategy.splitTable()) {
            return invocation.proceed();
        }
        //如果需要分表，则替换sql中的表名
        //获取sql
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        //获取sql中的表名
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if (matcher.find()) {
            tableName = matcher.group().trim();
        }
        assert StringUtils.isNotEmpty(tableName);
        //将sql中的表名替换成动态设置的表名
        String replaceSql = matcher.replaceAll(tableName + "_" + DBContextHolder.getTbKey());
        //通过反射执行sql
        Field sqlField = boundSql.getClass().getDeclaredField("sql");
        sqlField.setAccessible(true);
        sqlField.set(boundSql, replaceSql);
        return invocation.proceed();
    }
}
