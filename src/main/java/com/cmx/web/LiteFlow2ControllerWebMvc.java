package com.cmx.web;

import com.cmx.model.vo.*;
import com.cmx.web.service.LiteFlowViewService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
//@Conditional({OnServletBasedWebApplication.class})
@RequestMapping("/v1/liteflow/api/")
public class LiteFlow2ControllerWebMvc {

    @Resource
    private LiteFlowViewService liteFlowViewService;

    @GetMapping("/cmpList")
    public List<CmpInfoVO> getCmpList() {
        return liteFlowViewService.getCmpList();
    }

    @GetMapping("/chainList")
    public List<ChainInfoVO> getChainList() {
        return liteFlowViewService.getChainList();
    }

    @GetMapping("/chainDetail")
    public ChainDetailVO getChainDetail(@RequestParam String chainId) {
        return liteFlowViewService.getChainJsonEL(chainId);
    }

    @GetMapping("/nodeDetail")
    public NodeDetailVO getNodeDetail(@RequestParam String nodeId) {
        return liteFlowViewService.getNodeDetail(nodeId);
    }

    @GetMapping("/extensionDetail")
    public ExtensionDetailVO getExtensionDetail(@RequestParam String bizCode, @RequestParam String extCode) {
        return liteFlowViewService.getExtensionDetail(bizCode, extCode);
    }

    @PutMapping("/extensionScript")
    public String updateExtensionScript(@RequestBody ScriptDataVO scriptData) {
        liteFlowViewService.updateExtensionScript(scriptData.getBizCode(),
                scriptData.getExtCode(),
                scriptData.getScriptDetail());
        return "success";
    }

    @PostMapping("/extensionScript")
    public String createExtensionScript(@RequestBody ScriptDataVO scriptData) {
        liteFlowViewService.createExtensionScript(scriptData.getBizCode(),
                scriptData.getExtCode(),
                scriptData.getScriptDetail());
        return "success";
    }


}
