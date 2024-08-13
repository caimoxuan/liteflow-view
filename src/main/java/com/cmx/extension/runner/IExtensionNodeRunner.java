package com.cmx.extension.runner;


import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;

public interface IExtensionNodeRunner {

    /**
     * 执行扩展点
     * @param node node
     * @return result
     * @param <D> res
     * @param <P> req
     */
    <D extends ExtensionData<?>, P extends ExtensionParam> D run(AbstractExtensionNode<D, P> node, ExtensionParam param);


    /**
     * 判断当前是否支持执行
     * @param fileName 文件名称
     * @param scriptType 脚本类型
     * @return true | false
     */
    boolean isSupport(String fileName, String scriptType);


    /**
     * 清除缓存
     * @param bizCode 业务code
     * @param extCode 扩展点code
     */
    void clearCache(String bizCode, String extCode);

}
