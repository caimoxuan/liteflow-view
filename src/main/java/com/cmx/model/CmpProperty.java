package com.cmx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmpProperty {

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
    private NodeProperties properties;

    /**
     * 条件
     */
    private CmpProperty condition;

    /**
     * 子集
     */
    private List<CmpProperty> children;


}
