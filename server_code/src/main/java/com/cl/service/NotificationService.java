package com.cl.service;

import com.cl.entity.YishengyuyueEntity;
import com.cl.entity.TongzhijiluEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 通知服务类
 * 处理预约相关的所有通知逻辑
 */
@Service
public class NotificationService {

    @Autowired
    private TongzhijiluService tongzhijiluService;
    
    @Autowired
    private JiuzhentongzhiService jiuzhentongzhiService;
    
    private static final int MAX_RETRY_COUNT = 3;
    
    /**
     * 通知类型常量
     */
    public static final int NOTIFY_TYPE_APPOINTMENT_SUCCESS = 1; // 预约成功通知
    public static final int NOTIFY_TYPE_ONE_DAY_BEFORE = 2;      // 就诊前一天提醒
    public static final int NOTIFY_TYPE_ON_DAY = 3;              // 就诊当天提醒
    
    /**
     * 发送状态常量
     */
    public static final int SEND_STATUS_PENDING = 0;   // 待发送
    public static final int SEND_STATUS_SUCCESS = 1;   // 发送成功
    public static final int SEND_STATUS_FAILED = 2;    // 发送失败
    
    /**
     * 接收状态常量
     */
    public static final int RECEIVE_STATUS_UNRECEIVED = 0;  // 未接收
    public static final int RECEIVE_STATUS_RECEIVED = 1;    // 已接收
    public static final int RECEIVE_STATUS_READ = 2;        // 已读
    
    /**
     * 预约审核通过后立即发送所有后续通知
     * @param yuyue 预约信息
     */
    @Transactional
    public void sendAllNotificationsAfterApproval(YishengyuyueEntity yuyue) {
        if (yuyue == null || yuyue.getYuyueshijian() == null) {
            return;
        }
        
        Date appointmentTime = yuyue.getYuyueshijian();
        Date now = new Date();
        
        // 1. 立即发送预约成功通知
        createAndSendNotification(yuyue, NOTIFY_TYPE_APPOINTMENT_SUCCESS, 
            "预约成功通知", 
            "您的预约已成功！预约时间：" + formatDate(appointmentTime), 
            now);
        
        // 2. 创建就诊前一天提醒（预约成功后立即创建记录，按计划时间显示）
        Date oneDayBefore = getOneDayBefore(appointmentTime);
        if (oneDayBefore.after(now)) {
            createNotificationRecord(yuyue, NOTIFY_TYPE_ONE_DAY_BEFORE,
                "就诊前一天提醒",
                "提醒您：明天有预约就诊，请准时到达。预约时间：" + formatDate(appointmentTime),
                oneDayBefore);
        }
        
        // 3. 创建就诊当天提醒（预约成功后立即创建记录，按计划时间显示）
        Date onDay = getOnDayMorning(appointmentTime);
        if (onDay.after(now)) {
            createNotificationRecord(yuyue, NOTIFY_TYPE_ON_DAY,
                "就诊当天提醒",
                "提醒您：今天有预约就诊，请准时到达。预约时间：" + formatDate(appointmentTime),
                onDay);
        }
    }
    
    /**
     * 创建并立即发送通知
     */
    private void createAndSendNotification(YishengyuyueEntity yuyue, int type, 
            String typeName, String content, Date sendTime) {
        TongzhijiluEntity record = new TongzhijiluEntity();
        record.setYuyuebianhao(yuyue.getYuyuebianhao());
        record.setTongzhileixing(type);
        record.setTongzhileixingmingcheng(typeName);
        record.setZhanghao(yuyue.getZhanghao());
        record.setYishengzhanghao(yuyue.getYishengzhanghao());
        record.setTongzhineirong(content);
        record.setJihuafasongshijian(sendTime);
        record.setChongshicishu(0);
        record.setZuidachongshicishu(MAX_RETRY_COUNT);
        record.setJieshouzhuangtai(RECEIVE_STATUS_UNRECEIVED);
        
        // 立即发送
        boolean success = sendNotification(record);
        
        if (success) {
            record.setFasongzhuangtai(SEND_STATUS_SUCCESS);
            record.setFasongshijian(new Date());
        } else {
            record.setFasongzhuangtai(SEND_STATUS_FAILED);
            record.setShibaiyuanyin("首次发送失败");
        }
        
        tongzhijiluService.insert(record);
    }
    
