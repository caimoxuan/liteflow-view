package com.cmx.extension.loader;

import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;

public abstract class AbstractExtensionRemoteLoader {


    abstract <T> AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loadRemoteExtension(String loadKey, String extCode, ExtensionParam param, Class<? extends AbstractExtensionNode<ExtensionData<T>, ExtensionParam>> clazz);



}
