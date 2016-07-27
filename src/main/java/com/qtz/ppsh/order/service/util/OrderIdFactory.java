package com.qtz.ppsh.order.service.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.qtz.base.util.Constants;
import com.qtz.base.util.RedisGroupKey;
import com.qtz.commons.framework.SpringContextHelper;
import com.qtz.commons.text.CfgHelper;


/**
 * 
 * <p>Title:OrderIdFactory</p>
 * <p>Description:(订单Id生成)</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 - changbo.li
 * @version v1.0 2015年8月26日
 */
public class OrderIdFactory {
	private static String orderIdPrefix="";
	private OrderIdFactory(){
	}
	private static OrderIdSingle orderIdSingle=null;
	private static final Long initSuffix=100000L;
	 
	public static Long getOrderId(){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		String prefix = sdf.format(new Date());
		synchronized (OrderIdFactory.class) {
			Long suffix = OrderIdSingle.getInstance().suffix()+10000;
			//TODO 正式环境记得修改
			orderIdPrefix=CfgHelper.getValue(Constants.orderId_prefix);
			return  Long.valueOf(orderIdPrefix+prefix+suffix);
		}
		
	}
	
	
	
	 static class OrderIdSingle{
		public static OrderIdSingle getInstance() {
			if (orderIdSingle==null) {
				synchronized (OrderIdSingle.class) {
					if(orderIdSingle==null){
						orderIdSingle=new OrderIdSingle();
					}
				}
			}
			return orderIdSingle;
		}
		
		private Long suffix(){
		StringRedisTemplate stringRedisTemplate=SpringContextHelper.getBean(StringRedisTemplate.class);
			//	
		Long increment=null;
			if(stringRedisTemplate.hasKey(RedisGroupKey.amf_pay_order_suffix)){
				increment=stringRedisTemplate.opsForValue().increment(RedisGroupKey.amf_pay_order_suffix, 1);
			}else{
				stringRedisTemplate.opsForValue().set(RedisGroupKey.amf_pay_order_suffix, String.valueOf(initSuffix),24,TimeUnit.HOURS);
				increment=initSuffix;
			}
			return increment;
		}
	}
}
