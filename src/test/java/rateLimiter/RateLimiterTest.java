package rateLimiter;

import com.google.common.util.concurrent.RateLimiter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RateLimiterTest {
    //Guava有两种限流模式，一种为稳定模式(SmoothBursty:令牌生成速度恒定)，一种为渐进模式(SmoothWarmingUp:令牌生成速度缓慢提升直到维持在一个稳定值)
//两种模式实现思路类似，主要区别在等待时间的计算上，本篇重点介绍SmoothBursty
    public static void main(String[] args) {
        //每秒限制访问5次
        final RateLimiter rateLimiter = RateLimiter.create(5.0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.now();
        System.out.println("当前时间:" + startDate.format(f));
        for (int i = 0; i < 10; i++) {
            rateLimiter.acquire();
            // 每秒打印两次
            System.out.println(i);
        }
        LocalDateTime endDate = LocalDateTime.now();
        System.out.println("当前时间:" + endDate.format(f));
    }

}