package com.cmx.model;

import lombok.Data;

@Data
public class ScriptDetail {

    /**
     * 脚本内容信息
     */
    private String scriptText;

    /**
     * 脚本文件名称
     */
    private String fileName;

    /**
     * 脚本的类型
     */
    private String scriptType;

}
