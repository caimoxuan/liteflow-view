package com.cmx.parser.generator;

import com.cmx.model.CmpProperty;
import com.cmx.model.ELInfo;
import com.cmx.parser.base.ExpressParser;
import com.cmx.parser.selector.ParserSelector;
import com.ql.util.express.DefaultContext;
import com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder;
import com.yomahub.liteflow.common.ChainConstant;
import com.yomahub.liteflow.flow.FlowBus;
import com.yomahub.liteflow.flow.element.Condition;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder.EXPRESS_RUNNER;

/**
 * ExpressBuilder
 */
@Slf4j
public final class ExpressGenerator {

    public static CmpProperty generateJsonEL(ELInfo elInfo) {
        CmpProperty cmpProperty = new CmpProperty();
        if (elInfo == null) {
            return cmpProperty;
        }
        try {
            DefaultContext<String, Object> context = new DefaultContext<>();
            // 这里一定要先放chain，再放node，因为node优先于chain，所以当重名时，node会覆盖掉chain
            // 往上下文里放入所有的chain，是的el表达式可以直接引用到chain
            FlowBus.getChainMap().values().forEach(chain -> context.put(chain.getChainId(), chain));
            // 往上下文里放入所有的node，使得el表达式可以直接引用到nodeId
            FlowBus.getNodeMap().keySet().forEach(nodeId -> context.put(nodeId, FlowBus.getNode(nodeId)));
            context.put(ChainConstant.CURR_CHAIN_ID, elInfo.getChainId());
            List<String> errorList = new ArrayList<>();
            // promotionChain: THEN(fullCutCmp, fullDiscountCmp, rushBuyCmp);
            String elStr = elInfo.getElStr();
            log.debug("generate el source str : {}", elStr);
            Condition condition = (Condition) EXPRESS_RUNNER.execute(elStr, context, errorList, true, true);
            // 设置最 外层 内层, 其实每一层都是这样的
            // 1.id, condition是没有组件编码的,只有Node的时候才有
            // 2.type格式: THEN,SWITCH,IF,WHEN,FOR,WHILE,CATCH
            // 3.properties: id, tag 只有condition才有的属性, Node没有
            // 4.condition, 根据类型来区分, 比如:THEN、WHEN就没有条件
            // 5.children
            cmpProperty = builderJsonEL(condition);
        } catch (Exception ex) {
            log.error("generateJsonEL got ex: ", ex);
            throw new RuntimeException(ex);
        }
        return cmpProperty;
    }

    public static CmpProperty builderJsonEL(Condition condition) {
        ExpressParser parser = ParserSelector.getParser(condition);
        // id, type, property
        CmpProperty cmpProperty = parser.builderVO(condition);
        // conditionList
        cmpProperty.setCondition(parser.builderCondition(condition));
        // childList
        cmpProperty.setChildren(parser.builderChildren(condition));
        return cmpProperty;
    }

    public static ELInfo generateEL(CmpProperty jsonEl) {
        ELInfo vo = new ELInfo();
        String elStr = builderEL(jsonEl);

        vo.setElStr(elStr);
        return vo;
    }

    public static boolean verifyELExpression(CmpProperty jsonEl) {
        String elStr = builderEL(jsonEl);
        if (null == elStr) {
            return false;
        }
        log.info("verifyELExpression got elStr: {}", elStr);
        return LiteFlowChainELBuilder.validate(elStr);
    }

    public static String builderEL(CmpProperty jsonEl) {
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
        elExpress = parser.generateELEnd(jsonEl, elExpress);
        return elExpress;
    }


}
