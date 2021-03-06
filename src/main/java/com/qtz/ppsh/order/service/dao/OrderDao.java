package com.qtz.ppsh.order.service.dao;
import java.util.List;
import java.util.Map;

import com.qtz.base.dao.BizDao;
import com.qtz.base.exception.DaoException;
import com.qtz.ppsh.order.spi.dto.Order;
/**
 * <p>Title:OrderDao</p>
 * <p>Description:订单DAO接口类</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-09-02
 */
public interface OrderDao extends BizDao<Order,Long> {
	
	/**
	 * 
	* 【查询商家的客户】(这里用一句话描述这个方法的作用)
	* @param map
	* @return
	* @throws DaoException
	 */
	List<Order> querySellerCummer(Map<String,Object> map)throws DaoException;
	/**
	 * 
	  * 【查询交易关闭订单】
	  * @param pageIndex
	  * @return  
	  * @time:2015年9月16日 下午4:01:58
	  * @author 涂鑫
	  * @version
	 */
	List<Order> queryTransactionClose(Map<String, Object> map)throws DaoException;
	/**
	 * 
	  * 【查询可用关闭的订单】
	  * @param ctime
	  * @return
	  * @throws DaoException  
	  * @time:2015年9月30日 下午11:35:33
	  * @author 涂鑫
	  * @version
	 */
	List<Order> findOrderClose(Long ctime) throws DaoException;
	/**
	 * 
	  * 【取消订单优惠劵】
	  * @param orderId				订单id
	  * @param payPrice				更新支付金额
	  * @throws DaoException  
	  * @time:2015年10月8日 下午5:47:32
	  * @author 涂鑫
	  * @version
	 */
	@Deprecated
	void updateCancelOrderCoupon(Long orderId,Double payPrice)throws DaoException;
	
	/**
	 * 
	  * 【查询订单每日统计】
	  * @param userId
	  * @param month
	  * @param pageIndex
	  * @return
	  * @throws DaoException  
	  * @time:2015年12月31日 下午1:05:21
	  * @author 涂鑫
	  * @version
	 */
	public List<Map<Object, Object>> queryCountOrderMonth(Long userId, String month,int pageIndex,int pageSize) throws DaoException;
	
	/**
	 * 
	  * 【获取订单  有数据库悲观锁】
	  * @param orderId
	  * @return
	  * @throws DaoException  
	  * @time:2016年1月22日 下午3:51:52
	  * @author 涂鑫
	  * @version
	 */
	public Order getLockOrder(Long orderId)throws DaoException;
	
	/**
	 * 
	  * 【查询过年期间25号下单订单】
	  * @return
	  * @throws DaoException  
	  * @time:2016年1月30日 下午5:53:32
	  * @author 涂鑫
	  * @version
	 */
	public List<Order> queryTempOrder()throws DaoException;
	
	/**
	 * 获取时间段内的订单
	 * @param param
	 * @return
	 * @throws ServiceException
	 */
	public List<Map<String, Object>> getOrdersToExport(Map<String, Object> param) throws DaoException;
	
	
	/**
	  * 【查询订单总数】
	  * @param vo
	  * @return
	  * @throws DaoException Long
	  * @time: 2016年6月21日下午7:27:00
	  * @author 张玉兵
	 */
	public Integer findOrderCount(Order vo)throws DaoException;

	
}