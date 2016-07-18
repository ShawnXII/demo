package com.qtz.ppsh.order.service.dao.impl;
import org.springframework.stereotype.Repository;

import com.qtz.base.dao.impl.MyBaitsDaoImpl;
import com.qtz.ppsh.order.service.dao.OrderGoodsDao;
import com.qtz.ppsh.order.spi.dto.OrderGoods;
/**
 * <p>Title:OrderGoodsDaoImpl</p>
 * <p>Description:订单对应商品DAO实现类</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-09-02
 */
@Repository("orderGoodsDaoImpl")
public class OrderGoodsDaoImpl extends MyBaitsDaoImpl<OrderGoods,Long> implements OrderGoodsDao {
	/**MYBatis命名空间名*/
	private static String preName = OrderGoodsDao.class.getName();
	/** 
	* 【取得】MYBatis命名空间名
	* @return  	MYBatis命名空间名
	*/
	@Override
	protected String getPreName() {
		return preName;
	}
}