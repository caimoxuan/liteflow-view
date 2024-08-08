package com.cmx.parser.el;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.cmx.enums.ExpressParserEnum;
import com.cmx.model.CmpProperty;
import com.cmx.model.Properties;
import com.cmx.parser.base.AbstractLoopExpressParser;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.Node;
import com.yomahub.liteflow.flow.element.condition.WhileCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * 循环解析
 */
public class WhileConditionParser extends AbstractLoopExpressParser {

    @Override
    public ConditionTypeEnum parserType() {
        return ConditionTypeEnum.TYPE_WHILE;
    }

    @Override
    public CmpProperty builderCondition(Condition condition) {
        WhileCondition whileCondition = (WhileCondition) condition;
        Executable whileItem = whileCondition.getWhileItem();
        CmpProperty vo = null;
        if (whileItem instanceof Condition) {
            // 这里解析器有: AndOrConditionParser、NotConditionParser
            vo = builderChildVO((Condition) whileItem);
        } else if(whileItem instanceof Node) {
            vo = Optional.of((Node) whileItem).map(nodeMapper).orElse(new CmpProperty());
        }
        return vo;
    }

    @Override
    public String generateELMethod(CmpProperty jsonEl) {
        return elWhileMethod;
    }

    @Override
    public String generateCondition(CmpProperty jsonEl, String elExpress) {
        if (Objects.isNull(jsonEl.getCondition())) {
            return elExpress;
        }
        CmpProperty condition = jsonEl.getCondition();

        String nodeComponentId = "";
        // 没有使用与或非表达式: AND,OR,NOT
        if (StringUtils.equals(NodeTypeEnum.IF.getMappingClazz().getSimpleName(), condition.getType())) {
            nodeComponentId = condition.getId();
        }
        // 使用与或非表达式: AND,OR,NOT
        else {
            nodeComponentId = generateNodeComponent(condition, nodeComponentId);
        }

        // WHILE({}).DO({}) -> WHILE(a).DO({})
        return StrUtil.replaceFirst(elExpress, "{}", nodeComponentId);
    }

    @Override
    public String generateCmp(CmpProperty jsonEl, String elExpress) {
        if (CollectionUtil.isEmpty(jsonEl.getChildren())) {
            return elExpress;
        }
        // 获取 DO({}) 内部的组件
        CmpProperty doExpressVO = nonBreakMapper(jsonEl.getChildren());
        // 生成 DO({}) 内部的表达式 -> THEN(b,c)
        String doEL = generateDoEL(doExpressVO);
        // 填充EL组件, WHILE(a).DO({}) -> WHILE(a).DO(THEN(b,c))
        return StrUtil.format(elExpress, doEL);
    }

    @Override
    public String generateIdAndTag(CmpProperty jsonEl, String elExpress) {
        Properties properties = jsonEl.getProperties();
        if (Objects.isNull(properties)) {
            return elExpress;
        }
        // 该表达式的 id 或者 tag
        String expressIdAndTag = "";
        if (StringUtils.isNotEmpty(properties.getId())) {
            expressIdAndTag = StrUtil.format(elExpressId, properties.getId());
        }
        if (StringUtils.isNotEmpty(properties.getTag())) {
            expressIdAndTag = expressIdAndTag + StrUtil.format(elExpressTag, properties.getTag());
        }
        return StrUtil.appendIfMissing(elExpress, expressIdAndTag);
    }

    @Override
    public String generateBreak(CmpProperty jsonEl, String elExpress) {
        if (Objects.isNull(jsonEl.getChildren()) || jsonEl.getChildren().isEmpty()) {
            return elExpress;
        }
        CmpProperty breakVO = breakMapper(jsonEl.getChildren());
        if (Objects.isNull(breakVO)) {
            return elExpress;
        }
        CmpProperty breakNode = foundBreakNode(breakVO.getChildren());
        if (Objects.isNull(breakNode)) {
            return elExpress;
        }
        // 循环跳出 BREAK 语句处理
        if (StringUtils.equals(NodeTypeEnum.BREAK.getMappingClazz().getSimpleName(), breakNode.getType())) {
            // .BREAK({}) -> .BREAK(d)
            String breakEL = StrUtil.format(elBreakMethod, breakNode.getId());
            // 补充 break 语句: FOR(a).DO(THEN(b,c)) -> FOR(a).DO(THEN(b,c)).BREAK(d)
            elExpress = elExpress + breakEL;
        }
        return elExpress;
    }

    @Override
    public ExpressParserEnum getExpressType(Condition condition) {
        return ExpressParserEnum.WHILE;
    }
}
