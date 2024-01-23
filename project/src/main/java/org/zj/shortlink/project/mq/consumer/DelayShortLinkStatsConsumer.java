package org.zj.shortlink.project.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.zj.shortlink.project.common.convention.exception.ServiceException;
import org.zj.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import org.zj.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import org.zj.shortlink.project.service.ShortLinkService;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static org.zj.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

/**
 * 延迟记录短链接统计组件
 * 继承 Spring 提供的 InitializingBean 并实现 afterPropertiesSet 方法，表示 Spring 容器加载后即执行 afterPropertiesSet 中的逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    public void onMessage() {
        // 单线程
        Executors.newSingleThreadExecutor(
                        // 自定义 ThreadFactory ，线程名更好看
                        runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setName("delay_short-link_stats_consumer");
                            thread.setDaemon(Boolean.TRUE);
                            return thread;
                        })
                .execute(() -> {
                    // 一个死循环的任务，负责消费延迟队列中的任务
                    RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
                    RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
                    for (; ; ) {
                        try {
                            // 从延迟队列中把没有持久化的监控数据持久化到数据库中
                            ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                            if (statsRecord != null) {
//                                shortLinkService.shortLinkStats(null, null, statsRecord);
                                if (!messageQueueIdempotentHandler.isMessageProcessed(statsRecord.getKeys())) {
                                    // 判断当前的这个消息流程是否执行完成
                                    if (messageQueueIdempotentHandler.isAccomplish(statsRecord.getKeys())) {
                                        return;
                                    }
                                    throw new ServiceException("消息未完成流程，需要消息队列重试");
                                }
                                try {
                                    shortLinkService.shortLinkStats(null, null, statsRecord);
                                } catch (Throwable ex) {
                                    messageQueueIdempotentHandler.delMessageProcessed(statsRecord.getKeys());
                                    log.error("延迟记录短链接监控消费异常", ex);
                                }
                                messageQueueIdempotentHandler.setAccomplish(statsRecord.getKeys());
                                continue;
                            }
                            // 如果延迟队列中没有任务则 park 当前线程 0.5 秒
                            LockSupport.parkUntil(500);
                        } catch (Throwable ignored) {
                        }
                    }
                });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }
}