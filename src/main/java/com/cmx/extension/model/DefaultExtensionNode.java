package com.cmx.extension.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DefaultExtensionNode<D extends ExtensionData<?>, P extends ExtensionParam> extends AbstractExtensionNode<D, P> {

    /**
     * 创建一个可执行扩展点节点信息
     * @param extensionNode extensionNode
     * @param param p
     * @return node
     * @param <D> data
     * @param <P> param
     */
    public static <D extends ExtensionData<?>, P extends ExtensionParam> DefaultExtensionNode<D, P> newDefault(AbstractExtensionNode<D, P> extensionNode, P param) {
        DefaultExtensionNode<D, P> node = new DefaultExtensionNode<>();
        node.setScriptText(extensionNode.getScriptText());
        node.setScriptFileName(extensionNode.getScriptFileName());
        node.setParam(param);
        return node;
    }
}
