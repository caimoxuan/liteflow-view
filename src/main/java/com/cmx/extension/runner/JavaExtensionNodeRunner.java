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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaExtensionNodeRunner implements IExtensionNodeRunner {

    private static final String DEFAULT_METHOD = "execute";

    private static final Map<String, Object> instanceCache = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <D extends ExtensionData<?>, P extends ExtensionParam> D run(AbstractExtensionNode<D, P> node, ExtensionParam param) {
        String fileName = node.getScriptFileName();
        String cacheKey = param.getBizCode() + "_" + param.getExtCode();
        if (StringUtils.isNotBlank(node.getScriptText())) {
            try {
                if (instanceCache.get(cacheKey) != null) {
                    Object instance = instanceCache.get(cacheKey);
                    return (D) instance.getClass().getMethod(DEFAULT_METHOD, ExtensionParam.class).invoke(instance, param);
                }
                List<String> classNames = getClassNames(node.getScriptText());
                if (classNames.size() != 1) {
                    throw new RuntimeException("only one class support, but find " + classNames.size());
                }
                String className = classNames.get(0);
                if (fileName == null || fileName.isEmpty()) {
                    fileName = classNames.get(0) + ".java";
                }
                if (!fileName.startsWith(className)) {
                    throw new RuntimeException("java file should named by class name");
                }
                String localPath = System.getProperty("user.dir");
                File javaFile = new File(localPath + "/" + fileName);
                Class<?> dynamicClass = compileToInstance(javaFile, node.getScriptText(), className);
               // Create an instance and invoke a method
                Object instance = dynamicClass.getDeclaredConstructor().newInstance();
                instanceCache.put(cacheKey, instance);
                return (D) dynamicClass.getMethod(DEFAULT_METHOD, ExtensionParam.class).invoke(instance, param);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public boolean isSupport(String fileName, String scriptType) {
        return "java".equals(scriptType) || (fileName != null && fileName.endsWith(".java"));
    }

    @Override
    public void clearCache(String bizCode, String extCode) {
        instanceCache.remove(bizCode + "_" + extCode);
    }

    @Override
    public void valid(String script) {
        String localPath = System.getProperty("user.dir");
        List<String> classNames = getClassNames(script);
        if (classNames.size() != 1) {
            throw new RuntimeException("only one class support, but find " + classNames.size());
        }
        String className = classNames.get(0);
        String fileName = className + ".java";
        File javaFile = null;
        try {
            javaFile = new File(localPath + "/" + fileName);
            Class<?> dynamicClass = compileToInstance(javaFile, script, className);
            Object instance = dynamicClass.getDeclaredConstructor().newInstance();
            dynamicClass.getMethod(DEFAULT_METHOD, ExtensionParam.class).invoke(instance, new ExtensionParam());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (javaFile != null) {
                javaFile.delete();
            }
        }
    }

    /**
     * 从脚本中获取类名称
     * @param javaSourceCode java code
     * @return className
     */
    private List<String> getClassNames(String javaSourceCode) {
        List<String> classNames = new ArrayList<>();
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(javaSourceCode);

        while (classMatcher.find()) {
            classNames.add(classMatcher.group(1));
        }

        return classNames;
    }

    /**
     * 将代码文编编译并加载
     * @param javaFile file
     * @param script code
     * @param className class
     * @return obj
     * @throws Exception e
     */
    private Class<?> compileToInstance(File javaFile, String script, String className) throws Exception {
        FileWriter writer = new FileWriter(javaFile);
        writer.write(script);
        writer.close();
        // 动态编译.java文件
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int compileResult = compiler.run(null, null, null, javaFile.getPath());
        if (compileResult != 0) {
            throw  new RuntimeException("file compile fail");
        }
        // 加载并执行.class文件中的代码
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File(".").toURI().toURL()});
        return Class.forName(className, true, classLoader);
    }

}
