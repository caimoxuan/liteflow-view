package com.cmx.extension;

import com.cmx.extension.loader.ExtensionLoader;
import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;
import com.cmx.extension.runner.IExtensionNodeRunner;
import com.cmx.extension.runner.JavaExtensionNodeRunner;
import com.cmx.extension.runner.LuaExtensionNodeRunner;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 扩展点外部操作类
 */
public class Extensions {

    /**
     * 所有
     */
    private static final List<IExtensionNodeRunner> runners = new ArrayList<>() {
        @Serial
        private static final long serialVersionUID = -3746006302505175571L;
        {
        add(new LuaExtensionNodeRunner());
        add(new JavaExtensionNodeRunner());
    }};

    /**
     * 清除所有扩展点缓存
     * @param bizCode 业务code
     * @param extCode 扩展点code
     */
    public static void clearExtensionCache(String bizCode, String extCode) {
        ExtensionLoader.clearCache(bizCode, extCode);
        runners.forEach(r -> r.clearCache(bizCode, extCode));
    }


    /**
     * 校验脚本语法格式
     * @param script script
     * @param type type
     */
    public static void checkScriptValid(String script, String type) {
        Optional<IExtensionNodeRunner> first = runners.stream()
                .filter(r -> r.isSupport("*." + type, type))
                .findFirst();
        if (first.isEmpty()) {
            throw new RuntimeException("script type " + type + " not support");
        }
        first.get().valid(script);
    }


    /**
     * 获取扩展点并执行
     * @param loadKey 加载key
     * @param extCode 扩展点code
     * @param param 扩展点参数
     * @param clazz 扩展点申明类
     * @return 执行结果
     * @param <T> 结果类型
     */
    public static <T> T execute(String loadKey, String extCode, ExtensionParam param, Class<? extends AbstractExtensionNode<ExtensionData<T>, ExtensionParam>> clazz) {
        // 扩展点参数基本信息赋值
        param.setBizCode(loadKey);
        param.setExtCode(extCode);
        // 加载本地扩展点
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> extNode = ExtensionLoader.loaderLocalExtension(loadKey, extCode, clazz);
        if (extNode == null) {
            extNode = ExtensionLoader.loaderRemoteExtension(loadKey, extCode, clazz);
        }
        if (extNode == null) {
            // 本地远程都不存在扩展点, 缓存, 返回空值
            ExtensionLoader.cacheEmptyNode(loadKey, extCode);
            return null;
        }
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> finalExtNode = extNode;
        IExtensionNodeRunner extensionNodeRunner = runners.stream().filter(r -> r.isSupport(finalExtNode.getScriptFileName(), finalExtNode.getScriptType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("file : " + finalExtNode.getScriptFileName() + " type not support"));
        ExtensionData<T> extensionData = extensionNodeRunner.run(extNode, param);
        if (extensionData.getCode() != 0) {
            throw new RuntimeException("run extension fail case : " + extensionData.getData());
        }
        return extensionData.getData();
    }

}
