package com.cl.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.Map;
import java.util.List;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;
import com.cl.utils.R;


import com.cl.dao.JiuzhentongzhiDao;
import com.cl.entity.JiuzhentongzhiEntity;
import com.cl.service.JiuzhentongzhiService;
import com.cl.entity.view.JiuzhentongzhiView;

@Service("jiuzhentongzhiService")
public class JiuzhentongzhiServiceImpl extends ServiceImpl<JiuzhentongzhiDao, JiuzhentongzhiEntity> implements JiuzhentongzhiService {

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
     
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<JiuzhentongzhiEntity> page = this.selectPage(
                new Query<JiuzhentongzhiEntity>(params).getPage(),
                new EntityWrapper<JiuzhentongzhiEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<JiuzhentongzhiEntity> wrapper) {
		  Page<JiuzhentongzhiView> page =new Query<JiuzhentongzhiView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
  	}
    
	@Override
	public List<JiuzhentongzhiView> selectListView(Wrapper<JiuzhentongzhiEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public JiuzhentongzhiView selectView(Wrapper<JiuzhentongzhiEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}
    
    @Override
    @Transactional
    public boolean sendNotification(Long id) {
        JiuzhentongzhiEntity tongzhi = this.selectById(id);
        if (tongzhi == null) {
            return false;
        }
        
        boolean success = false;
        String errorMessage = null;
        
        try {
            // 模拟发送通知的逻辑
            // 在实际项目中，这里可以是调用短信服务、邮件服务或其他通知服务
            System.out.println("发送通知：" + tongzhi.getTongzhibianhao() + " 给用户：" + tongzhi.getZhanghao());
            
            // 模拟发送成功（可以根据实际情况调整成功率）
            success = Math.random() > 0.2; // 80%的成功率
            
            if (!success) {
                throw new RuntimeException("发送通知失败，网络异常");
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            success = false;
        }
        
        // 更新通知状态
        tongzhi.setZuihoushijian(new Date());
        if (success) {
            tongzhi.setFsshuangtai("已发送");
            tongzhi.setCuowuxinxi(null);
        } else {
            tongzhi.setFsshuangtai("发送失败");
            tongzhi.setCuowuxinxi(errorMessage);
            // 增加重试次数
            if (tongzhi.getChongshicishu() == null) {
                tongzhi.setChongshicishu(0);
            }
            tongzhi.setChongshicishu(tongzhi.getChongshicishu() + 1);
        }
        
        this.updateById(tongzhi);
        return success;
    }
    
    @Override
    @Transactional
    public boolean retryNotification(Long id) {
        JiuzhentongzhiEntity tongzhi = this.selectById(id);
        if (tongzhi == null) {
            return false;
        }
        
        // 检查是否超过最大重试次数
        if (tongzhi.getChongshicishu() != null && tongzhi.getChongshicishu() >= MAX_RETRY_COUNT) {
            return false;
        }
        
        return this.sendNotification(id);
    }
    
    @Override
    @Transactional
    public R batchRetryNotifications(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return R.ok();
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Long id : ids) {
            boolean success = this.retryNotification(id);
            if (success) {
                successCount++;
            } else {
                failCount++;
            }
        }
        
        return R.ok().put("successCount", successCount).put("failCount", failCount);
    }
}
