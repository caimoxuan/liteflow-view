package com.cmx.extension.model;

import lombok.Data;

@Data
public class ExtensionParam {

    /**
     * 加载器注册key 用于获取指定远程加载器
     */
    private String loadKey;

}
