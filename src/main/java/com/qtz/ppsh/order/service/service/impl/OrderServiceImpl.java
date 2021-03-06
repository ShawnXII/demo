package com.qtz.ppsh.order.service.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.qtz.base.common.Pager;
import com.qtz.base.dao.BizDao;
import com.qtz.base.dto.order.PayStatusEnum;
import com.qtz.base.dto.user.Coupon;
import com.qtz.base.dto.user.Coupon.CouponType;
import com.qtz.base.dto.user.CouponRules;
import com.qtz.base.dto.user.User;
import com.qtz.base.dto.user.UserType;
import com.qtz.base.enums.Status;
import com.qtz.base.exception.DaoException;
import com.qtz.base.exception.ExceptionCode;
import com.qtz.base.exception.ServiceException;
import com.qtz.base.response.RespCode;
import com.qtz.base.service.impl.BaseServiceImpl;
import com.qtz.base.util.RespKey;
import com.qtz.commons.math.ArithUtil;
import com.qtz.commons.text.CfgHelper;
import com.qtz.goods.spi.dto.StoreGoods;
import com.qtz.goods.spi.dto.StoreGoods.GoodsStatus;
import com.qtz.goods.spi.dto.StoreGoods.IsCoupon;
import com.qtz.goods.spi.service.StoreGoodsService;
import com.qtz.id.spi.IdService;
import com.qtz.jpush.dto.JpushDto;
import com.qtz.jpush.service.JPushMessageService;
import com.qtz.member.spi.coupon.dto.CouponUser;
import com.qtz.member.spi.coupon.model.CouponKey;
import com.qtz.member.spi.coupon.service.CouponService;
import com.qtz.member.spi.coupon.service.CouponUserService;
import com.qtz.member.spi.store.dto.SellerStore;
import com.qtz.member.spi.store.dto.SellerStore.IsSend;
import com.qtz.member.spi.store.dto.SellerStore.IsStop;
import com.qtz.member.spi.store.service.SellerStoreService;
import com.qtz.member.spi.user.dto.UserReceivingInfo;
import com.qtz.member.spi.user.dto.UserShop;
import com.qtz.member.spi.user.model.UserKey;
import com.qtz.member.spi.user.service.UserReceivingInfoService;
import com.qtz.member.spi.user.service.UsersService;
import com.qtz.member.spi.user.service.UsersShopService;
import com.qtz.member.spi.userwallet.dto.ReconciliationRecord;
import com.qtz.member.spi.userwallet.enums.YesOrNoEnum;
import com.qtz.member.spi.userwallet.service.ReconciliationRecordService;
import com.qtz.member.spi.userwallet.service.UserWalletService;
import com.qtz.member.spi.utils.UserFiledsUtils;
import com.qtz.ppsh.order.service.dao.OrderDao;
import com.qtz.ppsh.order.service.util.OrderIdFactory;
import com.qtz.ppsh.order.spi.dto.Order;
import com.qtz.ppsh.order.spi.dto.Order.OrderStatus;
import com.qtz.ppsh.order.spi.dto.Order.OrderTypeEnum;
import com.qtz.ppsh.order.spi.dto.Order.ReceivingStatus;
import com.qtz.ppsh.order.spi.dto.Order.RefundStatus;
import com.qtz.ppsh.order.spi.dto.Order.SellerOrderStatus;
import com.qtz.ppsh.order.spi.dto.Order.TransactionStatus;
import com.qtz.ppsh.order.spi.dto.OrderGoods;
import com.qtz.ppsh.order.spi.dto.OrderKey;
import com.qtz.ppsh.order.spi.dto.OrderLog;
import com.qtz.ppsh.order.spi.dto.OrderPrefix;
import com.qtz.ppsh.order.spi.dto.PayOrderModel;
import com.qtz.ppsh.order.spi.dto.PayOrderTypeEnum;
import com.qtz.ppsh.order.spi.page.OrderPage;
import com.qtz.ppsh.order.spi.service.OrderGoodsService;
import com.qtz.ppsh.order.spi.service.OrderLogService;
import com.qtz.ppsh.order.spi.service.OrderService;
import com.qtz.system.spi.jpush.dto.CustomMsg;
import com.qtz.system.spi.jpush.model.MsgOutput;
import com.qtz.system.spi.jpush.service.CustomMsgService;

//import com.qtz.dm.pay.service.UnionpayService;

/**
 * <p>
 * Title:OrderServiceImpl
 * </p>
 * <p>
 * Description:订单服务实现类
 * </p>
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * <p>
 * Company: 深圳市擎天柱信息科技有限公司
 * </p>
 *
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-09-02
 */
@Service("orderServiceImpl")
public class OrderServiceImpl extends BaseServiceImpl<Order, Long> implements OrderService {

    /**
     * 初始化日志对象
     */
	private static Logger log = Logger.getLogger(OrderServiceImpl.class);

    private static Lock lock = new ReentrantLock();// 锁对象
    /**
     * 注入订单DAO接口类
     */

    @Resource(name = "orderDaoImpl")
    private OrderDao dao;

    @Autowired
    private OrderGoodsService orderGoodsService;

    @Autowired
    private StoreGoodsService storeGoodsService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUserService couponUserService;

    @Autowired
    private UserReceivingInfoService receivingInfoService;

    @Autowired
    private OrderLogService orderLogService;
    
    @Autowired
    private UsersService usersService;

    @Autowired
    private UsersShopService usersShopService;

    @Autowired
    private SellerStoreService sellerStoreService;

    @Autowired
    private UserWalletService userWalletService;

    @Autowired
    private JPushMessageService jPushMessageService;
    
    @Autowired
	private CustomMsgService customMsgService;
    

    @Autowired
    private ReconciliationRecordService reconciliationRecordService;
   
    @Autowired
    private IdService idServiceImpl;
   
    /**
     * 【取得】业务DAO对象
     *
     * @return 业务DAO对象
     */
    @Override
    protected BizDao<Order, java.lang.Long> getDao() {
        return dao;
    }



    /**
     * 【互斥锁】
     *
     * @param orderId
     * @return
     * @throws ServiceException
     * @time:2016年1月14日 下午6:04:49
     * @author 涂鑫
     * @version
     */
    private Order lockOrder(Long orderId) throws ServiceException {
        lock.lock();//得到锁
        synchronized (orderId) {
            try {
                //	synchronized (lock) {
                return dao.getLockOrder(orderId);
                //	}
            } catch (DaoException e) {
                log.error(e);
                throw new ServiceException(e);
            }
        }
    }

