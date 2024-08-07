package com.cmx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeProperties {

    /**
     * 标签
     */
    private String id;

    /**
     * tag
     */
    private String tag;

}
