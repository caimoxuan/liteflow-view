package com.cmx.parser.el;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.cmx.enums.ExpressParserEnum;
import com.cmx.model.CmpProperty;
import com.cmx.model.Properties;
import com.cmx.parser.base.AbstractExpressParser;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.Node;
import com.yomahub.liteflow.flow.element.condition.CatchCondition;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * CATCH().DO()、CATCH() 解析器
 */
public class CatchConditionParser extends AbstractExpressParser {

    @Override
    public ConditionTypeEnum parserType() {
        return ConditionTypeEnum.TYPE_CATCH;
    }

    @Override
    public ExpressParserEnum getExpressType(Condition condition) {
        return ExpressParserEnum.CATCH;
    }

    @Override
    public CmpProperty builderCondition(Condition condition) {
        CatchCondition catchCondition = (CatchCondition) condition;
        Executable catchItem = catchCondition.getCatchItem();
        CmpProperty vo = null;
        if (catchItem instanceof Condition) {
            vo = builderChildVO((Condition) catchItem);
        } else if(catchItem instanceof Node) {
            vo = Optional.of((Node) catchItem).map(nodeMapper).orElse(new CmpProperty());
        }
        return vo;
    }

    @Override
    public List<CmpProperty> builderChildren(Condition condition) {
        CatchCondition catchCondition = (CatchCondition) condition;
        // 获取 DO 中内容,只有两种可能:
        // 1. DO 为空
        // 2. 一个 node 或者 一个 condition
        Executable catchItem = catchCondition.getDoItem();
        if (Objects.isNull(catchItem)) {
            return Collections.emptyList();
        }
        CmpProperty vo = null;
        if (catchItem instanceof Condition) {
            vo = builderChildVO((Condition) catchItem);
        } else if(catchItem instanceof Node) {
            vo = Optional.of((Node) catchItem).map(nodeMapper).orElse(new CmpProperty());
        }
        return Collections.singletonList(vo);
    }

    @Override
    public String generateELMethod(CmpProperty jsonEl) {
        if (null == jsonEl.getChildren() || jsonEl.getChildren().isEmpty()) {
            return elCatchMethod;
        }
        return elCatchDoMethod;
    }

    @Override
    public String generateCondition(CmpProperty jsonEl, String elExpress) {
        if (Objects.isNull(jsonEl.getCondition())) {
            return elExpress;
        }
        String catchInternalExpressions = Strings.EMPTY;
        // CATCH内部表达式
        catchInternalExpressions = generateNodeComponent(jsonEl.getCondition(), catchInternalExpressions);
        // CATCH({}).DO({}) -> CATCH(THEN(a,b)).DO({})
        return StrUtil.replaceFirst(elExpress, "{}", catchInternalExpressions);
    }

    @Override
    public String generateCmp(CmpProperty jsonEl, String elExpress) {
        if (CollectionUtil.isEmpty(jsonEl.getChildren())) {
            return elExpress;
        }
        // DO({}) 中只会有一个元素,即 children.size()=0
        CmpProperty doItem = jsonEl.getChildren().get(0);
        String doExpressions = Strings.EMPTY;
        // CATCH内部表达式
        doExpressions = generateNodeComponent(doItem, doExpressions);
        // CATCH(THEN(a,b)).DO({}) -> CATCH(THEN(a,b)).DO(c)
        // CATCH(THEN(a,b)).DO({}) -> CATCH(THEN(a,b)).DO(THEN(c,d))
        return StrUtil.format(elExpress, doExpressions);
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
}
