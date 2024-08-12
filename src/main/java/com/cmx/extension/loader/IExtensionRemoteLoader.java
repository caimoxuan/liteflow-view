package com.cmx.extension.loader;

import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;

public interface IExtensionRemoteLoader {

    /**
     * 加载远程扩展点脚本信息
     * @param loadKey 加载业务key
     * @param extCode 扩展点编码
     * @return script
     */
     String loadRemoteScript(String loadKey, String extCode);

    /**
     * 获取扩展点信息
     * @param loadKey 业务key
     * @param extCode 扩展点编码
     * @param clazz 当前扩展点类
     * @return 返回
     * @param <T> 返回类型
     */
    <T> AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loadRemoteExtension(String loadKey, String extCode, Class<? extends AbstractExtensionNode<ExtensionData<T>, ExtensionParam>> clazz);

}
