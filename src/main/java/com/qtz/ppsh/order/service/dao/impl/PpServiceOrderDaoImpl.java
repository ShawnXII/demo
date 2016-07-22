package com.qtz.ppsh.order.service.dao.impl;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.qtz.base.dao.impl.MyBaitsDaoImpl;
import com.qtz.base.dto.order.PpServiceOrder;
import com.qtz.base.exception.BaseDaoException;
import com.qtz.ppsh.order.service.dao.PpServiceOrderDao;
/**
 * <p>Title:PpServiceOrderDaoImpl</p>
 * <p>Description:goodsDAO实现类</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-11-25
 */
@Repository("ppServiceOrderDaoImpl")
public class PpServiceOrderDaoImpl extends MyBaitsDaoImpl<PpServiceOrder,Long> implements PpServiceOrderDao {
	/**MYBatis命名空间名*/
	private static String preName = PpServiceOrderDao.class.getName();
	/** 
	* 【取得】MYBatis命名空间名
	* @return  	MYBatis命名空间名
	*/
	@Override
	protected String getPreName() {
		return preName;
	}
	@Override
	public int queryIsTrialPpStore(Long userId) throws BaseDaoException {
		Map<String, Object> parameter= new HashMap<String, Object>();
		parameter.put("userId", userId);
		return getMyBaitsTemplate().getSqlSession().selectOne(getPreName()+".queryIsTrialPpStore", parameter);
	}
	@Override
	public PpServiceOrder getLockOrder(Long orderId) throws BaseDaoException {
		if (orderId==null) {
			throw new BaseDaoException("null exception");
		}
		Map<String, Object> parameter= new HashMap<String, Object>();
		parameter.put("orderId", orderId);
		return getMyBaitsTemplate().getSqlSession().selectOne(getPreName()+".getLockOrder", parameter);
	}
}