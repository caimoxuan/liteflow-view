package com.cmx.extension.loader;

import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.DefaultExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;
import com.cmx.model.ScriptDetail;

public abstract class AbstractExtensionRemoteLoader implements IExtensionRemoteLoader {


    @Override
    public <T> AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loadRemoteExtension(String loadKey, String extCode, Class<? extends AbstractExtensionNode<ExtensionData<T>, ExtensionParam>> clazz) {
        ScriptDetail scriptDetail = loadRemoteScript(loadKey, extCode);
        if (scriptDetail == null) {
            return null;
        }
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> extensionNode = new DefaultExtensionNode<>();
        extensionNode.setScriptType(scriptDetail.getScriptType());
        extensionNode.setScriptFileName(scriptDetail.getFileName());
        extensionNode.setScriptType(scriptDetail.getScriptType());
        return extensionNode;
    }
}