    /**
     * 提交订单
     */
    @Override
    public Long saveSubOrder(Map<Long, Integer> goodsMaps, Long receivingId,
                             Long couponId, User user, Integer orderType, Long makeTime,
                             String note) throws ServiceException {
        log.info("正在提交订单....");
        // if(makeEndTime==null && makeStartTime!=null || makeStartTime==null &&
        // makeEndTime!=null){
        // throw new ServiceException("错误预约时间...");
        // }
        if (makeTime == null) {
            makeTime = System.currentTimeMillis() + (30 * 60 * 1000);
            // throw new ServiceException("错误预约时间");
        }
        if ((makeTime.longValue() + 5 * 60 * 1000) < System.currentTimeMillis()) {
            throw new ServiceException(ExceptionCode.ERROR_MAKETIME, "错误的预约时间");
        }
        if (user.getUserType().intValue() == UserType.BUSINESS.value()) {
            throw new ServiceException(ExceptionCode.ORDER_SUB_SELLER_ERROR,
                    "订单提交有误  商家不允许买");
        }
        if (goodsMaps == null || goodsMaps.isEmpty() || orderType == null) {
            throw new ServiceException(ExceptionCode.NULL_EXCEPTION,
                    "null exception");
        }
        if (orderType.intValue() != OrderTypeEnum.eatIn.value()
                && orderType.intValue() != OrderTypeEnum.takeOut.value()) {
            throw new ServiceException(ExceptionCode.ORDER_SUB_ERROR,
                    "订单提交有误  类型不正确");
        }
        if (orderType.intValue() == OrderTypeEnum.takeOut.value()) {
            if (receivingId == null) {
                throw new ServiceException(ExceptionCode.NULL_EXCEPTION,
                        "null exception");
            }
        }
        Long orderId = getOrderId(OrderIdFactory.getOrderId());
        Set<Entry<Long, Integer>> entrySet = goodsMaps.entrySet();
        Iterator<Entry<Long, Integer>> iterator = entrySet.iterator();
        double orderPrice = 0.0;// 订单总价
        long sellerId = 0l;
        int goodsCount = 0;
        while (iterator.hasNext()) {
            Entry<Long, Integer> next = iterator.next();
            OrderGoods orderGoods = new OrderGoods();
            orderGoods.setOrderId(orderId);
            StoreGoods storeGoods = storeGoodsService.findVo(next.getKey(),
                    null);
            if (storeGoods == null) {
                continue;
            }
            if (storeGoods.getStatus().intValue() == GoodsStatus.down.value()) {
                throw new ServiceException(ExceptionCode.GOODS_DOWN, "商品下架");
            }
            orderGoods.setGoodsName(storeGoods.getGoodsName());// 商品名字
            Integer goodsNum = next.getValue();
            orderGoods.setGoodsNum(goodsNum.intValue());// 商品单个数量
            goodsCount = goodsCount + goodsNum.intValue();
            orderGoods.setGoodsPrice(storeGoods.getPrice());// 商品单价
            double totalPrice = ArithUtil.mul(orderGoods.getGoodsNum(), orderGoods
                    .getGoodsPrice().doubleValue());
            totalPrice = ArithUtil.round(totalPrice, 2, BigDecimal.ROUND_HALF_UP);
            orderGoods.setGoodsTotalPrice(totalPrice);// 单个商品总价
            orderGoods.setGoodsId(storeGoods.getDmId());
            if(orderGoods.getDmId()==null||orderGoods.getDmId()<1){
                orderGoods.setDmId(this.idServiceImpl.generateId());
                orderGoodsService.addVo(orderGoods);
            }else{
                orderGoodsService.modVo(orderGoods);
            }

            orderPrice = ArithUtil.add(orderPrice, totalPrice);
            if (sellerId == 0L) {
                sellerId = storeGoods.getUserId().longValue();
            } else {
                if (sellerId != storeGoods.getUserId().longValue()) {
                    // 提交的订单存在不同商家
                    throw new ServiceException(ExceptionCode.ORDER_SUB_ERROR,
                            "错误提交订单  商品商家不一致");
                }
            }
        }
        SellerStore sellerStore = sellerStoreService.getSellerStore(sellerId);
        
        if (orderType.intValue() == OrderTypeEnum.eatIn.value()) {
            // 如果是堂食订单
            if (sellerStore.getIsStop().intValue() == IsStop.NO.value()) {

                // 该订单不支持到店
                throw new ServiceException(ExceptionCode.STORE_ISSTOP_1,
                        "不支持到店");

            }
        } else if (orderType.intValue() == OrderTypeEnum.takeOut.value()) {
            if (sellerStore.getIsSend().intValue() == IsSend.NO.value()) {
                // 该商铺不支持派送
                throw new ServiceException(ExceptionCode.STORE_ISSEND_1,
                        "不支持派送");

            }
        }

        Long nowTime = System.currentTimeMillis();
        if (nowTime.longValue() <= sellerStore.getcBEndTime().longValue()
                && nowTime.longValue() >= sellerStore.getcBStartTime()
                .longValue()) {
            throw new ServiceException(ExceptionCode.STORE_CLOSE_BUSINESS,
                    "店铺非营业中无法下单");
        }
        Order order = null;
        if (orderType.intValue() == OrderTypeEnum.takeOut.value()) {
            UserReceivingInfo userReceivingInfo = new UserReceivingInfo();
            userReceivingInfo = receivingInfoService.findVo(receivingId,
                    userReceivingInfo);
            if (userReceivingInfo == null) {
                throw new ServiceException(ExceptionCode.NULL_EXCEPTION,
                        "收货地址null");
            }
            if (userReceivingInfo.getStatus() != Status.OK.value()) {
                throw new ServiceException(
                        ExceptionCode.RECEIVINGINFO_STATUS_ERROR,
                        "收货地址status错误");
            }
            if (userReceivingInfo.getUserId().longValue() != user.getDmId()
                    .longValue()) {
                throw new ServiceException(ExceptionCode.ORDER_SUB_ERROR,
                        "订单提交有误,收货人错误.");
            }
            order = new Order(orderId, sellerId, couponId, user.getDmId(),
                    orderPrice, orderType, userReceivingInfo.getPhone(),
                    userReceivingInfo.getName(),
                    userReceivingInfo.getAddress(), goodsCount, makeTime,
                    userReceivingInfo.getHouseNumber());
        } else if (orderType.intValue() == OrderTypeEnum.eatIn.value()) {
            order = new Order(orderId, sellerId, couponId, user.getDmId(),
                    orderPrice, orderType, user.getMphonenum(),
                    user.getNickname(), null, goodsCount, makeTime, null);

        }
        order.setNote(note);
        if (orderType.intValue() == OrderTypeEnum.takeOut.value()) {
            // 如果是外卖订单 需要计算餐盒费用 派送费用
            if (sellerStore.getIsSend().intValue() == IsSend.OK.value()
                    && orderPrice < sellerStore.getSendOutUpFee().doubleValue()) {
                throw new ServiceException(ExceptionCode.ORDER_LT_SENDFEE,
                        "订单小于起送价格");
            }
            double tempMealFee = 0d;
            if (orderPrice < sellerStore.getFreeMealFee().doubleValue()) {
                tempMealFee = sellerStore.getMealFee() == null ? 0
                        : sellerStore.getMealFee();
                order.setMealFee(sellerStore.getMealFee());
            }
            double tempSendFee = 0d;
            if (orderPrice < sellerStore.getFreeSendFee().doubleValue()) {
                tempSendFee = sellerStore.getSendFee() == null ? 0
                        : sellerStore.getSendFee();
                order.setSendFee(sellerStore.getSendFee());
            }
            orderPrice = ArithUtil.add(ArithUtil.add(orderPrice, tempMealFee),
                    tempSendFee);
            order.setOrderPrice(orderPrice);
        }
        if (orderPrice >= 5000d) {
            throw new ServiceException(ExceptionCode.PRICE_ERROR, "单笔订单总金额需小于5000元");
        }
        /*if (orderPrice >= 1000d && user.getAuthen() == User.authenStatus.NOAuth.value()) {
            throw new ServiceException(ExceptionCode.PRICE_ERROR, "1000元以上。请实名认证后再消费");
        }新认证已经成功上线，不需要这个判断了*/
        
        try {
            getDao().addVo(order);
        } catch (DaoException e) {
            throw new ServiceException("添加错误", e);
        }
        OrderLog save = new OrderLog();
        save.setOrderId(orderId);
        save.setStatus(PayStatusEnum.PAY_FAILURE.getId());// 初始订单日志未支付
        save.setTime(order.getCrtime());
        // 记录订单日志
        if(save.getDmId()==null||save.getDmId()<1){
            save.setDmId(this.idServiceImpl.generateId());
            orderLogService.addVo(save);
        }else{
            orderLogService.modVo(save);
        }
        // 计算价格
        saveCalculatePaymentPrice(orderId, couponId, order.getUserId());
        return orderId;
    }

    private Long getOrderId(Long orderId) throws ServiceException {
    	if (findVo(orderId, null) != null) {
            getOrderId(OrderIdFactory.getOrderId().longValue());
        }
    	orderId = Long.parseLong(OrderPrefix.PP_USER_ORDER + String.valueOf(orderId));
        return orderId;
    }

