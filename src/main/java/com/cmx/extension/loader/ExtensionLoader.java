package com.cmx.extension.loader;

import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.DefaultExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExtensionLoader {

    /**
     * 本地扩展点目录
     */
    private static final String LOCAL_RESOURCE_DIR = "/extension/";

    /**
     * 文件路径分隔符
     */
    private static final String PATH_SPLIT = "/";

    /**
     * 扩展点code分隔符
     */
    private static final String KEY_SPLIT = ":";

    /**
     * 扩展点本地缓存
     */
    public static Map<String, Object> extensionNodeLocalCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loaderRemoteExtension(String loadKey, String extCode, ExtensionParam param, Class<? extends AbstractExtensionNode<ExtensionData<T>, ExtensionParam>> clazz) {
        String extensionCode = loadKey + KEY_SPLIT + extCode;
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> extensionNode = (AbstractExtensionNode<ExtensionData<T>, ExtensionParam>) extensionNodeLocalCache.get(extensionCode);
        if (extensionNode != null) {
            return DefaultExtensionNode.newDefault(extensionNode, param);
        }
        AbstractExtensionRemoteLoader loader = ExtensionLoaderFactory.getLoader(param.getLoadKey());
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loaderNode = loader.loadRemoteExtension(loadKey, extCode, param, clazz);
        extensionNodeLocalCache.put(extensionCode, loaderNode);
        return DefaultExtensionNode.newDefault(loaderNode, param);
    }

    @SuppressWarnings("unchecked")
    public static <T> AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loaderLocalExtension(String loadKey, String extCode, ExtensionParam param, Class<? extends AbstractExtensionNode<ExtensionData<T>, ExtensionParam>> clazz) {
        String extensionCode = loadKey + KEY_SPLIT + extCode;
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> extensionNode = (AbstractExtensionNode<ExtensionData<T>, ExtensionParam>) extensionNodeLocalCache.get(extensionCode);
        if (extensionNode != null) {
            return DefaultExtensionNode.newDefault(extensionNode, param);
        }
        URL url = ResourceLoader.class.getResource(LOCAL_RESOURCE_DIR + loadKey);
        if (url != null) {
            File file = new File(url.getFile());
            if (!file.isDirectory()) {
                throw new RuntimeException("can not find local extension file directory");
            }
            String[] list = file.list();
            if (list == null || list.length == 0) {
                throw new RuntimeException("can not find local extension file: " + extCode);
            }
            Optional<String> first = Arrays.stream(list).filter(s -> s.startsWith(extCode)).findFirst();
            if (!first.isPresent()) {
                throw new RuntimeException("can not find local extension file: " + extCode);
            }
            String fileName = first.get();
            try(InputStream resource = ResourceLoader.class.getResourceAsStream(LOCAL_RESOURCE_DIR + loadKey + PATH_SPLIT + first.get())) {
                if (resource == null) {
                    throw new RuntimeException("can not find local extension file: " + fileName);
                }
                String fileContext = getFileContent(resource);
                if (StringUtils.isBlank(fileContext)) {
                    throw new RuntimeException("local extension file: " + fileName + "is empty");
                }
                extensionNode = clazz.getDeclaredConstructor().newInstance();
                extensionNode.setScriptFileName(fileName);
                extensionNode.setScriptText(fileContext);
                extensionNodeLocalCache.put(extensionCode, extensionNode);
                return DefaultExtensionNode.newDefault(extensionNode, param);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("can not find extension directory: " + loadKey);
        }
    }


    /**
     * 获取文件内容转string
     * @param stream inputStream
     * @return file content
     * @throws IOException io e
     */
    private static String getFileContent(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        }
        return builder.toString();
    }
}
