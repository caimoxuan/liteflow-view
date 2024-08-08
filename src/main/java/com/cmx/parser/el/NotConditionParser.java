package com.cmx.parser.el;

import cn.hutool.core.util.StrUtil;
import com.cmx.enums.ExpressParserEnum;
import com.cmx.model.CmpProperty;
import com.cmx.model.Properties;
import com.cmx.parser.base.AbstractAndOrNotExpressParser;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.Node;
import com.yomahub.liteflow.flow.element.condition.NotCondition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * NotConditionParser
 */
@Component
public class NotConditionParser extends AbstractAndOrNotExpressParser {

    @Override
    public ConditionTypeEnum parserType() {
        return ConditionTypeEnum.TYPE_NOT_OPT;
    }

    @Override
    public CmpProperty builderCondition(Condition condition) {
        return null;
    }

    @Override
    public List<CmpProperty> builderChildren(Condition condition) {
        List<CmpProperty> children = new ArrayList<>();

        NotCondition notCondition = (NotCondition) condition;
        Executable notConditionItem = notCondition.getItem();
        CmpProperty vo = null;
        if (notConditionItem instanceof Condition) {
            // 这里解析器有: AndOrConditionParser、NotConditionParser
            vo = builderChildVO((Condition) notConditionItem);
        } else if(notConditionItem instanceof Node) {
            vo = Optional.of((Node) notConditionItem).map(nodeMapper).orElse(new CmpProperty());
        }

        children.add(vo);
        return children;
    }

    @Override
    public String generateELMethod(CmpProperty jsonEl) {
        return elNotMethod;
    }

    @Override
    public String generateCondition(CmpProperty jsonEl, String elExpress) {
        // 填充EL条件, NOT没有条件, NOT({})
        return elExpress;
    }

    @Override
    public String generateCmp(CmpProperty jsonEl, String elExpress) {
        List<CmpProperty> children = jsonEl.getChildren();
        // NOT的大小一定是:1
        if (1 != children.size()) {
            return elExpress;
        }
        String nodeComponentId = "";
        nodeComponentId = generateNodeComponent(children.get(0), nodeComponentId);
        // 填充EL组件, NOT({}) -> NOT(a)
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
        return ExpressParserEnum.NOT;
    }
}
