package com.qtz.ppsh.order.service.dao;
import java.util.List;

import com.qtz.base.dao.BizDao;
import com.qtz.base.exception.DaoException;
import com.qtz.ppsh.order.spi.dto.OrderLog;
/**
 * <p>Title:OrderLogDao</p>
 * <p>Description:订单日志DAO接口类</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-09-08
 */
public interface OrderLogDao extends BizDao<OrderLog,Long> {
	/**
	 * 
	  * 【根据订单日志  更新不为null】
	  * @param orderLog  
	  * @time:2015年9月17日 上午10:07:56
	  * @author 涂鑫
	  * @version
	 */
	public void updateOrderLogByOrderIdNotNull(OrderLog orderLog) throws DaoException;
	
	/**
	 * 
	 * 【获取订单日志信息】
	 * @param orderId					订单id
	 * @return
	 * @throws ServiceException  
	 * @time:2015年9月16日 上午11:12:10
	 * @author 聂恒
	 * @version
	 */
	public List<OrderLog> findByOrderId(Long orderId)throws DaoException;
}