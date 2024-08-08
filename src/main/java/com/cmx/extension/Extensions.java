package com.cmx.extension;

import com.cmx.extension.loader.ExtensionLoader;
import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;
import com.cmx.extension.runner.IExtensionNodeRunner;
import com.cmx.extension.runner.JavaExtensionNodeRunner;
import com.cmx.extension.runner.LuaExtensionNodeRunner;

import java.util.ArrayList;
import java.util.List;

public class Extensions {

    /**
     * 所有
     */
    private static final List<IExtensionNodeRunner> runners = new ArrayList<IExtensionNodeRunner>() {
        private static final long serialVersionUID = -3746006302505175571L;
        {
        add(new LuaExtensionNodeRunner());
        add(new JavaExtensionNodeRunner());
    }};


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
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> extNode = ExtensionLoader.loaderLocalExtension(loadKey, extCode, param, clazz);
        IExtensionNodeRunner extensionNodeRunner = runners.stream().filter(r -> r.isSupport(extNode.getScriptFileName(), extNode.getScriptText()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("file : " + extNode.getScriptFileName() + " type not support"));
        ExtensionData<T> extensionData = extensionNodeRunner.run(extNode);
        if (extensionData.getCode() != 0) {
            throw new RuntimeException("run extension fail case : " + extensionData.getData());
        }
        return extensionData.getData();
    }



}
