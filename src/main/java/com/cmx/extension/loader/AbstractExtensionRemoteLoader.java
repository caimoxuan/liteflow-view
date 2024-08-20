package com.cmx.extension.loader;

import com.cmx.extension.model.AbstractExtensionNode;
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
        try {
            AbstractExtensionNode<ExtensionData<T>, ExtensionParam> extensionNode = clazz.getDeclaredConstructor().newInstance();
            extensionNode.setScriptType(scriptDetail.getScriptType());
            extensionNode.setScriptFileName(scriptDetail.getFileName());
            extensionNode.setScriptText(scriptDetail.getScriptText());
            return extensionNode;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