    @Override
    public Double saveCalculatePaymentPrice(Long orderId, Long couponId,
                                            Long userId) throws ServiceException {
        // TODO 此处计算支付金额时，有保存到数据库，后面优化代码，应该只单纯计算优惠后金额，在订单支付时，计算一次再最终保存至数据库
        if (orderId == null || userId == null) {
            throw new ServiceException(ExceptionCode.NULL_EXCEPTION,
                    "null exception");
        }
        Order order = findVo(orderId, null);
        if (order.getOrderStatus() != OrderStatus.unpay.getId()) {
            throw new ServiceException(ExceptionCode.ORDER_PAY_ERROR,
                    "订单支付状态错误");
        }
        Double price = null;
        if (couponId == null) {
            price = order.getOrderPrice();
            updateCancelCouponPay(orderId, null, userId);// 取消优惠卷计算
        } else {
            CouponUser where = new CouponUser();
            where.setCouponId(couponId);
            where.setUserId(userId);
            List<CouponUser> findList = couponUserService.findList(where);
            if (findList == null || findList.isEmpty()) {
                throw new ServiceException(ExceptionCode.USER_NO_SUCH_COUPON,
                        "用户无此优惠卷");
            }
            Coupon coupon = new Coupon();
            coupon = couponService.findVo(couponId, coupon);

            if (coupon.getCouponRules().getMonetary() != null
                    && coupon.getCouponRules().getMonetary().doubleValue() > order
                    .getOrderPrice().doubleValue()) {
                throw new ServiceException(
                        ExceptionCode.COUPON_NOT_TO_USE_RULES, "优惠卷未达到使用规则");
            }
            boolean flag = false;
            if (coupon.getType().intValue() == CouponType.archLord.value()) {
                // 霸王卷
                price = ArithUtil.sub(order.getOrderPrice().doubleValue(), coupon
                        .getCouponRules().getFavorableMoney().doubleValue());
                price = price <= 0 ? 0 : price;
                flag = true;

            } else if (coupon.getType().intValue() == CouponType.discount
                    .value()) {
                // 折扣劵
                double dis = ArithUtil.div(coupon.getCouponRules()
                        .getFavorableMoney().doubleValue(), 10.0);
                price = ArithUtil.mul(order.getOrderPrice().doubleValue(), dis);
                flag = true;
            } else if (coupon.getType().intValue() == CouponType.favorable
                    .value()) {
                // 优惠卷
                price = ArithUtil.sub(order.getOrderPrice().doubleValue(), coupon
                        .getCouponRules().getFavorableMoney().doubleValue());
                flag = true;
            }
            if (flag) {
                // 有优惠卷计算 需要计算优惠金额
                price = ArithUtil.round(price, 2, BigDecimal.ROUND_HALF_UP);
                Order update = new Order();
                update.setDmId(orderId);
                update.setCouponId(couponId);
                double couponPrice = ArithUtil.sub(order.getOrderPrice(), price);
                update.setCouponPrice(couponPrice <= 0 ? 0 : couponPrice);
                modVoNotNull(update);
            }
        }
        Order update = new Order();
        update.setDmId(orderId);
        update.setPaymentPrice(price);
        log.error("订单实际支付价格 ："+price+update.getPaymentPrice());
        modVoNotNull(update);
        return price;
    }
    
    @Override
    public Order newCalculatePaymentPrice(Long orderId, Long couponId,Long userId) throws ServiceException {        
        if (orderId == null || userId == null) {
            throw new ServiceException(ExceptionCode.NULL_EXCEPTION,"null exception");
        }
        Order order = findVo(orderId, null);
        if (order.getOrderStatus() != OrderStatus.unpay.getId()) {
            throw new ServiceException(ExceptionCode.ORDER_PAY_ERROR,"订单支付状态错误");
        }
        Double price = null;
        boolean flag = false;
        if (couponId == null) {
        	price = order.getOrderPrice();
            //如果订单生成的时候本来就没有优惠券信息，这里就不需要更改
        	if(null != order.getCouponId()){
        		order.setCouponId(null);
        	}
            if(null != order.getCouponPrice()){
            	order.setCouponPrice(0D);
            }
            updateCancelCouponPay(orderId, null, userId);// 取消优惠卷计算
        } else {
            CouponUser where = new CouponUser();
            where.setCouponId(couponId);
            where.setUserId(userId);
            List<CouponUser> findList = couponUserService.findList(where);
            if (findList == null || findList.isEmpty()) {
                throw new ServiceException(ExceptionCode.USER_NO_SUCH_COUPON,
                        "用户无此优惠卷");
            }
            Coupon coupon = new Coupon();
            coupon = couponService.findVo(couponId, coupon);

            if (coupon.getCouponRules().getMonetary() != null
                    && coupon.getCouponRules().getMonetary().doubleValue() > order
                    .getOrderPrice().doubleValue()) {
                throw new ServiceException(
                        ExceptionCode.COUPON_NOT_TO_USE_RULES, "优惠卷未达到使用规则");
            }
            
            if (coupon.getType().intValue() == CouponType.archLord.value()) {
                // 霸王卷
                price = ArithUtil.sub(order.getOrderPrice().doubleValue(), coupon
                        .getCouponRules().getFavorableMoney().doubleValue());
                price = price <= 0 ? 0 : price;
                flag = true;
            } else if (coupon.getType().intValue() == CouponType.discount
                    .value()) {
                // 折扣劵
                double dis = ArithUtil.div(coupon.getCouponRules()
                        .getFavorableMoney().doubleValue(), 10.0);
                price = ArithUtil.mul(order.getOrderPrice().doubleValue(), dis);
                flag = true;
            } else if (coupon.getType().intValue() == CouponType.favorable
                    .value()) {
                // 优惠卷
                price = ArithUtil.sub(order.getOrderPrice().doubleValue(), coupon
                        .getCouponRules().getFavorableMoney().doubleValue());
                flag = true;
            }
        }
        
        if (flag) {        	
            // 有优惠卷计算 需要计算优惠金额
            price = ArithUtil.round(price, 2, BigDecimal.ROUND_HALF_UP);           
            double couponPrice = ArithUtil.sub(order.getOrderPrice(), price);
            order.setCouponPrice(couponPrice <= 0 ? 0 : couponPrice);
            order.setCouponId(couponId);
        }
        //修改订单的优惠券、优惠价格、实际支付金额
        order.setPreferentialPrice(price);
        order.setPaymentPrice(price);
        //每次都先重置这三个字段
        order.setTags("");
        order.setDeductionPrice(0d);
        order.setDiscountPercentByNow(0);
        modVoNotNull(order);        
        return order;
    }

    @Override
    public void updateCancelCouponPay(Long orderId, Long couponId, Long userId)
            throws ServiceException {
        if (orderId == null || userId == null) {
            throw new ServiceException(ExceptionCode.NULL_EXCEPTION,
                    "null exception");
        }
        Order order = findVo(orderId, null);
        if (order == null) {
            throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.unpay.getId()) {
            throw new ServiceException(ExceptionCode.ORDER_PAY_ERROR,
                    "订单支付状态错误");
        }
        if (order.getUserId().longValue() != userId.longValue()) {
            throw new ServiceException(ExceptionCode.ORDER_USER_FALSENESS,
                    "下单用户不正确");
        }
        Double price = order.getOrderPrice();
        try {
            dao.updateCancelOrderCoupon(orderId, price);
        } catch (DaoException e) {
            log.debug("取消失败.");
            throw new ServiceException(e);
        }
    }

