package com.cmx.model.vo;

import lombok.Data;

@Data
public class ExtensionDetailVO {

    /**
     * 当前扩展点的业务标识
     */
    private String bizCode;

    /**
     * 扩展点文本信息
     */
    private String scriptText;

    /**
     * 脚本类型
     */
    private String scriptType;

    /**
     * 扩展点信息
     */
    private ExtensionInfoVO extensionInfo;

}