    /**
     * 创建通知记录（不立即发送，按计划时间）
     */
    private void createNotificationRecord(YishengyuyueEntity yuyue, int type,
            String typeName, String content, Date planSendTime) {
        TongzhijiluEntity record = new TongzhijiluEntity();
        record.setYuyuebianhao(yuyue.getYuyuebianhao());
        record.setTongzhileixing(type);
        record.setTongzhileixingmingcheng(typeName);
        record.setZhanghao(yuyue.getZhanghao());
        record.setYishengzhanghao(yuyue.getYishengzhanghao());
        record.setTongzhineirong(content);
        record.setFasongzhuangtai(SEND_STATUS_PENDING);
        record.setJihuafasongshijian(planSendTime);
        record.setChongshicishu(0);
        record.setZuidachongshicishu(MAX_RETRY_COUNT);
        record.setJieshouzhuangtai(RECEIVE_STATUS_UNRECEIVED);
        
        tongzhijiluService.insert(record);
    }
    
    /**
     * 发送通知的实际方法
     */
    private boolean sendNotification(TongzhijiluEntity record) {
        try {
            // 这里可以实现实际的通知发送逻辑
            // 例如：发送短信、推送通知、站内信等
            
            // 模拟发送过程
            Thread.sleep(50);
            
            // 同时创建就诊通知记录（用于前端展示）
            com.cl.entity.JiuzhentongzhiEntity jiuzhentongzhi = 
                new com.cl.entity.JiuzhentongzhiEntity();
            jiuzhentongzhi.setTongzhibianhao(System.currentTimeMillis() + "");
            jiuzhentongzhi.setYishengzhanghao(record.getYishengzhanghao());
            jiuzhentongzhi.setZhanghao(record.getZhanghao());
            jiuzhentongzhi.setTongzhibeizhu(record.getTongzhineirong());
            jiuzhentongzhi.setTongzhishijian(new Date());
            jiuzhentongzhiService.insert(jiuzhentongzhi);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 处理定时任务：发送待发送的通知
     */
    @Transactional
    public void processPendingNotifications() {
        // 获取所有待发送且到达发送时间的通知
        List<TongzhijiluEntity> pendingList = tongzhijiluService.selectList(
            new com.baomidou.mybatisplus.mapper.EntityWrapper<TongzhijiluEntity>()
                .eq("fasongzhuangtai", SEND_STATUS_PENDING)
                .le("jihuafasongshijian", new Date())
        );
        
        for (TongzhijiluEntity record : pendingList) {
            boolean success = sendNotification(record);
            
            if (success) {
                record.setFasongzhuangtai(SEND_STATUS_SUCCESS);
                record.setFasongshijian(new Date());
            } else {
                record.setFasongzhuangtai(SEND_STATUS_FAILED);
                record.setChongshicishu(record.getChongshicishu() + 1);
                record.setShibaiyuanyin("定时发送失败");
            }
            
            tongzhijiluService.updateById(record);
        }
    }
    
    /**
     * 处理定时任务：重试发送失败的通知
     */
    @Transactional
    public void processFailedNotifications() {
        List<TongzhijiluEntity> failedList = tongzhijiluService.selectRetryList(MAX_RETRY_COUNT);
        
        for (TongzhijiluEntity record : failedList) {
            boolean success = sendNotification(record);
            
            if (success) {
                record.setFasongzhuangtai(SEND_STATUS_SUCCESS);
                record.setFasongshijian(new Date());
                record.setShibaiyuanyin(null);
            } else {
                record.setChongshicishu(record.getChongshicishu() + 1);
                record.setShibaiyuanyin("重试发送失败，次数：" + record.getChongshicishu());
                
                if (record.getChongshicishu() >= MAX_RETRY_COUNT) {
                    record.setFasongzhuangtai(SEND_STATUS_FAILED);
                }
            }
            
            tongzhijiluService.updateById(record);
        }
    }
    
    /**
     * 获取就诊前一天的日期
     */
    private Date getOneDayBefore(Date appointmentTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointmentTime);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 9); // 上午9点发送提醒
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }
    
    /**
     * 获取就诊当天早上的日期
     */
    private Date getOnDayMorning(Date appointmentTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointmentTime);
        cal.set(Calendar.HOUR_OF_DAY, 8); // 上午8点发送提醒
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }
    
    /**
     * 格式化日期
     */
    private String formatDate(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }
}
