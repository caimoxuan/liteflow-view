package com.cmx.parser.base;

import com.cmx.model.CmpProperty;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;

import java.util.List;

public interface ExpressParser {


    ConditionTypeEnum parserType();

    CmpProperty buildVO(Condition condition);

    CmpProperty builderCondition(Condition condition);

    List<CmpProperty> builderChildren(Condition condition);

}
