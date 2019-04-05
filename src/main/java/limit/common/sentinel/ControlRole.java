package limit.common.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.Collections;

/**
 * @Author: zhanglin
 * @Date: 2019/4/2
 * @Time: 8:25 PM
 */
public class ControlRole {
    private String KEY = "DEFAULT";

    public String getKEY() {
        return KEY;
    }

    public void setKEY(String KEY) {
        this.KEY = KEY;
    }

    public void initFlowControlRule(int bucketNum) {
        FlowRule rule = new FlowRule();
        rule.setResource(KEY);
        rule.setCount(bucketNum);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");

        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        rule.setMaxQueueingTimeMs(10 * 1000);
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }
}
