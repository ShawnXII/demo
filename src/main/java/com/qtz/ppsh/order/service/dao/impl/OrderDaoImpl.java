package com.qtz.ppsh.order.service.dao.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.qtz.base.dao.impl.MyBaitsDaoImpl;
import com.qtz.base.exception.DaoException;
import com.qtz.base.util.Global;
import com.qtz.ppsh.order.service.dao.OrderDao;
import com.qtz.ppsh.order.spi.dto.Order;
/**
 * <p>Title:OrderDaoImpl</p>
 * <p>Description:订单DAO实现类</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-09-02
 */
@Repository("orderDaoImpl")
public class OrderDaoImpl extends MyBaitsDaoImpl<Order,Long> implements OrderDao {
	/**MYBatis命名空间名*/
	private static String preName = OrderDao.class.getName();
	/** 
	* 【取得】MYBatis命名空间名
	* @return  	MYBatis命名空间名
	*/
	@Override
	protected String getPreName() {
		return preName;
	}
	@Override
	public List<Order> queryTransactionClose(Map<String, Object> map) throws DaoException{
		return getMyBaitsTemplate().findList(getPreName(), "queryTransactionClose", map);
	}
	@Override
	public List<Order> findOrderClose(Long ctime) throws DaoException {
		return getMyBaitsTemplate().findList(getPreName(), "queryCloseOrder", ctime);
	}
	@Override
	public void updateCancelOrderCoupon(Long orderId, Double payPrice)
			throws DaoException {
		Map<String, Object> parameter=new HashMap<String, Object>();
		parameter.put("orderId", orderId);
		parameter.put("payPrice", payPrice);
		getMyBaitsTemplate().getSqlSession().update(getPreName()+Global.SPLIT_DIAN+"updateCancelOrderCoupon", parameter);
	}
	@Override
	public List<Order> querySellerCummer(Map<String, Object> map)
			throws DaoException {
		return getMyBaitsTemplate().findList(getPreName(), "querySellerCummer", map);
	}
	@Override
	public List<Map<Object, Object>> queryCountOrderMonth(Long userId, String month,int pageIndex,int pageSize) throws DaoException {
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("month", month);
		map.put("pageIndex", (pageIndex-1)*pageSize);
		map.put("pageSize", pageSize);
		return getMyBaitsTemplate().findList(getPreName(), "queryCountOrderMonth", map);
	}
	@Override
	public Order getLockOrder(Long orderId) throws DaoException {
		Map<String, Object> parameter=new HashMap<String, Object>();
		parameter.put("orderId", orderId);
		return getMyBaitsTemplate().getSqlSession().selectOne(getPreName()+Global.SPLIT_DIAN+"getLockOrder", parameter);
	}
	@Override
	public List<Order> queryTempOrder() throws DaoException {
		return getMyBaitsTemplate().getSqlSession().selectList(getPreName()+Global.SPLIT_DIAN+"queryTempOrder");
	}
	@Override
	public List<Map<String, Object>> getOrdersToExport(Map<String, Object> param)
			throws DaoException {
		return getMyBaitsTemplate().getSqlSession().selectList(getPreName()+Global.SPLIT_DIAN+"getOrdersToExport", param);
	}
	@Override
	public Integer findOrderCount(Order vo) throws DaoException {
		return getMyBaitsTemplate().getSqlSession().selectOne(getPreName()+Global.SPLIT_DIAN+"findOrderCount", vo);
		
	}
}