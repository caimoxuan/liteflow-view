package com.cmx.extension.loader;

import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.net.URL;
import java.util.*;

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
    /**
     * 空节点缓存
     */
    public static Set<String> emptyNodeLocalCache = new HashSet<>();

    /**
     * 设置空节点
     * @param loadKey key
     * @param extCode code
     */
    public static void cacheEmptyNode(String loadKey, String extCode) {
        emptyNodeLocalCache.add(loadKey + KEY_SPLIT + extCode);
    }

    /**
     * 清除当前缓存（脚本更新可用）
     * @param loadKey key
     * @param extCode code
     */
    public static void clearCache(String loadKey, String extCode) {
        emptyNodeLocalCache.remove(loadKey + KEY_SPLIT + extCode);
        extensionNodeLocalCache.remove(loadKey + KEY_SPLIT + extCode);
    }

    /**
     * 加载远程节点
     * @param loadKey key
     * @param extCode code
     * @param clazz class
     * @return node
     * @param <T> node return type
     */
    @SuppressWarnings("unchecked")
    public static <T> AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loaderRemoteExtension(String loadKey, String extCode, Class<? extends AbstractExtensionNode<ExtensionData<T>, ExtensionParam>> clazz) {
        String extensionCode = loadKey + KEY_SPLIT + extCode;
        // 判断空节点缓存
        if (emptyNodeLocalCache.contains(extensionCode)) {
            return null;
        }
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> extensionNode = (AbstractExtensionNode<ExtensionData<T>, ExtensionParam>) extensionNodeLocalCache.get(extensionCode);
        if (extensionNode != null) {
            return extensionNode;
        }
        AbstractExtensionRemoteLoader loader = ExtensionLoaderFactory.getLoader();
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loaderNode = loader.loadRemoteExtension(loadKey, extCode, clazz);
        extensionNodeLocalCache.put(extensionCode, loaderNode);
        return loaderNode;
    }

    /**
     * 加载本地节点
     * @param loadKey key
     * @param extCode code
     * @param clazz class
     * @return node
     * @param <T> node return type
     */
    @SuppressWarnings("unchecked")
    public static <T> AbstractExtensionNode<ExtensionData<T>, ExtensionParam> loaderLocalExtension(String loadKey, String extCode, Class<? extends AbstractExtensionNode<ExtensionData<T>, ExtensionParam>> clazz) {
        String extensionCode = loadKey + KEY_SPLIT + extCode;
        // 空节点缓存判断
        if (emptyNodeLocalCache.contains(extensionCode)) {
            return null;
        }
        AbstractExtensionNode<ExtensionData<T>, ExtensionParam> extensionNode = (AbstractExtensionNode<ExtensionData<T>, ExtensionParam>) extensionNodeLocalCache.get(extensionCode);
        if (extensionNode != null) {
            return extensionNode;
        }
        URL url = ResourceLoader.class.getResource(LOCAL_RESOURCE_DIR + loadKey);
        if (url != null) {
            File file = new File(url.getFile());
            if (!file.isDirectory()) {
                return null;
            }
            String[] list = file.list();
            if (list == null || list.length == 0) {
                return null;
            }
            Optional<String> first = Arrays.stream(list).filter(s -> s.startsWith(extCode)).findFirst();
            if (!first.isPresent()) {
                return null;
            }
            String fileName = first.get();
            try(InputStream resource = ResourceLoader.class.getResourceAsStream(LOCAL_RESOURCE_DIR + loadKey + PATH_SPLIT + first.get())) {
                if (resource == null) {
                    return null;
                }
                String fileContext = getFileContent(resource);
                if (StringUtils.isBlank(fileContext)) {
                    throw new RuntimeException("local extension file: " + fileName + "is empty");
                }
                extensionNode = clazz.getDeclaredConstructor().newInstance();
                extensionNode.setScriptFileName(fileName);
                extensionNode.setScriptText(fileContext);
                extensionNodeLocalCache.put(extensionCode, extensionNode);
                return extensionNode;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
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
