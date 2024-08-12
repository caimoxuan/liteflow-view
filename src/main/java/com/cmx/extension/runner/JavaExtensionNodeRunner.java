package com.cmx.extension.runner;

import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;
import org.apache.commons.lang.StringUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class JavaExtensionNodeRunner implements IExtensionNodeRunner {

    private static final String DEFAULT_METHOD = "execute";

    private static final Map<String, Object> instanceCache = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <D extends ExtensionData<?>, P extends ExtensionParam> D run(AbstractExtensionNode<D, P> node, ExtensionParam param) {
        String fileName = node.getScriptFileName();
        String cacheKey = param.getBizCode() + "_" + fileName;
        if (StringUtils.isNotBlank(node.getScriptText())) {
            try {
                if (instanceCache.get(cacheKey) != null) {
                    Object instance = instanceCache.get(cacheKey);
                    return (D) instance.getClass().getMethod(DEFAULT_METHOD).invoke(instance);
                }
                String localPath = System.getProperty("user.dir");
                File javaFile = new File(localPath + "/" + fileName);
                FileWriter writer = new FileWriter(javaFile);
                writer.write(node.getScriptText());
                writer.close();

                // Compile the .java file
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                compiler.run(null, null, null, javaFile.getPath());

                // Load the compiled class dynamically
                URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File("").toURI().toURL()});
                Class<?> dynamicClass = Class.forName(fileName.replace(".java", ""), true, classLoader);

                // Create an instance and invoke a method
                Object instance = dynamicClass.getDeclaredConstructor().newInstance();
                instanceCache.put(cacheKey, instance);
                return (D) dynamicClass.getMethod(DEFAULT_METHOD).invoke(instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public boolean isSupport(String fileName, String fileContent) {
        return fileName != null && fileName.endsWith(".java");
    }

}