    @Override
    public List<Map<Object, Object>> queryCountOrderMonth(Long userId, String month, int pageIndex, int pageSize) throws ServiceException {
        try {
            return dao.queryCountOrderMonth(userId, month, pageIndex, pageSize);
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public JSONArray getUsableCoupon(Long orderId, Long userId)
            throws ServiceException {
        if (orderId == null || userId == null) {
            throw new ServiceException(ExceptionCode.NULL_EXCEPTION,
                    "null exception");
        }
        Order order = findVo(orderId, null);
        if (order == null) {
            throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        OrderGoods where = new OrderGoods();
        where.setOrderId(orderId);
        List<OrderGoods> ordList = orderGoodsService.findList(where);
        for (OrderGoods orderGoods : ordList) {
            StoreGoods storeGoods = storeGoodsService.findVo(
                    orderGoods.getGoodsId(), null);// 商品
            if (storeGoods.getIsCoupon().intValue() == IsCoupon.no.value()) {
                // 有一个商品不支持优惠卷 则都不支持
                log.debug("商品不支持优惠卷" + storeGoods.getGoodsName());
                return new JSONArray();
            }
        }
        JSONArray usableCoupon = couponUserService.getUsableCoupon(
                order.getSellerId(), userId);
        Iterator<Object> iterator = usableCoupon.iterator();
        long dmId = 0;
        while (iterator.hasNext()) {

            Object object = iterator.next();
            JSONObject json = JSONObject.parseObject(object.toString());
            if ((Long) json.get(CouponKey.dmId) == dmId) {
                iterator.remove();// 过滤相同优惠劵
                continue;
            }
            long nowTime = System.currentTimeMillis();
            if (json.containsKey(CouponKey.startTime)
                    && json.containsKey(CouponKey.endTime)) {
                if (Long.valueOf(json.getString(CouponKey.startTime))
                        .longValue() > nowTime
                        || nowTime > Long.valueOf(
                        json.getString(CouponKey.endTime)).longValue()) {
                    log.info("优惠卷使用日期还没开始无法使用. 过滤");
                    iterator.remove();
                    continue;
                }

            }
            dmId = (Long) json.get(CouponKey.dmId);
            if (json.containsKey(CouponKey.type)) {
                if ((int) json.get(CouponKey.type) == CouponType.gift.value()) {
                    iterator.remove();
                    continue;
                }
            }
            // 这里会有一个null 异常 因为霸王卷是直接使用的 不存在monetary 属性
            CouponRules couponRules = JSON.parseObject(
                    json.get(CouponKey.couponRules).toString(),
                    CouponRules.class);
            if ((int) json.get(CouponKey.type) == CouponType.archLord.value()) {
                if (couponRules.getFavorableMoney().doubleValue() < order
                        .getOrderPrice().doubleValue()) {
                    // //霸王卷 直接使用
                    //
                    log.debug("未达到优惠卷规则  过滤 [" + json.get(RespKey.dmId) + "]");
                    iterator.remove();
                    continue;
                } else {
                    continue;
                }

            }
            log.debug("订单总额	[" + order.getOrderPrice() + "] 优惠价格满["
                    + couponRules.getMonetary());
            if (couponRules.getMonetary().doubleValue() > order.getOrderPrice()
                    .doubleValue()) {
                log.debug("未达到优惠卷规则  过滤 [" + json.get(RespKey.dmId) + "]");
                iterator.remove();
            }
        }
        return usableCoupon;
    }

    @Override
    public Pager<Order, Long> getOrderList(Long userId, Integer orderStatus,
                                           Integer pageIndex, Integer orderType) throws ServiceException {
        try {
            OrderPage page = new OrderPage();
            User user = usersService.getUser(userId);
            if (user == null) {
                user = usersShopService.getUser(userId);
            }
            if (user.getUserType().intValue() == UserType.PERSON.value()) {
                page.setUserId(userId);
            } else {
                page.setSellerId(userId);
            }


            page.setOrderType(orderType);

            page.setNowPage(pageIndex);
            page.setOrderField(OrderKey.crtime);
            page.setOrderDirection(false);
            List<Order> list = null;
            Pager<Order, Long> query = null;
            if (Order.OrderStatus.failure.getId() == orderStatus.intValue()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("userId", userId);
                map.put("orderType", orderType);
                map.put("pageIndex", (pageIndex - 1) * 20);
                list = dao.queryTransactionClose(map);
            } else if (orderStatus.intValue() == -1) {
                query = query(page, null);
                list = query.getList();
            } else {
                page.setOrderStatus(orderStatus);
                query = query(page, null);
                list = query.getList();
            }
            if (null == query)
                query = new OrderPage();
            return getOrderAndOrderGoods(list,query);
        } catch (ServiceException e) {
            throw new ServiceException(e);
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void updateReceivingOrder(Long orderId, Long sellerId) throws ServiceException {
        Order order = null;
        try {
            order = checkOrder(orderId, sellerId);
            if (order.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                throw new ServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态不正确");
            }
            Order update = new Order();
            update.setDmId(orderId);
            update.setSellerOrderStatus(SellerOrderStatus.havaOrder.value());
            update.setReceivingStatus(ReceivingStatus.notSpending.value());
            update.setOrderStatus(OrderStatus.reorder.getId());
            modVoNotNull(update);
            OrderLog updateOrderLog = new OrderLog();
            updateOrderLog.setOrderId(orderId);
            updateOrderLog.setTime(System.currentTimeMillis());
            updateOrderLog.setStatus(OrderStatus.reorder.getId());
            if(updateOrderLog.getDmId()==null||updateOrderLog.getDmId()<1){
                updateOrderLog.setDmId(this.idServiceImpl.generateId());
                orderLogService.addVo(updateOrderLog);
            }else{
                orderLogService.modVo(updateOrderLog);
            }
            // 钱包划账
            if (System.currentTimeMillis() > order.getMakeTime().longValue()) {
                // 预约时间小于当前时间，则用当前时间 userWalletService
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), System.currentTimeMillis(), order.getPayType().intValue());
            } else {
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), order.getMakeTime(), order.getPayType().intValue());
            }
            
            String sid = usersService.getUserLastOperationSid(order.getUserId());
			if(sid== null){
				sid = usersShopService.getUserLastOperationSid(order.getUserId());
			}
			
			User user = this.usersService.getUser(order.getUserId());
			if(user == null){
				user = this.usersShopService.getUser(order.getUserId());
			}
	        CustomMsg cm = this.customMsgService.findByCode(RespCode.order_receiving);
	        Map<String, String> extra = new HashMap<String, String>();
            extra.put("code", RespCode.order_receiving);
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("data", JSONObject.toJSONString(ex));
            extra.put("message", cm.getMessage());
	        //发送极光消息
	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
			jPushMessageService.sendMessage(jpushDto);
            
        } finally {
            lock.unlock();
        }

    }
    
    @Transactional
    @Override
    public void newReceivingOrder(Long orderId, Long sellerId) throws ServiceException {
    	Order order = null;
        try {
            order = checkOrder(orderId, sellerId);
            if (order.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                throw new ServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态不正确");
            }
            Order orderUpdate = new Order();
            orderUpdate.setDmId(orderId);
            orderUpdate.setSellerOrderStatus(SellerOrderStatus.havaOrder.value());
            orderUpdate.setReceivingStatus(ReceivingStatus.notSpending.value());
            orderUpdate.setOrderStatus(OrderStatus.reorder.getId());
            modVoNotNull(orderUpdate);
            OrderLog updateOrderLog = new OrderLog();
            updateOrderLog.setOrderId(orderId);
            updateOrderLog.setTime(System.currentTimeMillis());
            updateOrderLog.setStatus(OrderStatus.reorder.getId());
            if(updateOrderLog.getDmId()==null||updateOrderLog.getDmId()<1){
                updateOrderLog.setDmId(this.idServiceImpl.generateId());
                orderLogService.addVo(updateOrderLog);
            }else{
                orderLogService.modVo(updateOrderLog);
            }
            //找到当前订单商家的二级分类
 			SellerStore sstore = sellerStoreService.getSellerStore(order.getSellerId());
 			if (null == sstore) {
 				throw new ServiceException(ExceptionCode.NULL_EXCEPTION, "店铺不存在,商家ID：" + order.getSellerId());
 			}else if(null == sstore.getTwoCategoryId()){
 				throw new ServiceException(ExceptionCode.NULL_EXCEPTION, "店铺的二级标签不存在,商家ID：" + order.getSellerId());
 			}
 			//////////////为了避免订单的preferentialPrice为0，这里重新计算一遍//////////////////
 			
			if(null != order.getCouponId() && null != order.getCouponPrice() && order.getCouponPrice().longValue()>0){
				order.setPreferentialPrice(ArithUtil.sub(order.getOrderPrice(),order.getCouponPrice()));  //订单总金额  - 优惠券金额
			}else{
				order.setPreferentialPrice(order.getOrderPrice());
			}
 			
 			
 			///////////////////////////////
            // 钱包划账
            if (System.currentTimeMillis() > order.getMakeTime().longValue()) {
                // 预约时间小于当前时间，则用当前时间 userWalletService
                this.userWalletService.newAccectOrder(orderId + "",order.getOrderPrice(),order.getPreferentialPrice(),order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), System.currentTimeMillis(), order.getPayType().intValue(),sstore.getTwoCategoryId());
            } else {
                this.userWalletService.newAccectOrder(orderId + "",order.getOrderPrice(),order.getPreferentialPrice(),order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), order.getMakeTime(), order.getPayType().intValue(),sstore.getTwoCategoryId());
            }


			String sid = usersService.getUserLastOperationSid(order.getUserId());
			if(sid== null){
				sid = usersShopService.getUserLastOperationSid(order.getUserId());
			}
			
			User user = this.usersService.getUser(order.getUserId());
			if(user == null){
				user = this.usersShopService.getUser(order.getUserId());
			}
	        CustomMsg cm = this.customMsgService.findByCode(RespCode.order_receiving);
	        Map<String, String> extra = new HashMap<String, String>();
	        extra.put("code", RespCode.order_receiving);
	        MsgOutput ex = new MsgOutput();
	        ex.setId(orderId + "");
	        extra.put("message", cm.getMessage());
	        extra.put("data", JSONObject.toJSONString(ex));
	        extra.put("message", cm.getMessage());
	        //发送极光消息
	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
			jPushMessageService.sendMessage(jpushDto);
			
                       
        } finally {
            lock.unlock();
        }
    }    

    private Order checkOrder(Long orderId, Long sellerId)
            throws ServiceException {
        Order order = lockOrder(orderId.longValue());
        if (order == null) {
            log.info("订单不存在.");
            throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        
        UserShop sellerUser = usersShopService.getUser(sellerId);
        
        if (sellerUser == null) {
            log.info("商家不存在.");
            throw new ServiceException(ExceptionCode.USER_NULL_EXCEPTION,
                    "用户不存在");
        }
        if (sellerUser.getUserType().intValue() == UserType.PERSON.value()) {
            log.info("账户类型不匹配,只有商家才能接单 拒单");
            throw new ServiceException(ExceptionCode.USERTYPE_DONT_MATCH,
                    "用户类型不匹配");
        }
        if (order.getSellerId().longValue() != sellerUser.getDmId().longValue()) {
            log.info("错误订单,该订单不是请求商家所属.[订单商家是" + order.getSellerId()
                    + ",实际接单商家是" + sellerId + "]");
            throw new ServiceException(ExceptionCode.ORDER_SELLER_ERROR,
                    "接单 拒单 有误");
        }
        if (order.getPayStatus().intValue() != PayStatusEnum.PAY_SUCCESS
                .getId()) {
            log.info("订单处于未支付状态或者处于退款状态不能接单.");
            throw new ServiceException(ExceptionCode.ORDER_PAY_ERROR_1, "订单未支付");
        }
        if (null != order.getSellerOrderStatus()
                && order.getSellerOrderStatus() != SellerOrderStatus.donHavaOrder
                .value()) {
            log.info("订单接单状态有误.");
            throw new ServiceException(
                    ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR,
                    "订单接单状态有误");
        }
        return order;
    }

    @Override
    public void updateRefusedOrder(Long orderId, Long sellerId)
            throws ServiceException {
        try {
            Order orders = checkOrder(orderId, sellerId);
            if (orders.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                throw new ServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态错误不能拒绝");
            }
            Order update = new Order();
            update.setDmId(orderId);
            update.setSellerOrderStatus(SellerOrderStatus.refusedOrder.value());
//		 update.setReceivingStatus(ReceivingStatus.notSpending.value());
            update.setOrderStatus(OrderStatus.failure.getId());
            modVoNotNull(update);
            OrderLog updateOrderLog = new OrderLog();
            updateOrderLog.setOrderId(orderId);
            updateOrderLog.setTime(System.currentTimeMillis());
            updateOrderLog.setStatus(OrderStatus.refused.getId());
            if(updateOrderLog.getDmId()==null||updateOrderLog.getDmId()<1){
                updateOrderLog.setDmId(this.idServiceImpl.generateId());
                orderLogService.addVo(updateOrderLog);
            }else{
                orderLogService.modVo(updateOrderLog);
            }
            // 商家拒绝订单钱包划账
            this.userWalletService.saveNoAccOrder(orderId + "", orders.getUserId());

            
            String sid = usersService.getUserLastOperationSid(orders.getUserId());
			if(sid== null){
				sid = usersShopService.getUserLastOperationSid(orders.getUserId());
			}
			
			User user = this.usersService.getUser(orders.getUserId());
			if(user == null){
				user = this.usersShopService.getUser(orders.getUserId());
			}
	        CustomMsg cm = this.customMsgService.findByCode(RespCode.refuse_place_an_order);
	        Map<String, String> extra = new HashMap<String, String>();
            extra.put("code", RespCode.refuse_place_an_order);
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("message", cm.getMessage());
            extra.put("data", JSONObject.toJSONString(ex));
	         
	        //发送极光消息
	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
			jPushMessageService.sendMessage(jpushDto);
            
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateCancelOrder(Long orderId, Long persionId)
            throws ServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order.getUserId().longValue() != persionId.longValue()) {
                throw new ServiceException(ExceptionCode.ORDER_USER_FALSENESS,
                        "订单下单用户不正确");
            }
            if (order.getPayStatus().intValue() == PayStatusEnum.PAY_FAILURE
                    .getId()) {
                // 订单未支付取消订单
                // 删除订单商品
                // this.orderGoodsService.delOrderGoodsByOrderId(orderId);
                OrderLog orderLog = new OrderLog();
                orderLog.setOrderId(orderId);
                orderLog.setStatus(OrderStatus.failure.getId());
                orderLog.setTime(System.currentTimeMillis());
                if(orderLog.getDmId()==null||orderLog.getDmId()<1){
                    orderLog.setDmId(this.idServiceImpl.generateId());
                    orderLogService.addVo(orderLog);
                }else{
                    orderLogService.modVo(orderLog);
                }
                // 删除订单日志
                // this.orderLogService.delOrderLogByOrderId(orderId);
                Order updateOrder = new Order();
                updateOrder.setDmId(orderId);
                updateOrder.setOrderStatus(OrderStatus.failure.getId());
                // 删除订单
                // delId(orderId);
                modVoNotNull(updateOrder);
            } else if (order.getPayStatus().intValue() == PayStatusEnum.PAY_SUCCESS
                    .getId()) {
                log.info("订单已经支付 ");
                if (order.getOrderStatus().intValue() == OrderStatus.pay_dont_answer_sheet
                        .getId()) {
                    log.info("支付未接单");

                    Order update = new Order();
                    update.setDmId(orderId);
                    update.setOrderStatus(OrderStatus.failure.getId());// 取消订单
                    // 直接交易关闭
                    update.setTransactionStatus(TransactionStatus.failure.value());
                    this.modVoNotNull(update);

                    OrderLog saveOrderLog = new OrderLog();
                    saveOrderLog.setOrderId(orderId);
                    long time = System.currentTimeMillis();
                    saveOrderLog.setTime(time);
                    saveOrderLog.setStatus(OrderStatus.failure.getId());
                    if(saveOrderLog.getDmId()==null||saveOrderLog.getDmId()<1){
                        saveOrderLog.setDmId(this.idServiceImpl.generateId());
                        orderLogService.addVo(saveOrderLog);
                    }else{
                        orderLogService.modVo(saveOrderLog);
                    }
                    // 取消订单钱包操作
                    this.userWalletService.saveNoAccOrder(orderId + "", order.getUserId());
                   
                    String sid = usersService.getUserLastOperationSid(order.getUserId());
        			if(sid== null){
        				sid = usersShopService.getUserLastOperationSid(order.getUserId());
        			}
        			
        			User user = this.usersService.getUser(order.getUserId());
        			if(user == null){
        				user = this.usersShopService.getUser(order.getUserId());
        			}
        	        CustomMsg cm = this.customMsgService.findByCode(RespCode.order_cancel);
        	        Map<String, String> extra = new HashMap<String, String>();
                    extra.put("code", RespCode.order_cancel);
                    MsgOutput ex = new MsgOutput();
                    ex.setId(orderId + "");
                    extra.put("message", cm.getMessage());
                    extra.put("data", JSONObject.toJSONString(ex));
                    
                    //发送极光消息
        	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
        			jPushMessageService.sendMessage(jpushDto);
                    
                    return;
                } else {
                    throw new ServiceException(
                            ExceptionCode.ORDERS_STATUS_CANNOT_BE_CANCELLED,
                            "该订单状态无法取消");
                }

            }
        } finally {
            lock.unlock();
        }
        // if(order.getSellerOrderStatus()!=null){
        // throw new ServiceException("服务器异常.");
        // }

    }


    @Override
    public void updateConfirmOrder(Long orderId, Long persionId)
            throws ServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order == null) {
                log.debug("订单不存在.");
                throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
            }
            if (order.getOrderStatus().intValue() != OrderStatus.reorder.getId()) {
                log.debug("状态不正确");
                throw new ServiceException(ExceptionCode.ORDER_UNPAY, "订单未支付不能接单");
            }
            if (order.getUserId().longValue() != persionId.longValue()) {
                log.debug("订单买家不正确");
                throw new ServiceException(ExceptionCode.ORDER_USER_FALSENESS,
                        "下单用户不正确.");
            }
            Order updateOrder = new Order();
            updateOrder.setDmId(orderId);
            updateOrder.setReceivingStatus(ReceivingStatus.hasSpending.value());
            updateOrder.setTransactionStatus(TransactionStatus.success.value());
            updateOrder.setOrderStatus(OrderStatus.success.getId());
            modVoNotNull(updateOrder);
            long time = System.currentTimeMillis();
            OrderLog saveOrderLog = new OrderLog();
            saveOrderLog.setStatus(OrderStatus.success.getId());
            saveOrderLog.setTime(time);
            saveOrderLog.setOrderId(orderId);
            if(saveOrderLog.getDmId()==null||saveOrderLog.getDmId()<1){
                saveOrderLog.setDmId(this.idServiceImpl.generateId());
                orderLogService.addVo(saveOrderLog);
            }else{
                orderLogService.modVo(saveOrderLog);
            }
        } finally {
            lock.unlock();
        }
        // //商家钱包更新
        // Wallet sellerWallet =
        // walletService.getWallet(order.getSellerId());//该方法 会创建钱包
        //
        // Wallet updateSellerWallet=new Wallet();
        // updateSellerWallet.setDmId(sellerWallet.getDmId());
        // updateSellerWallet.setMoney(Arith.add(updateSellerWallet.getMoney(),
        // order.getPaymentPrice()));
        // walletService.modVoNotNull(sellerWallet);
        //
        // //商家钱包日志
        // walletLogService.delWalletLog(orderId, order.getSellerId());
        // WalletLog saveSellerWalletLog=new
        // WalletLog(order.getSellerId(),WalletLogType.income.value(),orderId+"",order.getPaymentPrice());
        // walletLogService.save(saveSellerWalletLog);
    }

    @Override
    public void updateApplyRefund(Long userId, Long orderId, String refundNote) throws ServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order == null) {
                log.debug("订单不存在");
                throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
            }
            if (order.getUserId().longValue() != userId.longValue()) {
                log.debug("下单用户不正确");
                throw new ServiceException(ExceptionCode.ORDER_USER_FALSENESS,
                        "下单用户不正确");
            }
            if (order.getOrderStatus().intValue() == OrderStatus.unpay.getId()
                    || order.getOrderStatus().intValue() == OrderStatus.failure
                    .getId()
                    || order.getOrderStatus().intValue() == OrderStatus.success
                    .getId()) {
                log.debug("不允许退款");
                throw new ServiceException(ExceptionCode.NO_REFUND, "不允许退款");
            }

            //检查订单是否还处理对账中，若存在可退款，若不存在不允许退款
            ReconciliationRecord record = new ReconciliationRecord();
            record.setSaleOrdersId(orderId.toString());
            record.setUserType(UserType.BUSINESS.value());
            List<ReconciliationRecord> records = reconciliationRecordService.findList(record);
            Integer count = reconciliationRecordService.orderIsFrozen(orderId);
            if (count > 0) {
                log.debug("订单已完成，不能退款");
                throw new ServiceException(ExceptionCode.ORDERFINISHED_REJECTREIM, "订单已完成，不能退款");
            }

            Order updateOrder = new Order();
            updateOrder.setDmId(orderId);
            updateOrder.setOrderStatus(OrderStatus.applyRefund.getId());
            updateOrder.setRefundStatus(RefundStatus.applyRefund.value());
            updateOrder.setRefundNote(refundNote);
            modVoNotNull(updateOrder);

            OrderLog saveOrderLog = new OrderLog();
            saveOrderLog.setStatus(OrderStatus.applyRefund.getId());
            saveOrderLog.setTime(System.currentTimeMillis());
            saveOrderLog.setNotes(refundNote);
            saveOrderLog.setOrderId(orderId);
            if(saveOrderLog.getDmId()==null||saveOrderLog.getDmId()<1){
                saveOrderLog.setDmId(this.idServiceImpl.generateId());
                orderLogService.addVo(saveOrderLog);
            }else{
                orderLogService.modVo(saveOrderLog);
            }

            for (int i = 0; i < records.size(); i++) {
                ReconciliationRecord r = records.get(i);
                r.setOrderStatus(YesOrNoEnum.NO.getValue());//申请退款中
                reconciliationRecordService.modVoNotNull(r);
            }

            
            String sid = usersService.getUserLastOperationSid(order.getUserId());
			if(sid== null){
				sid = usersShopService.getUserLastOperationSid(order.getUserId());
			}
			
			User user = this.usersService.getUser(order.getUserId());
			if(user == null){
				user = this.usersShopService.getUser(order.getUserId());
			}
	        CustomMsg cm = this.customMsgService.findByCode(RespCode.apply_for_refund);
	        Map<String, String> extra = new HashMap<String, String>();
            extra.put("code", RespCode.apply_for_refund);
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("message", cm.getMessage());
            extra.put("data", JSONObject.toJSONString(ex));
	        //发送极光消息
	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
			jPushMessageService.sendMessage(jpushDto);
            
            
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateAgreedRefund(Long sellerId, Long orderId, Integer stype,
                                   String message) throws ServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order == null) {
                log.debug("订单不存在");
                throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
            }
            if (order.getSellerId().longValue() != sellerId.longValue()) {
                log.debug("订单不属于商家");
                throw new ServiceException(ExceptionCode.ORDER_SELLER_ERROR,
                        "订单不属于商家");
            }
            if (order.getOrderStatus().intValue() != OrderStatus.applyRefund
                    .getId()) {
                log.debug("订单状态不是申请退款");
                throw new ServiceException(ExceptionCode.ORDER_STATUS_ERROR,
                        "订单状态错误");
            }

            ReconciliationRecord record = new ReconciliationRecord();
            record.setSaleOrdersId(orderId.toString());
            //record.setUserType(com.qtz.dm.user.vo.User.UserType.Business.value());
            
            List<ReconciliationRecord> records = reconciliationRecordService.findList(record);
            
            Map<String, String> extra = new HashMap<String, String>();
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("data", JSONObject.toJSONString(ex));
            //是否同意退款
            if (stype == 1) {
                // 不同意退款
                Order updateOrder = new Order();
                updateOrder.setDmId(orderId);
                updateOrder.setOrderStatus(OrderStatus.reorder.getId());
                updateOrder.setRefundStatus(RefundStatus.refusedRefund.value());
                modVoNotNull(updateOrder);
                OrderLog saveOrderLog = new OrderLog();
                saveOrderLog.setStatus(OrderStatus.applyUnAgrenRefund.getId());
                saveOrderLog.setTime(System.currentTimeMillis());
                saveOrderLog.setOrderId(orderId);
                saveOrderLog.setNotes(message);
                if(saveOrderLog.getDmId()==null||saveOrderLog.getDmId()<1){
                    saveOrderLog.setDmId(this.idServiceImpl.generateId());
                    orderLogService.addVo(saveOrderLog);
                }else{
                    orderLogService.modVo(saveOrderLog);
                }

                for (int i = 0; i < records.size(); i++) {
                    ReconciliationRecord r = records.get(i);
                    r.setOrderStatus(YesOrNoEnum.YES.getValue());//未退款
                    reconciliationRecordService.modVoNotNull(r);
                }

                String sid = usersService.getUserLastOperationSid(order.getUserId());
    			if(sid== null){
    				sid = usersShopService.getUserLastOperationSid(order.getUserId());
    			}
    			
    			User user = this.usersService.getUser(order.getUserId());
    			if(user == null){
    				user = this.usersShopService.getUser(order.getUserId());
    			}
    	        CustomMsg cm = this.customMsgService.findByCode(RespCode.no_agree_to_refund);
    	        extra.put("code", RespCode.no_agree_to_refund);
    	        extra.put("message", cm.getMessage());
    	        //发送极光消息
    	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
    			jPushMessageService.sendMessage(jpushDto);
                
                
            } else {
                //检查订单是否还处理对账中，若存在可退款，若不存在不允许退款
            	Integer count = reconciliationRecordService.orderIsFrozen(orderId);
                if (count > 0) {
                    log.debug("订单已完成，不能退款");
                    throw new ServiceException(ExceptionCode.ORDERFINISHED_REJECTREIM, "订单已完成，不能退款");
                }

                // 同意退款
                Order updateOrder = new Order();
                updateOrder.setDmId(orderId);
                updateOrder.setOrderStatus(OrderStatus.failure.getId());
                updateOrder.setRefundStatus(RefundStatus.agreedRefund.value());
                modVoNotNull(updateOrder);
                OrderLog saveOrderLog = new OrderLog();
                saveOrderLog.setStatus(RefundStatus.agreedRefund.value());
                saveOrderLog.setTime(System.currentTimeMillis());
                saveOrderLog.setOrderId(orderId);
                if(saveOrderLog.getDmId()==null||saveOrderLog.getDmId()<1){
                    saveOrderLog.setDmId(this.idServiceImpl.generateId());
                    orderLogService.addVo(saveOrderLog);
                }else{
                    orderLogService.modVo(saveOrderLog);
                }
                this.userWalletService.saveAccBackMoney(orderId + "", order.getUserId());

                
                
                String sid = usersService.getUserLastOperationSid(order.getUserId());
    			if(sid== null){
    				sid = usersShopService.getUserLastOperationSid(order.getUserId());
    			}
    			
    			User user = this.usersService.getUser(order.getUserId());
    			if(user == null){
    				user = this.usersShopService.getUser(order.getUserId());
    			}
    	        CustomMsg cm = this.customMsgService.findByCode(RespCode.agree_to_refund);
    	        extra.put("code", RespCode.agree_to_refund);
    	        extra.put("message", cm.getMessage());
    	        //发送极光消息
    	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
    			jPushMessageService.sendMessage(jpushDto);

            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Order> findOrderClose(Long ctime) throws ServiceException {

        try {
            return dao.findOrderClose(ctime);
        } catch (DaoException e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    @Override
    public boolean queryOrderPaymentStatus(Long orderId, Integer paymentType,
                                           Integer resultDeal) throws ServiceException {
        throw new ServiceException(-100137, "处理中,请稍候");//该接口后续再处理
    }

    @Override
    public Map<String, Object> querySumTurnover(Long time)
            throws ServiceException {

        return null;
    }

    @Override
    public int findNewOrderCoupon(Integer orderType, Long userId)
            throws ServiceException {
        Order where = new Order();
        where.setOrderType(orderType);
        where.setSellerId(userId);
        where.setOrderStatus(OrderStatus.pay_dont_answer_sheet.getId());
        try {
            Long count = getDao().findCount(where);

            return Integer.parseInt((count == null ? 0 : count) + "");
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public List<Order> queryUser(Long sellerid, int pageIndex)
            throws ServiceException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sellerId", sellerid);
        map.put("pageIndex", (pageIndex - 1) * 20);

        try {
            return dao.querySellerCummer(map);
        } catch (DaoException e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    @Override
    public List<Order> queryTempOrder() throws ServiceException {
        try {
            return dao.queryTempOrder();
        } catch (DaoException e) {
            throw new ServiceException(e);
        }

    }

    @Override
    public List<Map<String, Object>> getOrdersToExport(Map<String, Object> param)
            throws ServiceException {
        try {
            return dao.getOrdersToExport(param);
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public PayOrderModel findAndCheckOrder(Long orderId) throws ServiceException {
        Order order = this.findVo(orderId, null);
        if (order == null) {
            throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        if (order.getOrderStatus() != Order.OrderStatus.unpay.getId()) {
            throw new ServiceException(ExceptionCode.ORDER_STATUS_ERROR, "订单已支付或关闭");
        }
        PayOrderModel model = new PayOrderModel();
        model.setOrderId(String.valueOf(orderId));
        model.setTime(order.getCrtime());
        model.setOrderName("胖胖生活订单");
        model.setOrderType(PayOrderTypeEnum.ORDER);
        Double price = order.getPaymentPrice();
        model.setYuanPrice(ArithUtil.moneyFormat(price));
        model.setFenPrice(String.valueOf(ArithUtil.yuanToFen(price)));
        model.setPayType(order.getPayType());
        model.setThirdSn(order.getThreeSerialNumber());
        model.setUserId(order.getUserId());
        model.setSellerId(order.getSellerId());
        return model;
    }

	@Override
	public Order queryUserOrder(Long orderId) throws ServiceException {
		Order order = findVo(orderId, null);
		return order;
	}

	@Override
	public void receviceOrder(Long orderId, Integer isAccept,Long sellerId)
			throws ServiceException {
		if(Order.IsAccept.yes.value() == isAccept){
			this.acceptReceiveOrder(orderId, sellerId);
		}
		else if(Order.IsAccept.no.value() == isAccept){
			this.refuseReceiveOrder(orderId, sellerId);
		}
		
	}
	
	@Override
	public void applyRefund(Long orderId,Integer isAccept,
			Long sellerId,String message) throws ServiceException {
		try {
            Order order = lockOrder(orderId.longValue());
            if (order == null) {
                log.debug("订单不存在");
                throw new ServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
            }
            if (order.getSellerId().longValue() != sellerId.longValue()) {
                log.debug("订单不属于商家");
                throw new ServiceException(ExceptionCode.ORDER_SELLER_ERROR,
                        "订单不属于商家");
            }
            if (order.getOrderStatus().intValue() != OrderStatus.applyRefund
                    .getId()) {
                log.debug("订单状态不是申请退款");
                throw new ServiceException(ExceptionCode.ORDER_STATUS_ERROR,
                        "订单状态错误");
            }

            ReconciliationRecord record = new ReconciliationRecord();
            record.setSaleOrdersId(orderId.toString());
            List<ReconciliationRecord> records = reconciliationRecordService.findList(record);

            Map<String, String> extra = new HashMap<String, String>();
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("data", JSONObject.toJSONString(ex));
            //是否同意退款
            if (Order.IsAccept.no.value() == isAccept) {
            	int orderStatus = 0;
//                if(Order.OrderTypeEnum.takeOut.value() == orderType){
                orderStatus = OrderStatus.reorder.getId();			//配送中
//                }else if(Order.OrderTypeEnum.eatIn.value() == orderType){
//                	orderStatus = OrderStatus.reorder.getId();          //待消费
//                }
                // 不同意退款
                Order updateOrder = new Order();
                updateOrder.setDmId(orderId);
                updateOrder.setOrderStatus(orderStatus);
                updateOrder.setRefundStatus(RefundStatus.refusedRefund.value());
                modVoNotNull(updateOrder);
                OrderLog saveOrderLog = new OrderLog();
                saveOrderLog.setStatus(OrderStatus.applyUnAgrenRefund.getId());
                saveOrderLog.setTime(System.currentTimeMillis());
                saveOrderLog.setOrderId(orderId);
                saveOrderLog.setNotes(message);
                if(saveOrderLog.getDmId()==null||saveOrderLog.getDmId()<1){
                    saveOrderLog.setDmId(this.idServiceImpl.generateId());
                    orderLogService.addVo(saveOrderLog);
                }else{
                    orderLogService.modVo(saveOrderLog);
                }

                for (int i = 0; i < records.size(); i++) {
                    ReconciliationRecord r = records.get(i);
                    r.setOrderStatus(YesOrNoEnum.YES.getValue());//未退款
                    reconciliationRecordService.modVoNotNull(r);
                }

                
                String sid = usersService.getUserLastOperationSid(order.getUserId());
    			if(sid== null){
    				sid = usersShopService.getUserLastOperationSid(order.getUserId());
    			}
    			
    			User user = this.usersService.getUser(order.getUserId());
    			if(user == null){
    				user = this.usersShopService.getUser(order.getUserId());
    			}
    	        CustomMsg cm = this.customMsgService.findByCode(RespCode.no_agree_to_refund);
    	        extra.put("code", RespCode.no_agree_to_refund);
    	        extra.put("message", cm.getMessage());
    	        //发送极光消息
    	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
    			jPushMessageService.sendMessage(jpushDto);
   
            } else {
                //检查订单是否还处理对账中，若存在可退款，若不存在不允许退款
                if (records == null || records.size() == 0) {
                    log.debug("订单已完成，不能退款");
                    throw new ServiceException(ExceptionCode.ORDERFINISHED_REJECTREIM, "订单已完成，不能退款");
                }

                // 同意退款
                Order updateOrder = new Order();
                updateOrder.setDmId(orderId);
                updateOrder.setOrderStatus(OrderStatus.failure.getId());
                updateOrder.setRefundStatus(RefundStatus.agreedRefund.value());
                modVoNotNull(updateOrder);
                OrderLog saveOrderLog = new OrderLog();
                saveOrderLog.setStatus(RefundStatus.agreedRefund.value());
                saveOrderLog.setTime(System.currentTimeMillis());
                saveOrderLog.setOrderId(orderId);
                if(saveOrderLog.getDmId()==null||saveOrderLog.getDmId()<1){
                    saveOrderLog.setDmId(this.idServiceImpl.generateId());
                    orderLogService.addVo(saveOrderLog);
                }else{
                    orderLogService.modVo(saveOrderLog);
                }
                this.userWalletService.saveAccBackMoney(orderId + "", order.getUserId());

                String sid = usersService.getUserLastOperationSid(order.getUserId());
    			if(sid== null){
    				sid = usersShopService.getUserLastOperationSid(order.getUserId());
    			}
    			
    			User user = this.usersService.getUser(order.getUserId());
    			if(user == null){
    				user = this.usersShopService.getUser(order.getUserId());
    			}
    	        CustomMsg cm = this.customMsgService.findByCode(RespCode.no_agree_to_refund);
    	        extra.put("code", RespCode.no_agree_to_refund);
    	        extra.put("message", cm.getMessage());
    	        //发送极光消息
    	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
    			jPushMessageService.sendMessage(jpushDto);
            }
        } finally {
            lock.unlock();
        }
		
	}

	
	//同意接单
	private void acceptReceiveOrder(Long orderId,Long sellerId) throws ServiceException{
		Order order = null;
		try {
            order = checkOrder(orderId, sellerId);
            if (order.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                throw new ServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态不正确");
            }
            int orderStatus = 0;
//            if(Order.OrderTypeEnum.takeOut.value() == orderType){ 
            orderStatus = OrderStatus.reorder.getId();			//配送中
//            }else if(Order.OrderTypeEnum.eatIn.value() == orderType){
//            	orderStatus = OrderStatus.reorder.getId();          //待消费
//            }
            Order update = new Order();
            update.setDmId(orderId);
            update.setSellerOrderStatus(SellerOrderStatus.havaOrder.value());
            update.setReceivingStatus(ReceivingStatus.notSpending.value());
            update.setOrderStatus(orderStatus);
            modVoNotNull(update);
            OrderLog updateOrderLog = new OrderLog();
            updateOrderLog.setOrderId(orderId);
            updateOrderLog.setTime(System.currentTimeMillis());
            updateOrderLog.setStatus(orderStatus);
            if(updateOrderLog.getDmId()==null||updateOrderLog.getDmId()<1){
                updateOrderLog.setDmId(this.idServiceImpl.generateId());
                orderLogService.addVo(updateOrderLog);
            }else{
                orderLogService.modVo(updateOrderLog);
            }
            // 钱包划账
            if (System.currentTimeMillis() > order.getMakeTime().longValue()) {
                // 预约时间小于当前时间，则用当前时间 userWalletService
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), System.currentTimeMillis(), order.getPayType().intValue());
            } else {
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), order.getMakeTime(), order.getPayType().intValue());
            }
            
            String sid = usersService.getUserLastOperationSid(order.getUserId());
			if(sid== null){
				sid = usersShopService.getUserLastOperationSid(order.getUserId());
			}
			
			User user = this.usersService.getUser(order.getUserId());
			if(user == null){
				user = this.usersShopService.getUser(order.getUserId());
			}
	        CustomMsg cm = this.customMsgService.findByCode(RespCode.order_receiving);
	        Map<String, String> extra = new HashMap<String, String>();
	        extra.put("code", RespCode.order_receiving);
	        MsgOutput ex = new MsgOutput();
	        ex.setId(orderId + "");
	        extra.put("data", JSONObject.toJSONString(ex));
	        extra.put("message", cm.getMessage());
	        //发送极光消息
	        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
			jPushMessageService.sendMessage(jpushDto);
            
        } finally {
            lock.unlock();
        }
	}
	
	//拒绝接单
	private void refuseReceiveOrder(Long orderId,Long sellerId) throws ServiceException{
		 try {
	            Order orders = checkOrder(orderId, sellerId);
	            if (orders.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
	                throw new ServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态错误不能拒绝");
	            }
	            Order update = new Order();
	            update.setDmId(orderId);
	            update.setSellerOrderStatus(SellerOrderStatus.refusedOrder.value());
	            update.setOrderStatus(OrderStatus.failure.getId());
	            modVoNotNull(update);
	            OrderLog updateOrderLog = new OrderLog();
	            updateOrderLog.setOrderId(orderId);
	            updateOrderLog.setTime(System.currentTimeMillis());
	            updateOrderLog.setStatus(OrderStatus.refused.getId());
	            if(updateOrderLog.getDmId()==null||updateOrderLog.getDmId()<1){
	                updateOrderLog.setDmId(this.idServiceImpl.generateId());
	                orderLogService.addVo(updateOrderLog);
	            }else{
	                orderLogService.modVo(updateOrderLog);
	            }
	            // 商家拒绝订单钱包划账
	            this.userWalletService.saveNoAccOrder(orderId + "", orders.getUserId());

	            
	            String sid = usersService.getUserLastOperationSid(orders.getUserId());
				if(sid== null){
					sid = usersShopService.getUserLastOperationSid(orders.getUserId());
				}
				
				User user = this.usersService.getUser(orders.getUserId());
				if(user == null){
					user = this.usersShopService.getUser(orders.getUserId());
				}
		        CustomMsg cm = this.customMsgService.findByCode(RespCode.refuse_place_an_order);
		        Map<String, String> extra = new HashMap<String, String>();
		        extra.put("code", RespCode.refuse_place_an_order);
		        MsgOutput ex = new MsgOutput();
		        ex.setId(orderId + "");
		        extra.put("data", JSONObject.toJSONString(ex));
		        extra.put("message", cm.getMessage());
		        //发送极光消息
		        JpushDto jpushDto = new JpushDto(user.getUserType(),user.getPlatForm(),cm.getMessage(),sid,extra,Boolean.valueOf(CfgHelper.getValue("jpush.environment")));
				jPushMessageService.sendMessage(jpushDto);
	            
	        } finally {
	            lock.unlock();
	        }
	}

	@Override
	public Pager<Order, Long> getOrderListByIdOrNickname(Long sellerId,String nickname, Long orderId, Integer pageIndex) throws ServiceException {
		OrderPage page = new OrderPage();
		page.setSellerId(sellerId);
		page.setNowPage(pageIndex);
		page.setOrderField(OrderKey.crtime);
		page.setOrderDirection(false);
		try {
			if(StringUtils.isNotBlank(nickname)){
				User user = usersService.findUserByNickName(nickname);
				if(user != null){
					page.setUserId(user.getDmId());
				}
			}else{
				page.setDmId(orderId);
			}
			Pager<Order, Long> query = query(page, null);
			List<Order> list  = query.getList();
			return getOrderAndOrderGoods(list,query);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Pager<Order, Long> getOrderAndOrderGoods(List<Order> list,Pager<Order, Long> query) throws ServiceException{
		JSONArray orderaArray = new JSONArray();
		if (list == null || list.isEmpty()) {
			query.setList2(orderaArray);
			return query;
		}
		for (Order order : list) {
			if("CONSUME_SUBSIDY".equals(order.getTags())){
				order.setConstantName("可消费补贴");
			}else if("HT_SUBSIDY".equals(order.getTags())){
				order.setConstantName("海淘补贴");
			}
			OrderGoods where = new OrderGoods();
			where.setOrderId(order.getDmId());
			List<OrderGoods> findList = orderGoodsService.findList(where);
			for(OrderGoods og:findList){
				StoreGoods sg = storeGoodsService.getGoods(og.getGoodsId(), null);
				og.setImgs(sg.getImgs());
			}
			JSONObject orderJSON = JSONObject.parseObject(JSON.toJSONString(order));
			orderJSON.remove("userId");
			orderJSON.remove("sellerId");
			orderJSON.put(RespKey.orderGoods, findList);
			DBObject fileds = UserFiledsUtils.getBasicUser();
			User order_user = usersService.getUser(order.getUserId(),fileds);
			fileds = new BasicDBObject();
			fileds.put(UserKey.id, 1);
			fileds.put(UserKey.headimg, 1);
			fileds.put(UserKey.address, 1);
			fileds.put(UserKey.nickname, 1);
			fileds.put(UserKey.userType, 1);
			fileds.put(UserKey.isDirectCamp, 1);
			UserShop seller = usersShopService.getUser(order.getSellerId(),fileds);
			// 后追加字段，此字段值为null，默认加一个值 0非直营 1直营
			if (null != seller && null == seller.getIsDirectCamp()) {
				seller.setIsDirectCamp(0);
			}
			orderJSON.put(RespKey.sell, seller);
			orderJSON.put(RespKey.user, order_user);
			if (order.getOrderStatus().intValue() == OrderStatus.success
					.getId()) {
				OrderLog orderLogwhere = new OrderLog();
				orderLogwhere.setOrderId(order.getDmId());
				orderLogwhere.setStatus(order.getOrderStatus());
				List<OrderLog> orderLogs = orderLogService
						.findList(orderLogwhere);
				if (orderLogs == null || orderLogs.isEmpty()) {
					log.error("错误的订单日志");
					break;
				}
				orderJSON.put(RespKey.successTime, orderLogs.get(0)
						.getTime());
			}
			orderaArray.add(orderJSON);
		}
		query.setList2(orderaArray);
		return query;
	}
	
	@Override
	public int findOrderCount(Integer orderType, Integer orderStatus,
			Long userId) throws ServiceException {
		  Order where = new Order();
		  if(null != orderType){
			  where.setOrderType(orderType);
		  }
		  if(null != orderStatus){
			  where.setOrderStatus(orderStatus);
		  }
	      where.setSellerId(userId);
	      try {
            Integer count = dao.findOrderCount(where);

            return Integer.parseInt((count == null ? 0 : count) + "");
	      } catch (DaoException e) {
	        throw new ServiceException(e);
	      }
	}
}