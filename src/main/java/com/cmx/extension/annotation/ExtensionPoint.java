package com.cmx.extension.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtensionPoint {

    /**
     * 扩展点编码
     * @return code
     */
    String extCode();

    /**
     * 扩展点说明
     * @return desc
     */
    String extDesc();


}
