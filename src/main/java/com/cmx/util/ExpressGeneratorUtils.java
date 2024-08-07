package com.cmx.util;

import com.cmx.model.CmpProperty;
import com.cmx.model.ELInfo;
import com.ql.util.express.DefaultContext;
import com.yomahub.liteflow.common.ChainConstant;
import com.yomahub.liteflow.flow.FlowBus;
import com.yomahub.liteflow.flow.element.Condition;

import java.util.ArrayList;
import java.util.List;

import static com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder.EXPRESS_RUNNER;

public final class ExpressGeneratorUtils {

    public static CmpProperty generateJsonEL(ELInfo elInfo) {
        CmpProperty cmpProperty = new CmpProperty();
        if (elInfo == null) {
            return cmpProperty;
        }
        try {
            DefaultContext<String, Object> context = new DefaultContext<>();

            FlowBus.getChainMap().values().forEach(chain -> context.put(chain.getChainId(), chain));
            FlowBus.getNodeMap().values().forEach(node -> context.put(node.getId(), node));
            // 当前chain
            context.put(ChainConstant.CURR_CHAIN_ID, elInfo.getChainId());
            List<String> errList = new ArrayList<>();
            String elStr = elInfo.getElStr();
            Condition condition = (Condition) EXPRESS_RUNNER.execute(elStr, context, errList, false, false);
            cmpProperty = buildJsonEL(condition);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cmpProperty;
    }


    public static CmpProperty buildJsonEL(Condition condition) {
        return null;
    }


}
