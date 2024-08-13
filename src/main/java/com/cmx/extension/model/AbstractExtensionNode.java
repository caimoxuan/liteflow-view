package com.cmx.extension.model;

import lombok.Data;

@Data
public abstract class AbstractExtensionNode<D extends ExtensionData<?>, P extends ExtensionParam> {

    /**
     * 脚本返回数据
     */
    private D data;

    /**
     * 脚本参数
     */
    private P param;

    /**
     * 脚本信息
     */
    private String scriptText;

    /**
     * 脚本文件名称
     */
    private String scriptFileName;

    /**
     * 脚本类型 （java / lua / js / ...）
     */
    private String scriptType;

}
