package com.cmx.extension.loader;

import java.util.HashMap;
import java.util.Map;

public class ExtensionLoaderFactory {

    private static final Map<String, AbstractExtensionRemoteLoader> remoteLoaders = new HashMap<>();

    /**
     * 注册加载器
     * @param registerKey 注册key
     * @param loader 加载器实例
     */
    public static void register(String registerKey, AbstractExtensionRemoteLoader loader) {
        remoteLoaders.put(registerKey, loader);
    }


    /**
     * 获取加载器
     * @param registerKey register key
     * @return loader
     */
    public static AbstractExtensionRemoteLoader getLoader(String registerKey) {
        AbstractExtensionRemoteLoader loader = remoteLoaders.get(registerKey);
        if (loader == null) {
            throw new RuntimeException("loader :" + registerKey + " not register yet");
        }
        return loader;
    }


}
