package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.entity.JiuzhentongzhiEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.JiuzhentongzhiView;


/**
 * 就诊通知
 *
 * @author 
 * @email 
 * @date 2025-03-27 15:44:15
 */
public interface JiuzhentongzhiService extends IService<JiuzhentongzhiEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<JiuzhentongzhiView> selectListView(Wrapper<JiuzhentongzhiEntity> wrapper);
   	
   	JiuzhentongzhiView selectView(@Param("ew") Wrapper<JiuzhentongzhiEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<JiuzhentongzhiEntity> wrapper);
   	
   	/**
   	 * 发送通知
   	 * @param id 通知ID
   	 * @return 是否发送成功
   	 */
   	boolean sendNotification(Long id);
   	
   	/**
   	 * 重试发送失败的通知
   	 * @param id 通知ID
   	 * @return 是否发送成功
   	 */
   	boolean retryNotification(Long id);
   	
   	/**
   	 * 批量重试发送失败的通知
   	 * @param ids 通知ID列表
   	 * @return 重试结果
   	 */
   	R batchRetryNotifications(Long[] ids);
   
}

