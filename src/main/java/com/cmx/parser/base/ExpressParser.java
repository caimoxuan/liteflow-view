package com.cmx.parser.base;

import com.cmx.model.CmpProperty;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.flow.element.Condition;

import java.util.List;

public interface ExpressParser {

    String elSeparate = ",";

    String elPlaceholder = "{}";

    String elExpressId = ".id(\"{}\")";

    String elExpressTag = ".tag(\"{}\")";

    String elNodeTag = ".tag(\"{}\")";

    String elEnd = ";";

    String elThenMethod = "THEN({})";

    String elWhenMethod = "WHEN({})";

    String elIfMethod = "IF({},{})";

    String elCatchMethod = "CATCH({})";

    String elCatchDoMethod = "CATCH({}).DO({})";

    String elAndMethod = "AND({})";

    String elOrMethod = "OR({})";

    String elNotMethod = "NOT({})";

    String elSwitchMethod = "SWITCH({}).to({})";

    String elForMethod = "FOR({}).DO({})";

    String elWhileMethod = "WHILE({}).DO({})";

    String elBreakMethod = ".BREAK({})";


    /**
     *
     * @see ConditionTypeEnum
     * */
    ConditionTypeEnum parserType();

    /**
     * 构造VO
     */
    CmpProperty builderVO(Condition condition);

    CmpProperty builderCondition(Condition condition);

    List<CmpProperty> builderChildren(Condition condition);

    String builderEL(CmpProperty jsonEl);

    String generateELMethod(CmpProperty jsonEl);

    String generateCondition(CmpProperty jsonEl, String elExpress);

    String generateCmp(CmpProperty jsonEl, String elExpress);

    String generateIdAndTag(CmpProperty jsonEl, String elExpress);

    String generateELEnd(CmpProperty jsonEl, String elExpress);

}
