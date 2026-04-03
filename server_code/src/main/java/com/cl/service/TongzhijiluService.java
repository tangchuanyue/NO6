package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.TongzhijiluEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.TongzhijiluView;


/**
 * 通知记录
 *
 * @author 
 * @email 
 * @date 2025-04-02 10:00:00
 */
public interface TongzhijiluService extends IService<TongzhijiluEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TongzhijiluView> selectListView(Wrapper<TongzhijiluEntity> wrapper);
   	
   	TongzhijiluView selectView(@Param("ew") Wrapper<TongzhijiluEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TongzhijiluEntity> wrapper);
   	
   	/**
	 * 查询需要重试的通知记录
	 */
	List<TongzhijiluEntity> selectRetryList(Integer maxRetryCount);
   	
   	/**
	 * 手动重试发送通知
	 */
	boolean retrySendNotification(Long id);
   	
   	/**
	 * 批量重试发送通知
	 */
	int batchRetrySendNotifications(List<Long> ids);
   	
   	/**
	 * 更新用户接收状态
	 */
	boolean updateReceiveStatus(Long id, Integer receiveStatus);
   	
}
