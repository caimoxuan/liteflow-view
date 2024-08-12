package com.cmx.extension.loader;

import org.springframework.context.ApplicationContext;

public class ExtensionLoaderFactory {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * 获取加载器
     * @return loader
     */
    public static AbstractExtensionRemoteLoader getLoader() {
        if (applicationContext == null) {
            throw new RuntimeException("extension not enable");
        }
        return (AbstractExtensionRemoteLoader)applicationContext.getBean(IExtensionRemoteLoader.class);
    }


}
