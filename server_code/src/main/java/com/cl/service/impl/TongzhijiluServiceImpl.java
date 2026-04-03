package com.cl.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.Date;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;


import com.cl.dao.TongzhijiluDao;
import com.cl.entity.TongzhijiluEntity;
import com.cl.service.TongzhijiluService;
import com.cl.entity.view.TongzhijiluView;
import com.cl.service.JiuzhentongzhiService;
import com.cl.entity.JiuzhentongzhiEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service("tongzhijiluService")
public class TongzhijiluServiceImpl extends ServiceImpl<TongzhijiluDao, TongzhijiluEntity> implements TongzhijiluService {

    @Autowired
    private JiuzhentongzhiService jiuzhentongzhiService;
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TongzhijiluEntity> page = this.selectPage(
                new Query<TongzhijiluEntity>(params).getPage(),
                new EntityWrapper<TongzhijiluEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TongzhijiluEntity> wrapper) {
		  Page<TongzhijiluView> page =new Query<TongzhijiluView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<TongzhijiluView> selectListView(Wrapper<TongzhijiluEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TongzhijiluView selectView(Wrapper<TongzhijiluEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}
	
	@Override
	public List<TongzhijiluEntity> selectRetryList(Integer maxRetryCount) {
		return baseMapper.selectRetryList(maxRetryCount);
	}
	
	@Override
	@Transactional
	public boolean retrySendNotification(Long id) {
		TongzhijiluEntity record = this.selectById(id);
		if (record == null) {
			return false;
		}
		
		// 模拟发送通知
		boolean sendSuccess = sendNotification(record);
		
		if (sendSuccess) {
			record.setFasongzhuangtai(1);
			record.setFasongshijian(new Date());
			record.setShibaiyuanyin(null);
		} else {
			record.setChongshicishu(record.getChongshicishu() + 1);
			record.setShibaiyuanyin("手动重试发送失败");
			if (record.getChongshicishu() >= record.getZuidachongshicishu()) {
				record.setFasongzhuangtai(2); // 标记为最终失败
			}
		}
		
		return this.updateById(record);
	}
	
	@Override
	@Transactional
	public int batchRetrySendNotifications(List<Long> ids) {
		int successCount = 0;
		for (Long id : ids) {
			if (retrySendNotification(id)) {
				successCount++;
			}
		}
		return successCount;
	}
	
	@Override
	@Transactional
	public boolean updateReceiveStatus(Long id, Integer receiveStatus) {
		TongzhijiluEntity record = this.selectById(id);
		if (record == null) {
			return false;
		}
		record.setJieshouzhuangtai(receiveStatus);
		record.setJieshoushijian(new Date());
		return this.updateById(record);
	}
	
	/**
	 * 模拟发送通知的方法
	 */
	private boolean sendNotification(TongzhijiluEntity record) {
		// 这里可以实现实际的通知发送逻辑（短信、推送等）
		// 目前模拟发送，返回true表示成功
		try {
			// 模拟发送延迟
			Thread.sleep(100);
			
			// 同时创建就诊通知记录
			JiuzhentongzhiEntity jiuzhentongzhi = new JiuzhentongzhiEntity();
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

}
