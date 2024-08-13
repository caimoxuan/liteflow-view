package com.cmx.model.vo;

import com.cmx.model.ScriptDetail;
import lombok.Data;

@Data
public class ScriptDataVO {

    /**
     * 扩展点code
     */
    private String bizCode;

    /**
     * 扩展点code
     */
    private String extCode;

    /**
     * 脚本信息
     */
    private ScriptDetail scriptDetail;



}
