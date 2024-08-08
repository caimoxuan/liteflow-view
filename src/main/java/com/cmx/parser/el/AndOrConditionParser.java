package com.cmx.parser.el;

import cn.hutool.core.util.StrUtil;
import com.cmx.enums.ExpressParserEnum;
import com.cmx.model.CmpProperty;
import com.cmx.model.Properties;
import com.cmx.parser.base.AbstractAndOrNotExpressParser;
import com.yomahub.liteflow.common.ChainConstant;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.Node;
import com.yomahub.liteflow.flow.element.condition.AndOrCondition;
import com.yomahub.liteflow.flow.element.condition.BooleanConditionTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * AndOrConditionParser
 */
public class AndOrConditionParser extends AbstractAndOrNotExpressParser {

    @Override
    public ConditionTypeEnum parserType() {
        return ConditionTypeEnum.TYPE_AND_OR_OPT;
    }

    @Override
    public CmpProperty builderCondition(Condition condition) {
        return null;
    }

    @Override
    public List<CmpProperty> builderChildren(Condition condition) {
        List<CmpProperty> children = new ArrayList<>();

        AndOrCondition andOrCondition = (AndOrCondition) condition;
        List<Executable> andOrConditionItem = andOrCondition.getItem();
        andOrConditionItem.forEach(andOrItem -> {
            CmpProperty vo = null;
            if (andOrItem instanceof Condition) {
                // 这里解析器有: AndOrConditionParser、NotConditionParser
                vo = builderChildVO((Condition) andOrItem);
            } else if(andOrItem instanceof Node) {
                vo = Optional.of((Node) andOrItem).map(nodeMapper).orElse(new CmpProperty());
            }
            children.add(vo);
        });
        return children;
    }

    @Override
    public String generateELMethod(CmpProperty jsonEl) {
        String type = ExpressParserEnum.of(jsonEl.getType()).getType();
        if (ChainConstant.AND.equals(type)) {
            return elAndMethod;
        } else {
            return elOrMethod;
        }
    }

    @Override
    public String generateCondition(CmpProperty jsonEl, String elExpress) {
        // 填充EL条件, AND,OR没有条件, AND({})
        return elExpress;
    }

    @Override
    public String generateCmp(CmpProperty jsonEl, String elExpress) {
        List<CmpProperty> children = jsonEl.getChildren();
        // AND,OR的大小一定是2
        if (2 != children.size()) {
            return elExpress;
        }

        String nodeComponentId = "";
        for (CmpProperty vo : children) {
            nodeComponentId = generateNodeComponent(vo, nodeComponentId);
            // 这里会多拼接一个逗号
            nodeComponentId = StrUtil.appendIfMissing(nodeComponentId, elSeparate);
        }
        // 去除多的逗号
        nodeComponentId = StringUtils.substringBeforeLast(nodeComponentId, elSeparate);
        // 填充EL组件, AND({}) -> AND(a,b)
        return StrUtil.format(elExpress, nodeComponentId);
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
    public ExpressParserEnum getExpressType(Condition condition) {
        AndOrCondition andOrCondition = (AndOrCondition) condition;
        BooleanConditionTypeEnum booleanConditionType = andOrCondition.getBooleanConditionType();
        return ExpressParserEnum.of(booleanConditionType.name());
    }
}
