package com.cmx.parser.el;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cmx.enums.ExpressParserEnum;
import com.cmx.model.CmpProperty;
import com.cmx.model.Properties;
import com.cmx.parser.base.AbstractExpressParser;
import com.yomahub.liteflow.common.ChainConstant;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.Node;
import com.yomahub.liteflow.flow.element.condition.IfCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * 判断解析
 */
public class IfConditionParser extends AbstractExpressParser {

    @Override
    public ConditionTypeEnum parserType() {
        return ConditionTypeEnum.TYPE_IF;
    }

    @Override
    public CmpProperty builderCondition(Condition condition) {
        IfCondition ifCondition = (IfCondition) condition;
        Executable ifItem = ifCondition.getIfItem();
        CmpProperty vo = null;
        if (ifItem instanceof Condition) {
            // 这里解析器有: AndConditionParser、OrConditionParser、NotConditionParser
             vo = builderChildVO((Condition) ifItem);
        } else if(ifItem instanceof Node) {
            vo = Optional.of((Node) ifItem).map(nodeMapper).orElse(new CmpProperty());
        }
        return vo;
    }

    @Override
    public List<CmpProperty> builderChildren(Condition condition) {
        List<CmpProperty> children = new ArrayList<>();

        IfCondition ifCondition = (IfCondition) condition;
        // 获得 trueCase
        Executable trueCaseExecutableItem = ifCondition.getTrueCaseExecutableItem();
        addChildList(trueCaseExecutableItem, children);

        // 获得 falseCase
        Executable falseCaseExecutableItem = ifCondition.getFalseCaseExecutableItem();
        addChildList(falseCaseExecutableItem, children);

        return children;
    }

    private void addChildList(Executable item, List<CmpProperty> children) {
        if (ObjectUtil.isNull(item)) {
            return;
        }
        // 可执行对象不为空，则去执行
        if (ObjectUtil.isNotNull(item)) {
            CmpProperty vo = null;
            if (item instanceof Condition) {
                // 这里解析器有: AndOrConditionParser、NotConditionParser
                 vo = builderChildVO((Condition) item);
            } else if (item instanceof Node) {
                vo = Optional.of((Node) item).map(nodeMapper).orElse(new CmpProperty());
            }
            children.add(vo);
        }
    }

    @Override
    public String generateELMethod(CmpProperty jsonEl) {
        return elIfMethod;
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

        // IF({},{}) -> IF(AND(a,b),{})
        return StrUtil.replaceFirst(elExpress, "{}", nodeComponentId);
    }

    @Override
    public String generateCmp(CmpProperty jsonEl, String elExpress) {
        if (CollectionUtil.isEmpty(jsonEl.getChildren()) || jsonEl.getChildren().isEmpty()) {
            return elExpress;
        }
        List<CmpProperty> children = jsonEl.getChildren();
        String caseNodeComponentId = "";
        // IF的二元表达式
        if (1 == children.size()) {
            CmpProperty trueCaseVO = children.get(0);
            // 获取 trueCase
            caseNodeComponentId = generateNodeComponent(trueCaseVO, caseNodeComponentId);
        }
        // IF的三元元表达式
        else if (2 == children.size()) {
            for (CmpProperty caseVO : children) {
                caseNodeComponentId = generateNodeComponent(caseVO, caseNodeComponentId);
                // 这里会多拼接一个逗号
                caseNodeComponentId = StrUtil.appendIfMissing(caseNodeComponentId, elSeparate);
            }
            // 去除多的逗号: c, d, -> c, d
            caseNodeComponentId = StringUtils.substringBeforeLast(caseNodeComponentId, elSeparate);
        }
        // IF(AND(a,b), {}) -> IF(AND(a,b), c, d)
        return StrUtil.format(elExpress, caseNodeComponentId);
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

    private CmpProperty andMapper(List<CmpProperty> voList) {
        return voList.stream()
                .filter(vo -> caseMatcher.test(vo.getType(), ChainConstant.AND))
                .findFirst()
                .orElse(null);
    }

    // 筛选 BREAK 类型  Matcher
    private final BiPredicate<String, String> caseMatcher = StringUtils::equals;

    @Override
    public ExpressParserEnum getExpressType(Condition condition) {
        return ExpressParserEnum.IF;
    }
}
