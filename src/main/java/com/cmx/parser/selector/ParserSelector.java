package com.cmx.parser.selector;

import com.cmx.parser.base.ExpressParser;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.cmx.parser.factory.ExpressParserFactory.PARSER_MAP;


/**
 * 解析器选择器
 */
public class ParserSelector {


    public static ExpressParser getParser(String type) {
        return foundMatchParserType(type).map(expressParserMapper).orElseThrow(RuntimeException::new);
    }

    public static <T extends Condition> ExpressParser getParser(T condition) {
        return foundMatchParserType(condition).map(expressParserMapper).orElseThrow(RuntimeException::new);
    }

    private static <T extends Condition> Optional<String> foundMatchParserType(T condition) {
        for (String key : PARSER_MAP.keySet()) {
            String parserType = condition.getConditionType().getType();
            if (keyMatcher.test(key, parserType)) {
                return Optional.of(key);
            } else if (andOrOptMatcher.test(parserType)) {
                return Optional.of(key);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> foundMatchParserType(String type) {
        for (String key : PARSER_MAP.keySet()) {
            if (keyMatcher.test(key, type)) {
                return Optional.of(key);
            } else if (andOrOptMatcher.test(type)) {
                return Optional.of(key);
            }
        }
        return Optional.empty();
    }

    private static final Function<String, ExpressParser> expressParserMapper = parserType -> Optional.ofNullable(parserType)
            .map(PARSER_MAP::get)
            .orElse(null);

    private static final BiPredicate<String, String> keyMatcher = StringUtils::equals;

    private static final Predicate<String> andOrOptMatcher =
            key -> StringUtils.contains(ConditionTypeEnum.TYPE_AND_OR_OPT.getType(), key);

}
