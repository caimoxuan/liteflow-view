package com.cmx.parser.base;

import cn.hutool.core.util.StrUtil;

import com.cmx.enums.ExpressParserEnum;
import com.cmx.model.CmpProperty;
import com.cmx.model.Properties;
import com.cmx.parser.selector.ParserSelector;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.FallbackNode;
import com.yomahub.liteflow.flow.element.Node;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


/**
 * AbstractExpressParser
 *
 * @author <a href="mailto:dogsong99@163.com">dosong</a>
 * @since 2024/4/18
 */
public abstract class AbstractExpressParser implements ExpressParser {

    /** chain类型转换器 */
    protected final Function<Condition, String> typeMapper = condition -> Optional.ofNullable(condition)
            .map(Condition::getConditionType)
            .map(ConditionTypeEnum::getType)
            .map(String::toUpperCase)
            .orElse(StringUtils.EMPTY);


    // Node 转 CmpPropertyVO
    protected final Function<Node, CmpProperty> nodeMapper = node -> {
        if (FallbackNode.class.getSimpleName().equals(node.getClass().getSimpleName())) {
            FallbackNode fallbackNode = (FallbackNode) node;
            return CmpProperty.builder()
                    .id(fallbackNode.getExpectedNodeId())
                    .type(fallbackNode.getType().getCode())
                    .properties(getProperties(null, fallbackNode.getTag()))
                    .build();
        }
        return CmpProperty.builder()
                .id(node.getId())
                .type(node.getType().getMappingClazz().getSimpleName())
                .properties(getProperties(null, node.getTag()))
                .build();
    };

    // FallbackNode 转 CmpPropertyVO
    protected final Function<FallbackNode, CmpProperty> fallbackNodeMapper = node ->
            CmpProperty.builder()
                    .id(node.getExpectedNodeId())
                    .type(node.getType().getCode())
                    .properties(getProperties(null, node.getTag()))
                    .build();

    @Override
    public CmpProperty builderVO(Condition condition) {
        return CmpProperty.builder()
                .id(null)
                .type(this.getExpressType(condition).getType())
                .properties(getProperties(condition.getId(), condition.getTag()))
                .build();
    }

    public abstract ExpressParserEnum getExpressType(Condition condition);

    @Override
    public String generateELEnd(CmpProperty jsonEl, String elExpress) {
        return StrUtil.appendIfMissing(elExpress, elEnd);
    }

    protected Properties getProperties(String id, String tag) {
        if (null == id && null == tag) {
            return null;
        }
        String propertyId = getPropertyId(id);
        if (null == propertyId && null == tag) {
            return null;
        }
        return new Properties(propertyId, tag);
    }

    protected String getPropertyId(String propertyId) {
        // LF默认id: condition-switch
        if (StrUtil.equals(defaultConditionId(), propertyId)) {
            return null;
        } else {
            return propertyId;
        }
    }

    protected String defaultConditionId() {
       return StrUtil.format("condition-{}", this.parserType().getType().toLowerCase());
    }

    protected CmpProperty builderChildVO(Condition condition) {
        ExpressParser parser = ParserSelector.getParser(condition);
        // id, type, properties
        CmpProperty cmpProperty = parser.builderVO(condition);
        // condition
        cmpProperty.setCondition(parser.builderCondition(condition));
        // children
        cmpProperty.setChildren(parser.builderChildren(condition));
        return cmpProperty;
    }

    protected void builderChildList(List<Executable> executableList, List<CmpProperty> children) {
        if (CollectionUtils.isEmpty(executableList)) {
            return;
        }
        executableList.forEach(executable -> {
            CmpProperty vo = null;
            if (executable instanceof Condition) {
                vo = builderChildVO((Condition) executable);
            } else if(executable instanceof Node) {
                vo = Optional.of((Node) executable).map(nodeMapper).orElse(new CmpProperty());
            }
            children.add(vo);
        });
    }

    @Override
    public String builderEL(CmpProperty jsonEl) {
        String generateEL = abstractGenerateEL(jsonEl);
        ExpressParser parser = ParserSelector.getParser(jsonEl.getType().toLowerCase());
        // 5.补充分号 THEN(a, b, c).id("dog");
        generateEL = parser.generateELEnd(jsonEl, generateEL);

        return generateEL;
    }

    protected String abstractGenerateEL(CmpProperty jsonEl) {
        ExpressParser parser = ParserSelector.getParser(jsonEl.getType().toLowerCase());
        // 1.生成外部函数表达式 THEN({})
        String elExpress = parser.generateELMethod(jsonEl);
        // 2.填充EL条件, THEN没有条件, THEN(a, b, c)
        elExpress = parser.generateCondition(jsonEl, elExpress);
        // 3.填充EL组件 THEN(a, b, c)
        elExpress = parser.generateCmp(jsonEl, elExpress);
        // 4.拼接属性 THEN(a, b, c).id("dog")
        elExpress = parser.generateIdAndTag(jsonEl, elExpress);
        // 5.补充分号 THEN(a, b, c).id("dog");
        // elExpress = parser.generateELEnd(elExpress);
        return elExpress;
    }

    protected String generateNodeComponent(CmpProperty jsonEl, String nodeComponentId) {
        String id = jsonEl.getId();
        if (null == id) {
            // 说明是一个 condition
            nodeComponentId = nodeComponentId + abstractGenerateEL(jsonEl);
        } else {
            // 普通节点处理
            if (StringUtils.equals(NodeTypeEnum.COMMON.getMappingClazz().getSimpleName(), jsonEl.getType())) {
                // 节点组件标签处理 a.tag("dog")
                // String nodeIdAndTag = id + getELNodeTag(jsonEl);
                String nodeIdAndTag = StringUtils.appendIfMissing(id, getELNodeTag(jsonEl));
                nodeComponentId = nodeComponentId + nodeIdAndTag;
            }
            // 条件节点处理
            else if (StringUtils.equals(NodeTypeEnum.IF.getMappingClazz().getSimpleName(), jsonEl.getType())) {
                nodeComponentId = nodeComponentId + id;
            }
        }
        return nodeComponentId;
    }

    protected String getELNodeTag(CmpProperty jsonEl) {
        if (Objects.isNull(jsonEl.getProperties())) {
            return null;
        }
        Properties properties = jsonEl.getProperties();
        if (null == properties.getTag()) {
            return null;
        }
        return StrUtil.format(elNodeTag, properties.getTag());
    }

}
