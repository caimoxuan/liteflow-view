package com.cmx.extension.annotation;

import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ExtensionCmp {


    /**
     * 当前组件下的扩展点信息
     * @return ext node
     */
    Class<? extends AbstractExtensionNode<? extends ExtensionData<?>, ? extends ExtensionParam>>[] nodes();


}
