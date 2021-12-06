package limit.common.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
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

    public void initFlowControlRule(int bucketNum) {
        FlowRule rule = new FlowRule();
        rule.setResource(KEY);
        rule.setCount(bucketNum);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");
        //使用匀速排队限流
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        rule.setMaxQueueingTimeMs(10 * 1000);
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    public static void doLimitSentinel(String name) {
        Entry entry = null;
        try {
            ContextUtil.enter(name);
            entry = SphU.entry(name, EntryType.OUT);

            // Your business logic here.
        } catch (BlockException ex) {
            // Blocked.
            System.out.printf("[%d] Blocked!\n", System.currentTimeMillis());
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }
}
