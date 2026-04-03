package com.cl.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import com.cl.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.annotation.SysLog;

import com.cl.entity.TongzhijiluEntity;
import com.cl.entity.view.TongzhijiluView;

import com.cl.service.TongzhijiluService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.MapUtils;
import com.cl.utils.CommonUtil;

/**
 * 通知记录
 * 后端接口
 * @author 
 * @email 
 * @date 2025-04-02 10:00:00
 */
@RestController
@RequestMapping("/tongzhijilu")
public class TongzhijiluController {
    @Autowired
    private TongzhijiluService tongzhijiluService;

    /**
     * 后台列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TongzhijiluEntity tongzhijilu,
                                                                                HttpServletRequest request){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();
        
        PageUtils page = tongzhijiluService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tongzhijilu), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TongzhijiluEntity tongzhijilu,
		HttpServletRequest request){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();

		PageUtils page = tongzhijiluService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tongzhijilu), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TongzhijiluEntity tongzhijilu){
       	EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();
      	ew.allEq(MPUtil.allEQMapPre( tongzhijilu, "tongzhijilu")); 
        return R.ok().put("data", tongzhijiluService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TongzhijiluEntity tongzhijilu){
        EntityWrapper< TongzhijiluEntity> ew = new EntityWrapper< TongzhijiluEntity>();
 		ew.allEq(MPUtil.allEQMapPre( tongzhijilu, "tongzhijilu")); 
		TongzhijiluView tongzhijiluView =  tongzhijiluService.selectView(ew);
		return R.ok("查询通知记录成功").put("data", tongzhijiluView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TongzhijiluEntity tongzhijilu = tongzhijiluService.selectById(id);
		tongzhijilu = tongzhijiluService.selectView(new EntityWrapper<TongzhijiluEntity>().eq("id", id));
        return R.ok().put("data", tongzhijilu);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TongzhijiluEntity tongzhijilu = tongzhijiluService.selectById(id);
		tongzhijilu = tongzhijiluService.selectView(new EntityWrapper<TongzhijiluEntity>().eq("id", id));
        return R.ok().put("data", tongzhijilu);
    }
    
    /**
     * 后端保存
     */
    @RequestMapping("/save")
    @SysLog("新增通知记录")
    public R save(@RequestBody TongzhijiluEntity tongzhijilu, HttpServletRequest request){
    	tongzhijiluService.insert(tongzhijilu);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @SysLog("新增通知记录")
    @RequestMapping("/add")
    public R add(@RequestBody TongzhijiluEntity tongzhijilu, HttpServletRequest request){
    	tongzhijiluService.insert(tongzhijilu);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    @SysLog("修改通知记录")
    public R update(@RequestBody TongzhijiluEntity tongzhijilu, HttpServletRequest request){
        tongzhijiluService.updateById(tongzhijilu);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @SysLog("删除通知记录")
    public R delete(@RequestBody Long[] ids){
        tongzhijiluService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
    /**
     * 手动重试发送通知
     */
    @RequestMapping("/retry/{id}")
    @SysLog("手动重试发送通知")
    public R retry(@PathVariable("id") Long id){
        boolean success = tongzhijiluService.retrySendNotification(id);
        if(success){
            return R.ok("重试发送成功");
        } else {
            return R.error("重试发送失败");
        }
    }
    
    /**
     * 批量重试发送通知
     */
    @RequestMapping("/batchRetry")
    @SysLog("批量重试发送通知")
    public R batchRetry(@RequestBody Long[] ids){
        int successCount = tongzhijiluService.batchRetrySendNotifications(Arrays.asList(ids));
        return R.ok("成功重试发送" + successCount + "条通知");
    }
    
    /**
     * 更新用户接收状态
     */
    @RequestMapping("/updateReceiveStatus")
    @SysLog("更新用户接收状态")
    public R updateReceiveStatus(@RequestParam Long id, @RequestParam Integer receiveStatus){
        boolean success = tongzhijiluService.updateReceiveStatus(id, receiveStatus);
        if(success){
            return R.ok("更新接收状态成功");
        } else {
            return R.error("更新接收状态失败");
        }
    }
    
    /**
     * 获取发送失败的记录列表
     */
    @RequestMapping("/failedList")
    public R failedList(@RequestParam Map<String, Object> params, HttpServletRequest request){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();
        ew.eq("fasongzhuangtai", 2); // 发送失败
        PageUtils page = tongzhijiluService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }
    
    /**
     * 获取待发送的记录列表
     */
    @RequestMapping("/pendingList")
    public R pendingList(@RequestParam Map<String, Object> params, HttpServletRequest request){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();
        ew.eq("fasongzhuangtai", 0); // 待发送
        PageUtils page = tongzhijiluService.queryPage(params, MPUtil.sort(MPUtil.between(ew, params), params));
        return R.ok().put("data", page);
    }
    
    /**
     * 获取需要重试的记录列表
     */
    @RequestMapping("/retryList")
    public R retryList(@RequestParam Integer maxRetryCount){
        List<TongzhijiluEntity> list = tongzhijiluService.selectRetryList(maxRetryCount);
        return R.ok().put("data", list);
    }
    
    /**
     * 统计通知发送情况
     */
    @RequestMapping("/statistics")
    public R statistics(){
        Map<String, Object> result = new HashMap<>();
        
        // 总记录数
        int totalCount = tongzhijiluService.selectCount(new EntityWrapper<TongzhijiluEntity>());
        result.put("totalCount", totalCount);
        
        // 发送成功数
        int successCount = tongzhijiluService.selectCount(
            new EntityWrapper<TongzhijiluEntity>().eq("fasongzhuangtai", 1));
        result.put("successCount", successCount);
        
        // 发送失败数
        int failedCount = tongzhijiluService.selectCount(
            new EntityWrapper<TongzhijiluEntity>().eq("fasongzhuangtai", 2));
        result.put("failedCount", failedCount);
        
        // 待发送数
        int pendingCount = tongzhijiluService.selectCount(
            new EntityWrapper<TongzhijiluEntity>().eq("fasongzhuangtai", 0));
        result.put("pendingCount", pendingCount);
        
        // 已接收数
        int receivedCount = tongzhijiluService.selectCount(
            new EntityWrapper<TongzhijiluEntity>().eq("jieshouzhuangtai", 1));
        result.put("receivedCount", receivedCount);
        
        return R.ok().put("data", result);
    }
	
}
