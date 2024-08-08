package com.cmx.extension.register;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.UUID;

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


}
