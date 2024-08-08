package com.cmx.model.vo;

import com.cmx.model.CmpProperty;
import com.cmx.model.Properties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChainDetailVO {

    /**
     * id
     */
    private String id;

    /**
     * 类型
     */
    private String type;

    /**
     * 属性
     */
    private Properties properties;

    /**
     * 条件
     */
    private CmpProperty condition;

    /**
     * 扩展点数量
     */
    private Integer extensionCount;

    /**
     * 子集
     */
    private List<ChainDetailVO> children;

}
