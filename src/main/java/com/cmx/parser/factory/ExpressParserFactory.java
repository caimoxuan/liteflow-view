package com.cmx.parser.factory;

import com.cmx.parser.base.ExpressParser;
import com.cmx.parser.el.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析器工厂
 */
@Slf4j
public class ExpressParserFactory {

    /**
     * 解析器容器
     */
    public static final Map<String, ExpressParser> PARSER_MAP = new ConcurrentHashMap<>();

    private static void parsersRegister() {
        register(new AndOrConditionParser());
        register(new CatchConditionParser());
        register(new ForConditionParser());
        register(new IfConditionParser());
        register(new NotConditionParser());
        register(new ThenConditionParser());
        register(new SwitchConditionParser());
        register(new WhenConditionParser());
        register(new WhileConditionParser());
    }

    static {
        parsersRegister();
    }

    private static void register(ExpressParser parser) {
        if (parser.parserType() == null) {
            return;
        }
        Assert.notNull(parser, "ExpressParser parser must not be null");
        PARSER_MAP.put(parser.parserType().getType(), parser);
        log.info("ExpressParser[{}] has been found", parser.parserType());
    }


}
