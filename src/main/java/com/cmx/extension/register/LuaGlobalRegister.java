package com.cmx.extension.register;

import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.event.Level;

import java.util.UUID;

@Slf4j
public class LuaGlobalRegister {

    private static final JseBaseLib jseBaseLib = new JseBaseLib();
    private static final MathLib mathLib = new MathLib();
    private static final OsLib osLib = new OsLib();
    private static final StringLib stringLib = new StringLib();
    private static final TableLib tableLib = new TableLib();


    public static Globals getLuaGlobals() {
        Globals globals = JsePlatform.standardGlobals();
        globals.load(jseBaseLib);
        globals.load(mathLib);
        globals.load(osLib);
        globals.load(stringLib);
        globals.load(tableLib);
        // 自定义库
        globals.load(new FuncRegister());
        return globals;
    }

    static class FuncRegister extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue luaValue, LuaValue env) {
            LuaValue table = new LuaTable(0, 30);
            table.set("getUUID", new IdGenFunc());
            table.set("logInfo", new LogFunc(Level.INFO));
            table.set("logWarn", new LogFunc(Level.WARN));
            table.set("logError", new LogFunc(Level.ERROR));
            env.set("GLOBAL", table);
            env.get("package").get("loaded").set("GLOBAL", table);
            return table;
        }
    }

    /**
     * 生成uuid
     */
    static class IdGenFunc extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            return LuaValue.valueOf(UUID.randomUUID().toString().replaceAll("-", ""));
        }
    }

    static class LogFunc extends TwoArgFunction {

        private Level levelType;

        public LogFunc(Level level) {
            this.levelType = level;
        }

        @Override
        public LuaValue call(LuaValue fmt, LuaValue param) {
            LuaTable table = param.checktable();
            int length = table.length();
            String[] arr = new String[length];
            for (int i = 0; i < length; i++) {
                arr[i] = table.get(i + 1).checkjstring();
            }
            switch (this.levelType) {
                case INFO:
                    log.info(fmt.checkjstring(), (Object[]) arr);
                    break;
                case WARN:
                    log.warn(fmt.checkjstring(), (Object[]) arr);
                    break;
                case ERROR:
                    log.error(fmt.checkjstring(), (Object[]) arr);
                    break;
                default:
                    break;
            }
            return LuaValue.valueOf(0);
        }
    }

}
