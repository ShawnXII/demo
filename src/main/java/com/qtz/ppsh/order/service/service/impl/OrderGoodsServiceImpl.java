package com.qtz.ppsh.order.service.service.impl;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qtz.base.dao.BizDao;
import com.qtz.base.exception.DaoException;
import com.qtz.base.exception.ServiceException;
import com.qtz.base.service.impl.BaseServiceImpl;
import com.qtz.ppsh.order.service.dao.OrderGoodsDao;
import com.qtz.ppsh.order.spi.dto.OrderGoods;
import com.qtz.ppsh.order.spi.service.OrderGoodsService;
/**
 * <p>Title:OrderGoodsServiceImpl</p>
 * <p>Description:订单对应商品服务实现类</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-09-02
 */
@Service("orderGoodsServiceImpl")
public class OrderGoodsServiceImpl extends BaseServiceImpl<OrderGoods,Long> implements OrderGoodsService  {
	/**初始化日志对象*/
	private static Logger log = Logger.getLogger(OrderGoodsServiceImpl.class);
	/**注入订单对应商品DAO接口类*/
	@Resource(name="orderGoodsDaoImpl")
    private OrderGoodsDao dao;
	
	/** 
	* 【取得】业务DAO对象
	* @return 	业务DAO对象  
	*/
	@Override
	protected BizDao<OrderGoods,Long> getDao() {
		return dao;
	}
	@Override
	public void delOrderGoodsByOrderId(Long orderid) throws ServiceException {
		// TODO Auto-generated method stub
		OrderGoods og = new OrderGoods();
		og.setOrderId(orderid);
		try {
			this.dao.delVo(og);
		} catch (DaoException e) {
			throw new  ServiceException(e);
		}
	}
	@Override
	public List<OrderGoods> getOrderGoodss(Long orderId)
			throws ServiceException {
		OrderGoods where=new OrderGoods();
		where.setOrderId(orderId);
		List<OrderGoods> findList = findList(where);
		return findList;
	}
	
}