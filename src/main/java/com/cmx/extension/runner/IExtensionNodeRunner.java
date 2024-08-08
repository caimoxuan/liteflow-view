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
    <D extends ExtensionData<?>, P extends ExtensionParam> D run(AbstractExtensionNode<D, P> node);


    /**
     * 判断当前是否支持执行
     * @param fileName 文件名称
     * @param fileContent 文件内容
     * @return true | false
     */
    boolean isSupport(String fileName, String fileContent);

}
