package com.cmx.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class NodeDetailVO {

    private String nodeId;

    private String nodeName;

    private List<ExtensionInfoVO> extensions;

}
