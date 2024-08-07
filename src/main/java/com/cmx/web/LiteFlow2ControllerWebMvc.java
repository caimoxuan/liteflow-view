package com.cmx.web;

import com.cmx.model.vo.ChainInfoVO;
import com.cmx.model.vo.CmpInfoVO;
import com.yomahub.liteflow.flow.FlowBus;
import com.yomahub.liteflow.flow.element.Chain;
import com.yomahub.liteflow.flow.element.Node;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
//@Conditional({OnServletBasedWebApplication.class})
@RequestMapping("/v1/liteflow/api/")
public class LiteFlow2ControllerWebMvc {


    @GetMapping("/cmpList")
    public List<CmpInfoVO> getCmpList() {
        List<CmpInfoVO> result = new ArrayList<>();
        Map<String, Node> nodeMap = FlowBus.getNodeMap();
        if (nodeMap.isEmpty()) {
            return result;
        }
        nodeMap.forEach((k, v) -> {
            result.add(CmpInfoVO.builder()
                            .cmpId(v.getId())
                            .cmpName(v.getName())
                    .build());
        });
        return result;
    }

    @GetMapping("/chainList")
    public List<ChainInfoVO> getChainList() {
        List<ChainInfoVO> result = new ArrayList<>();
        Map<String, Chain> chainMap = FlowBus.getChainMap();
        if (chainMap.isEmpty()) {
            return result;
        }
        chainMap.forEach((k, v) -> {
            result.add(ChainInfoVO.builder()
                            .chainId(v.getChainId())
                            .chainName(v.getId())
                    .build());
        });
        return result;
    }

}
