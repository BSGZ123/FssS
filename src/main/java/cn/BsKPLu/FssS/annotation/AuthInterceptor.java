package cn.BsKPLu.FssS.annotation;

import cn.BsKPLu.FssS.enums.InterceptorLevel;

import java.lang.annotation.*;

/**
 * @author BsKPLu
 * @since 2019/10/25
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthInterceptor {

    /**
     * 定义拦截级别，默认为用户级别拦截
     *
     * @return {@link InterceptorLevel}
     */
    InterceptorLevel value() default InterceptorLevel.USER;
}
