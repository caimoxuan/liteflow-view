package com.cmx.web.service;

import com.cmx.extension.Extensions;
import com.cmx.extension.annotation.ExtensionCmp;
import com.cmx.extension.annotation.ExtensionPoint;
import com.cmx.extension.loader.IExtensionRemoteLoader;
import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;
import com.cmx.model.CmpProperty;
import com.cmx.model.ScriptDetail;
import com.cmx.model.vo.*;
import com.cmx.parser.generator.ExpressGenerator;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import com.yomahub.liteflow.flow.FlowBus;
import com.yomahub.liteflow.flow.element.Chain;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Node;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LiteFlowViewService {


    @Autowired(required = false)
    private IExtensionRemoteLoader remoteLoader;

    /**
     * 获取当前项目所有的组件
     *
     * @return cmp list
     */
    public List<CmpInfoVO> getCmpList() {
        List<CmpInfoVO> result = new ArrayList<>();
        Map<String, Node> nodeMap = FlowBus.getNodeMap();
        if (nodeMap.isEmpty()) {
            return result;
        }
        for (Node node : nodeMap.values()) {
            result.add(CmpInfoVO.builder()
                    .cmpId(node.getId())
                    .cmpName(node.getName())
                    .build());
        }
        return result;
    }


    /**
     * 获取当前项目所有流程
     *
     * @return chain list
     */
    public List<ChainInfoVO> getChainList() {
        List<ChainInfoVO> result = new ArrayList<>();
        Map<String, Chain> chainMap = FlowBus.getChainMap();
        if (chainMap.isEmpty()) {
            return result;
        }
        for (Chain chain : chainMap.values()) {
            result.add(ChainInfoVO.builder()
                    .chainId(chain.getChainId())
                    .chainName(chain.getId())
                    .build());
        }
        return result;
    }


    /**
     * 获取流程详细信息
     *
     * @param chainId chainId
     * @return vo
     */
    public ChainDetailVO getChainJsonEL(String chainId) {
        Chain chain = FlowBus.getChainMap().get(chainId);
        if (chain == null) {
            return new ChainDetailVO();
        }
        List<Condition> conditions = chain.getConditionList();
        if (CollectionUtils.isEmpty(conditions)) {
            return new ChainDetailVO();
        }
        CmpProperty cmpProperty = ExpressGenerator.builderJsonEL(conditions.get(0));
        return toChainDetail(cmpProperty);
    }

    /**
     * 获取组件节点信息
     *
     * @param nodeId 节点id
     * @return node info
     */
    public NodeDetailVO getNodeDetail(String nodeId) {
        NodeDetailVO nodeDetail = new NodeDetailVO();
        Node node = FlowBus.getNode(nodeId);
        nodeDetail.setNodeId(nodeId);
        if (node == null) {
            return nodeDetail;
        }
        nodeDetail.setNodeName(node.getName());
        List<ExtensionInfoVO> extensionInfos = new ArrayList<>();
        ExtensionCmp annotation = node.getInstance().getClass().getAnnotation(ExtensionCmp.class);
        if (annotation != null) {
            Class<? extends AbstractExtensionNode<? extends ExtensionData<?>, ? extends ExtensionParam>>[] nodes = annotation.nodes();
            for (Class<? extends AbstractExtensionNode<? extends ExtensionData<?>, ? extends ExtensionParam>> n : nodes) {
                ExtensionInfoVO extensionInfo = new ExtensionInfoVO();
                ExtensionPoint pointAnno = n.getAnnotation(ExtensionPoint.class);
                extensionInfo.setExtCode(pointAnno.extCode());
                extensionInfo.setExtDesc(pointAnno.extDesc());
                extensionInfos.add(extensionInfo);
            }
        }
        nodeDetail.setExtensions(extensionInfos);
        return nodeDetail;
    }

    /**
     * 获取远程扩展点信息
     *
     * @param bizCode 业务编码
     * @param extCode 扩展点编码
     * @return 扩展点信息
     */
    public ExtensionDetailVO getExtensionDetail(String bizCode, String extCode) {
        ExtensionDetailVO detailVO = new ExtensionDetailVO();
        detailVO.setBizCode(bizCode);
        if (remoteLoader == null) {
            return detailVO;
        }
        ScriptDetail scriptDetail = remoteLoader.loadRemoteScript(bizCode, extCode);
        if (scriptDetail != null) {
            detailVO.setScriptText(scriptDetail.getScriptText());
            detailVO.setScriptType(scriptDetail.getScriptType());
        }
        ExtensionInfoVO extensionInfoVO = new ExtensionInfoVO();
        extensionInfoVO.setExtCode(extCode);
        detailVO.setExtensionInfo(extensionInfoVO);
        return detailVO;
    }

    /**
     * 保存扩展点
     * @param bizCode 业务code
     * @param extCode 扩展点code
     * @param scriptDetail 脚本信息
     */
    public void createExtensionScript(String bizCode, String extCode, ScriptDetail scriptDetail) {
        if (remoteLoader == null) {
            throw new RuntimeException("not implement method com.cmx.extension.loader.IExtensionRemoteLoader.saveRemoteScript!");
        }
        remoteLoader.saveRemoteScript(bizCode, extCode, scriptDetail);
    }

    /**
     * 更新扩展点脚本
     * @param bizCode 业务code
     * @param extCode 扩展点code
     * @param scriptDetail 脚本信息
     */
    public void updateExtensionScript(String bizCode, String extCode, ScriptDetail scriptDetail) {
        if (remoteLoader == null) {
            throw new RuntimeException("not implement method com.cmx.extension.loader.IExtensionRemoteLoader.updateRemoteScript!");
        }
        remoteLoader.updateRemoteScript(bizCode, extCode, scriptDetail);
    }

    public Boolean checkScript(String bizCode, String extCode, ScriptDetail scriptDetail) {
        Extensions.clearExtensionCache(bizCode, extCode);
    }

    /**
     * to detail
     *
     * @param cmpProperty property
     * @return v
     */
    private ChainDetailVO toChainDetail(CmpProperty cmpProperty) {
        if (cmpProperty == null) {
            return null;
        }
        int extensionCount = 0;
        if (NodeTypeEnum.COMMON.getMappingClazz().getSimpleName().equals(cmpProperty.getType())) {
            // find extension count
            Node node = FlowBus.getNode(cmpProperty.getId());
            if (node != null) {
                ExtensionCmp annotation = node.getInstance().getClass().getAnnotation(ExtensionCmp.class);
                if (annotation != null) {
                    extensionCount = annotation.nodes().length;
                }
            }
        }
        List<CmpProperty> cmpProperties = Optional.ofNullable(cmpProperty.getChildren()).orElse(new ArrayList<>());
        return ChainDetailVO.builder()
                .id(cmpProperty.getId())
                .properties(cmpProperty.getProperties())
                .type(cmpProperty.getType())
                .extensionCount(extensionCount)
                .condition(cmpProperty.getCondition())
                .children(cmpProperties.stream().map(this::toChainDetail).collect(Collectors.toList()))
                .build();
    }


}
