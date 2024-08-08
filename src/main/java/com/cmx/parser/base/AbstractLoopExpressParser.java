package com.cmx.parser.base;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cmx.model.CmpProperty;
import com.yomahub.liteflow.common.ChainConstant;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.Node;
import com.yomahub.liteflow.flow.element.condition.ConditionKey;
import com.yomahub.liteflow.flow.element.condition.LoopCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiPredicate;


/**
 * 循环Condition的抽象类
 * 主要继承对象有: ForConditionParser,WhileConditionParser,(暂时没有)IteratorConditionParser
 */
public abstract class AbstractLoopExpressParser extends AbstractExpressParser {

    private Map<String, List<Executable>> executableGroup = new HashMap<>();

    @Override
    public List<CmpProperty> builderChildren(Condition condition) {
        List<CmpProperty> children = new ArrayList<>();
        LoopCondition loopCondition = (LoopCondition) condition;
        this.executableGroup = loopCondition.getExecutableGroup();

        // 获得要循环的可执行对象
        Executable executableItem = this.getDoExecutor();
        // 可执行对象不为空，则去执行
        if (ObjectUtil.isNotNull(executableItem)) {
            if (executableItem instanceof Condition) {
                CmpProperty vo = builderChildVO((Condition) executableItem);
                children.add(vo);
            }
        }

        // 获取Break节点
        Executable breakItem = this.getBreakItem();
        // 如果break组件不为空，则去执行
        if (ObjectUtil.isNotNull(breakItem)) {
            if (breakItem instanceof Node) {
                // type: BREAK
                CmpProperty breakVO = new CmpProperty();
                breakVO.setType(ChainConstant.BREAK);
                List<CmpProperty> breakChildren = new ArrayList<>();
                CmpProperty vo = Optional.of((Node) breakItem).map(nodeMapper).orElse(new CmpProperty());
                breakChildren.add(vo);
                breakVO.setChildren(breakChildren);

                children.add(breakVO);
            }
        }
        return children;
    }

    protected Executable getBreakItem() {
        return this.getExecutableOne(ConditionKey.BREAK_KEY);
    }

    protected Executable getDoExecutor() {
        return this.getExecutableOne(ConditionKey.DO_KEY);
    }

    protected Executable getExecutableOne(String groupKey) {
        List<Executable> list = getExecutableList(groupKey);
        if (CollUtil.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    protected List<Executable> getExecutableList(String groupKey) {
        List<Executable> executableList = this.executableGroup.get(groupKey);
        if (CollUtil.isEmpty(executableList)) {
            executableList = new ArrayList<>();
        }
        return executableList;
    }

    @Override
    public String generateELEnd(CmpProperty jsonEl, String elExpress) {
        elExpress = generateBreak(jsonEl, elExpress);
        return StrUtil.appendIfMissing(elExpress, elEnd);
    }

    protected String generateDoEL(CmpProperty doExpressVO) {
        return abstractGenerateEL(doExpressVO);
    }

    protected CmpProperty breakMapper(List<CmpProperty> voList) {
        return voList.stream()
                .filter(vo -> breakMatcher.test(vo.getType(), ChainConstant.BREAK))
                .findFirst()
                .orElse(null);
    }

    protected CmpProperty nonBreakMapper(List<CmpProperty> voList) {
        return voList.stream()
                .filter(vo -> !breakMatcher.test(vo.getType(), ChainConstant.BREAK))
                .findFirst()
                .orElse(null);
    }

    // 筛选 BREAK 类型  Matcher
    protected final BiPredicate<String, String> breakMatcher = StringUtils::equals;


    public abstract String generateBreak(CmpProperty jsonEl, String elExpress);

    protected CmpProperty foundBreakNode(List<CmpProperty> children) {
        if (CollUtil.isEmpty(children)) {
            return null;
        } else {
            return children.get(0);
        }
    }
}
