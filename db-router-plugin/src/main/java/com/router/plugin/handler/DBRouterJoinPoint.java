package com.router.plugin.handler;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.router.plugin.annotation.DBRouter;
import com.router.plugin.model.DBRouterPluginConfig;
import com.router.plugin.strategy.IDBRouterStrategy;

/**
 * 功能描述
 *
 * @since 2022-04-27
 */
@Aspect
public class DBRouterJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);

    private DBRouterPluginConfig dbRouterPluginConfig;

    private IDBRouterStrategy idbRouterStrategy;

    /**
     * 通过构造方法注入配置类和策略类
     * @param dbRouterPluginConfig
     * @param idbRouterStrategy
     */
    public DBRouterJoinPoint(DBRouterPluginConfig dbRouterPluginConfig, IDBRouterStrategy idbRouterStrategy) {
        this.dbRouterPluginConfig = dbRouterPluginConfig;
        this.idbRouterStrategy = idbRouterStrategy;
    }

    @Pointcut("@annotation(com.router.plugin.annotation.DBRouter)")
    public void aopPoint() {

    }

    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint proceedingJoinPoint, DBRouter dbRouter) throws Throwable {
        String key = dbRouter.key();
        if (StringUtils.isEmpty(key) && StringUtils.isEmpty(dbRouterPluginConfig.getRouterKye())) {
            throw new RuntimeException("key is null");
        }
        key = StringUtils.isNotEmpty(key) ? key : dbRouterPluginConfig.getRouterKye();
        String keyAttr = getAttrValue(key, proceedingJoinPoint.getArgs());
        //执行数据库路由
        idbRouterStrategy.doRouter(keyAttr);
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            idbRouterStrategy.clear();
        }
    }

    public String getAttrValue(String attr, Object[] args) {
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {
                return arg.toString();
            }
        }

        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                filedValue = BeanUtils.getProperty(arg, attr);
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }
}
