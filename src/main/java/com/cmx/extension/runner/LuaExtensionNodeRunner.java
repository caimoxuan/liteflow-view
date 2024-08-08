package com.cmx.extension.runner;

import com.cmx.extension.model.AbstractExtensionNode;
import com.cmx.extension.model.ExtensionData;
import com.cmx.extension.model.ExtensionParam;
import com.cmx.extension.register.LuaGlobalRegister;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class LuaExtensionNodeRunner implements IExtensionNodeRunner {

    private static final String PARAMETER_NAME = "param";

    private static final String CODE_NAME = "code";

    private static final String MESSAGE_NAME = "message";

    private static final String DATA_NAME = "data";


    @Override
    @SuppressWarnings("unchecked")
    public <D extends ExtensionData<?>, P extends ExtensionParam> D run(AbstractExtensionNode<D, P> node) {
        Globals luaGlobals = LuaGlobalRegister.getLuaGlobals();
        LuaValue load = luaGlobals.load(node.getScriptText());
        luaGlobals.set(PARAMETER_NAME, CoerceJavaToLua.coerce(node.getParam()));
        LuaValue res = load.call();
        ExtensionData<Object> extensionData = new ExtensionData<>();
        LuaValue code = res.get(CODE_NAME);
        if (!code.isint()) {
            throw new RuntimeException("illegal return code : " + code);
        }
        LuaValue message = res.get(MESSAGE_NAME);
        if (!message.isstring()) {
            throw new RuntimeException("illegal return message: " + message);
        }
        extensionData.setCode(code.checkint());
        extensionData.setMessage(message.checkjstring());
        LuaValue data = res.get(DATA_NAME);
        ParameterizedType actualTypeArgument = (ParameterizedType) ((ParameterizedType) node.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Class<?> resultDataType = (Class<?>)actualTypeArgument.getActualTypeArguments()[0];
        extensionData.setData(getTargetValue(data, resultDataType));
        return (D) extensionData;
    }

    @Override
    public boolean isSupport(String fileName, String fileContent) {
        return fileName != null && fileName.endsWith(".lua");
    }


    private Object getTargetValue(LuaValue value, Class<?> clazz) {
        if (value == null || value.isnil()) {
            return null;
        }
        Object basicValue = getBasicValue(value);
        if (basicValue != null) {
            return basicValue;
        }
        if (value.istable()) {
            try {
                Object obj = clazz.getDeclaredConstructor().newInstance();
                LuaTable table = value.checktable();
                Field[] fields = clazz.getDeclaredFields();
                for (Field f : fields) {
                    LuaValue luaValue = table.get(f.getName());
                    f.setAccessible(true);
                    if (luaValue.istable()) {
                        Class<?> aClass = f.getDeclaringClass();
                        if (aClass == clazz) {
                            throw new RuntimeException("can not support inner class or self object value");
                        }
                        f.set(obj, getTargetValue(luaValue, aClass));
                    } else {
                        f.set(obj, getTargetValue(luaValue, f.getDeclaringClass()));
                    }
                }
                return obj;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("not allow lua return value : " + value.typename());
    }



    /**
     * 获取基础数据信息
     * @param value value
     * @return basic data
     */
    private Object getBasicValue(LuaValue value) {
        String typeName = value.typename();
        switch (typeName) {
            case "boolean":
                return value.checkboolean();
            case "string":
                return value.checkjstring();
            case "number":
                if (value.isint()) {
                    return value.checkint();
                }
                return value.checkdouble();
            default:
                return null;
        }
    }

}
