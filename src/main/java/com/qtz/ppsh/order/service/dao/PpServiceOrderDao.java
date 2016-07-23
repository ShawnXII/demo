package com.qtz.ppsh.order.service.dao;

import com.qtz.base.dao.BizDao;
import com.qtz.base.dto.order.PpServiceOrder;
import com.qtz.base.exception.DaoException;

/**
 * <p>
 * Title:PpServiceOrderDao
 * </p>
 * <p>
 * Description:goodsDAO接口类
 * </p>
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * <p>
 * Company: 深圳市擎天柱信息科技有限公司
 * </p>
 * 
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-11-25
 */
public interface PpServiceOrderDao extends
    BizDao<PpServiceOrder, Long> {
	/**
	 * 
	  * 【查询用户曾经是否试用过店铺产品 >0 则试用过】
	  * @param userId
	  * @return
	  * @throws DaoException  
	  * @time:2016年1月6日 上午9:57:18
	  * @author 涂鑫
	  * @version
	 */
	int queryIsTrialPpStore(Long userId) throws DaoException;
	/**
	 * 
	  * 【获取订单 有行锁】
	  * @param orderId
	  * @return
	  * @throws DaoException  
	  * @time:2016年1月28日 下午1:23:06
	  * @author 涂鑫
	  * @version
	 */
	PpServiceOrder getLockOrder(Long orderId) throws DaoException;

}
