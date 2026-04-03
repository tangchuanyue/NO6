package com.cl.task;

import com.cl.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 通知定时任务
 * 定时处理待发送和发送失败的通知
 */
@Component
public class NotificationTask {

    @Autowired
    private NotificationService notificationService;
    
    /**
     * 每5分钟处理一次待发送的通知
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void processPendingNotifications() {
        notificationService.processPendingNotifications();
    }
    
    /**
     * 每10分钟重试一次发送失败的通知
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void processFailedNotifications() {
        notificationService.processFailedNotifications();
    }
}
