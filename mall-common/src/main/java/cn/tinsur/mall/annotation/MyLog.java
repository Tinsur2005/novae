package cn.tinsur.mall.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyLog {

    /**
     * 模块
     */
    public String module() default "";

}