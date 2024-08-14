package com.cmx.extension.model;

import lombok.Data;

@Data
public class ExtensionData<T> {

    /**
     * 返回编码
     */
    private Integer code;

    /**
     * 扩展点返回信息描述
     */
    private String message;

    /**
     * 数据返回
     */
    private T data;

    public ExtensionData() {}

    public ExtensionData(T data) {
        this.data = data;
        this.code = 0;
        this.message = "success";
    }

}
