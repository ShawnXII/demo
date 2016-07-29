package com.qtz.ppsh.order.service.service.impl;



import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qtz.base.dao.BizDao;
import com.qtz.base.dto.order.PpServiceOrder;
import com.qtz.base.dto.user.User;
import com.qtz.base.dto.user.UserType;
import com.qtz.base.exception.DaoException;
import com.qtz.base.exception.ExceptionCode;
import com.qtz.base.exception.ServiceException;
import com.qtz.base.service.impl.BaseServiceImpl;
import com.qtz.commons.math.ArithUtil;
import com.qtz.goods.spi.dto.PpServiceGoods;
import com.qtz.goods.spi.service.PpServiceGoodsService;
import com.qtz.id.spi.IdService;
import com.qtz.member.spi.store.service.SellerStoreService;
import com.qtz.member.spi.user.service.UsersShopService;
import com.qtz.ppsh.order.service.dao.PpServiceOrderDao;
import com.qtz.ppsh.order.spi.dto.OrderPrefix;
import com.qtz.ppsh.order.spi.dto.PayOrderModel;
import com.qtz.ppsh.order.spi.dto.PayOrderTypeEnum;
import com.qtz.ppsh.order.spi.service.PpServiceOrderService;


/**
 * <p>
 * Title:PpServiceOrderServiceImpl
 * </p>
 * <p>
 * Description:goods服务实现类
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
@Service("ppServiceOrderServiceImpl")
public class PpServiceOrderServiceImpl extends
        BaseServiceImpl<PpServiceOrder, Long> implements
        PpServiceOrderService {
    /**
     * 初始化日志对象
     */
	private static Logger log = Logger.getLogger(PpServiceOrderServiceImpl.class);
    /**
     * 注入goodsDAO接口类
     */
    @Resource(name = "ppServiceOrderDaoImpl")
    private PpServiceOrderDao dao;

    @Autowired
    private UsersShopService usersShopService;

    @Autowired
    private PpServiceGoodsService serviceGoodsService;

    @Autowired
    private SellerStoreService sellerStoreService;
  
    @Autowired
    private IdService idService;

    /**
     * 【取得】业务DAO对象
     *
     * @return 业务DAO对象
     */
    @Override
    protected BizDao<PpServiceOrder, Long> getDao() {
        return dao;
    }


    @Override
    public Long saveSubSellerStoreOrder(String pgId, Long reqUserId, Integer payType) throws ServiceException {
        log.info("正在提交订单..");
        if (pgId == null || reqUserId == null) {
            throw new ServiceException(ExceptionCode.NULL_EXCEPTION, "null exception");
        }
        PpServiceGoods ppServiceGoods = serviceGoodsService.findVo(Long.valueOf(pgId), null);
        if (ppServiceGoods == null) {
            throw new ServiceException(ExceptionCode.NULL_EXCEPTION, " 不存在的服务");
        }
        if (ppServiceGoods.getCode().contains(PpServiceGoods.store_prefix + ".sy")) {
            if (this.queryIsTrialPpStore(reqUserId.longValue())) {
                throw new ServiceException(ExceptionCode.HAVE_TO_TRY_PP_STORE, "已经试用过店铺");
            }
        }
        User reqUser = usersShopService.getUser(reqUserId);
        if (reqUser == null) {
            throw new ServiceException(ExceptionCode.ORDER_SUB_SELLER_ERROR, "订单提交有误  商家不存在");
//    	reqUser = usersShopService.getUser(reqUserId);
        }
        if (reqUser.getUserType().intValue() == UserType.PERSON.value()) {
            throw new ServiceException(ExceptionCode.ORDER_SUB_SELLER_ERROR, "订单提交有误  个人用户不能够下单");
        }
        try {
            if (sellerStoreService.getSellerStore(reqUserId) != null) {
                throw new ServiceException(ExceptionCode.SELLER_STORE_EXIST_VALID, "店铺存在并且有效不能付款.");
            }
        } catch (ServiceException e) {
            log.error(e);
        }

        Long orderId = Long.parseLong(OrderPrefix.PP_SHOP_ORDER + idService.generateId());
        
        PpServiceOrder ppServiceOrder =
                new PpServiceOrder(Long.valueOf(pgId), reqUserId, ppServiceGoods.getOriginalPrice(), ppServiceGoods.toString(), payType.intValue(), ppServiceGoods.getPresentPrice());
       
        ppServiceOrder.setDmId(orderId);
        
		try {
			
			 getDao().addVo(ppServiceOrder);
			 
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
        
        return orderId;
    }

    @Override
    public boolean queryOrderPaymentStatus(Long orderId, Integer paymentType) throws ServiceException {
        return true;
    }

    @Override
    public boolean queryIsTrialPpStore(Long userId) throws ServiceException {
        try {
            int result = dao.queryIsTrialPpStore(userId);
            if (result > 0) {
                return true;
            }
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
        return false;
    }

    @Override
    public PpServiceOrder getLockOrder(Long orderId) throws ServiceException {
        try {
            return dao.getLockOrder(orderId);
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public PayOrderModel findAndCheckOrder(Long orderId) throws ServiceException {

        PpServiceOrder order = this.findVo(orderId, null);
        if (order == null) {
            throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        if (1 != order.getPayStatus()) {
            throw new ServiceException(ExceptionCode.ORDER_STATUS_ERROR, "订单已支付或关闭");
        }
        PayOrderModel model = new PayOrderModel();
        model.setOrderId(String.valueOf(orderId));
        model.setTime(order.getCrtime());
        model.setOrderName("胖胖生活年费");
        model.setOrderType(PayOrderTypeEnum.PPORDER);
        Double price = order.getToPayAmount();
        model.setYuanPrice(ArithUtil.moneyFormat(price));
        model.setFenPrice(String.valueOf(ArithUtil.yuanToFen(price)));
        model.setPayType(order.getPayType());
        model.setThirdSn(order.getThreeSerialNumber());
        return model;
    }
}
