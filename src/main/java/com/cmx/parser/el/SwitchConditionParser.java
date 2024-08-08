package com.cmx.parser.el;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.cmx.enums.ExpressParserEnum;
import com.cmx.model.CmpProperty;
import com.cmx.model.Properties;
import com.cmx.parser.base.AbstractExpressParser;
import com.yomahub.liteflow.common.ChainConstant;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.Node;
import com.yomahub.liteflow.flow.element.condition.SwitchCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 选择解析
 */
public class SwitchConditionParser extends AbstractExpressParser {

    private final String type = ConditionTypeEnum.TYPE_SWITCH.getType();

    /**
     *
     * @see ChainConstant
     * @see ConditionTypeEnum
     * */
    @Override
    public ConditionTypeEnum parserType() {
        return ConditionTypeEnum.TYPE_SWITCH;
    }

    @Override
    public CmpProperty builderCondition(Condition condition) {
        SwitchCondition switchCondition = (SwitchCondition) condition;
        Node switchNode = switchCondition.getSwitchNode();

        // Node 转 CmpPropertyVO
        return Optional.of(switchNode).map(nodeMapper).orElse(new CmpProperty());
    }

    @Override
    public List<CmpProperty> builderChildren(Condition condition) {
        List<CmpProperty> children = new ArrayList<>();
        SwitchCondition switchCondition = (SwitchCondition) condition;
        List<Executable> executableList = switchCondition.getTargetList();
        executableList.forEach(executable -> {
            CmpProperty vo = null;
            if (executable instanceof Condition) {
                vo = builderChildVO((Condition) executable);
            } else if(executable instanceof Node) {
                vo = Optional.of((Node) executable).map(nodeMapper).orElse(new CmpProperty());
            }
            children.add(vo);
        });
        return children;
    }

    @Override
    public String generateELMethod(CmpProperty jsonEl) {
        return elSwitchMethod;
    }

    @Override
    public String generateCondition(CmpProperty jsonEl, String elExpress) {
        if (Objects.isNull(jsonEl.getCondition())) {
            return elExpress;
        }
        CmpProperty condition = jsonEl.getCondition();
        return StrUtil.replaceFirst(elExpress, "{}", condition.getId());
    }

    @Override
    public String generateCmp(CmpProperty jsonEl, String elExpress) {
        if (CollectionUtil.isNotEmpty(jsonEl.getChildren())) {
            String nodeComponentId = "";
            for (CmpProperty child : jsonEl.getChildren()) {
                nodeComponentId = generateNodeComponent(child, nodeComponentId);
                // 这里会多拼接一个逗号
                nodeComponentId = StrUtil.appendIfMissing(nodeComponentId, elSeparate);
            }
            // 去除多的逗号
            nodeComponentId = StringUtils.substringBeforeLast(nodeComponentId, elSeparate);
            // 填充EL组件, SWITCH(a).to({}) -> SWITCH(a).to(b, c)
            elExpress = StrUtil.format(elExpress, nodeComponentId);
        }
        return elExpress;
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

    private boolean nonLiteFlowConditionId(String id) {
        // LF默认id: condition-switch
        return !StringUtils.equals(defaultConditionId(), id);
    }

    @Override
    public ExpressParserEnum getExpressType(Condition condition) {
        return ExpressParserEnum.SWITCH;
    }
}
