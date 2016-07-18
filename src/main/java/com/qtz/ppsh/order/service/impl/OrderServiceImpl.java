package com.qtz.ppsh.order.service.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.qtz.base.common.OrderPrefix;
import com.qtz.base.common.Pager;
import com.qtz.base.common.factory.OrderIdFactory;
import com.qtz.base.common.log.LogTool;
import com.qtz.base.common.response.RespCode;
import com.qtz.base.dao.BizDao;
import com.qtz.base.dto.order.PayOrderModel;
import com.qtz.base.dto.order.PayOrderTypeEnum;
import com.qtz.base.dto.order.PayStatusEnum;
import com.qtz.base.dto.order.PaymentMethodEnum;
import com.qtz.base.dto.order.StoreGoods;
import com.qtz.base.dto.order.StoreGoods.GoodsStatus;
import com.qtz.base.dto.order.StoreGoods.IsCoupon;
import com.qtz.base.dto.user.Coupon;
import com.qtz.base.dto.user.Coupon.CouponType;
import com.qtz.base.dto.user.CouponRules;
import com.qtz.base.dto.user.User;
import com.qtz.base.dto.user.UserType;
import com.qtz.base.enums.Status;
import com.qtz.base.exception.BaseDaoException;
import com.qtz.base.exception.BaseServiceException;
import com.qtz.base.exception.ExceptionCode;
import com.qtz.base.service.impl.BaseServiceImpl;
import com.qtz.base.util.Alipay;
import com.qtz.base.util.Arith;
import com.qtz.base.util.RespKey;
import com.qtz.base.util.XmlUtil;
import com.qtz.goods.spi.service.StoreGoodsService;
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
import com.qtz.member.spi.user.utils.UserFiledsUtils;
import com.qtz.member.spi.userwallet.dto.ReconciliationRecord;
import com.qtz.member.spi.userwallet.enums.YesOrNoEnum;
import com.qtz.member.spi.userwallet.enums.authenStatus;
import com.qtz.member.spi.userwallet.service.ReconciliationRecordService;
import com.qtz.member.spi.userwallet.service.UserWalletService;
import com.qtz.payment.spi.service.AlipayService;
import com.qtz.payment.spi.service.CnPayService;
import com.qtz.payment.spi.service.GhtPayService;
import com.qtz.payment.spi.service.JdPayService;
import com.qtz.payment.spi.service.LakalaPayService;
import com.qtz.payment.spi.service.LlPayService;
import com.qtz.payment.spi.service.MbPayService;
import com.qtz.payment.spi.service.MsPayService;
import com.qtz.payment.spi.service.PAPayService;
import com.qtz.payment.spi.service.PaymentService;
import com.qtz.payment.spi.service.YeePayService;
import com.qtz.payment.spi.service.YsPayService;
import com.qtz.payment.spi.service.ZfPayService;
import com.qtz.payment.spi.service.ZxPayService;
import com.qtz.ppsh.order.dao.OrderDao;
import com.qtz.ppsh.order.spi.dto.Order;
import com.qtz.ppsh.order.spi.dto.OrderGoods;
import com.qtz.ppsh.order.spi.dto.OrderLog;
import com.qtz.ppsh.order.spi.dto.Order.OrderStatus;
import com.qtz.ppsh.order.spi.dto.Order.OrderTypeEnum;
import com.qtz.ppsh.order.spi.dto.Order.ReceivingStatus;
import com.qtz.ppsh.order.spi.dto.Order.RefundStatus;
import com.qtz.ppsh.order.spi.dto.Order.SellerOrderStatus;
import com.qtz.ppsh.order.spi.dto.Order.TransactionStatus;
import com.qtz.ppsh.order.spi.page.OrderPage;
import com.qtz.ppsh.order.spi.service.OrderGoodsService;
import com.qtz.ppsh.order.spi.service.OrderLogService;
import com.qtz.ppsh.order.spi.service.OrderService;
import com.qtz.ppsh.order.vo.OrderKey;
import com.qtz.system.spi.jpush.model.MsgOutput;
import com.qtz.system.spi.jpush.service.JPushMessageService;




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
    private static LogTool log = LogTool.getInstance(OrderServiceImpl.class);

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
    private PaymentService paymentService;
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private JdPayService jdPayService;
    @Autowired
    private YsPayService ysPayService;
    @Autowired
    private PAPayService paPayService;
    @Autowired
    private YeePayService yeePayService;
    @Autowired
    private ZfPayService zfPayService;
    @Autowired
    private LakalaPayService lakalaPayService;
    @Autowired
    private CnPayService cnPayService;
    @Autowired
    private JPushMessageService jPushMessageService;
    @Autowired
    private ReconciliationRecordService reconciliationRecordService;
    @Autowired
    private MbPayService mbPayService;
    @Autowired
    private GhtPayService ghtPayService;
    @Autowired
    private MsPayService msPayService;
    @Autowired
    private ZxPayService zxPayService;
    @Autowired
    private LlPayService llPayService;
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
     * 【取得】日志对象
     *
     * @return 日志对象
     */
    @Override
    protected LogTool getLog() {
        return log;
    }

    /**
     * 【互斥锁】
     *
     * @param orderId
     * @return
     * @throws BaseServiceException
     * @time:2016年1月14日 下午6:04:49
     * @author 涂鑫
     * @version
     */
    private Order lockOrder(Long orderId) throws BaseServiceException {
        lock.lock();//得到锁
        synchronized (orderId) {
            try {
                //	synchronized (lock) {
                return dao.getLockOrder(orderId);
                //	}
            } catch (BaseDaoException e) {
                log.error(e);
                throw new BaseServiceException(e);
            }
        }
    }

    /**
     * 提交订单
     */
    public Long saveSubOrder(Map<Long, Integer> goodsMaps, Long receivingId,
                             Long couponId, User user, Integer orderType, Long makeTime,
                             String note) throws BaseServiceException {
        log.info("正在提交订单....");
        // if(makeEndTime==null && makeStartTime!=null || makeStartTime==null &&
        // makeEndTime!=null){
        // throw new BaseServiceException("错误预约时间...");
        // }
        if (makeTime == null) {
            makeTime = System.currentTimeMillis() + (30 * 60 * 1000);
            // throw new BaseServiceException("错误预约时间");
        }
        if ((makeTime.longValue() + 5 * 60 * 1000) < System.currentTimeMillis()) {
            throw new BaseServiceException(ExceptionCode.ERROR_MAKETIME, "错误的预约时间");
        }
        if (user.getUserType().intValue() == UserType.BUSINESS.value()) {
            throw new BaseServiceException(ExceptionCode.ORDER_SUB_SELLER_ERROR,
                    "订单提交有误  商家不允许买");
        }
        if (goodsMaps == null || goodsMaps.isEmpty() || orderType == null) {
            throw new BaseServiceException(ExceptionCode.NULL_EXCEPTION,
                    "null exception");
        }
        if (orderType.intValue() != OrderTypeEnum.eatIn.value()
                && orderType.intValue() != OrderTypeEnum.takeOut.value()) {
            throw new BaseServiceException(ExceptionCode.ORDER_SUB_ERROR,
                    "订单提交有误  类型不正确");
        }
        if (orderType.intValue() == OrderTypeEnum.takeOut.value()) {
            if (receivingId == null) {
                throw new BaseServiceException(ExceptionCode.NULL_EXCEPTION,
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
                throw new BaseServiceException(ExceptionCode.GOODS_DOWN, "商品下架");
            }
            orderGoods.setGoodsName(storeGoods.getGoodsName());// 商品名字
            Integer goodsNum = next.getValue();
            orderGoods.setGoodsNum(goodsNum.intValue());// 商品单个数量
            goodsCount = goodsCount + goodsNum.intValue();
            orderGoods.setGoodsPrice(storeGoods.getPrice());// 商品单价
            double totalPrice = Arith.mul(orderGoods.getGoodsNum(), orderGoods
                    .getGoodsPrice().doubleValue());
            totalPrice = Arith.round(totalPrice, 2, BigDecimal.ROUND_HALF_UP);
            orderGoods.setGoodsTotalPrice(totalPrice);// 单个商品总价
            orderGoods.setGoodsId(storeGoods.getDmId());
            orderGoodsService.save(orderGoods);

            orderPrice = Arith.add(orderPrice, totalPrice);
            if (sellerId == 0L) {
                sellerId = storeGoods.getUserId().longValue();
            } else {
                if (sellerId != storeGoods.getUserId().longValue()) {
                    // 提交的订单存在不同商家
                    throw new BaseServiceException(ExceptionCode.ORDER_SUB_ERROR,
                            "错误提交订单  商品商家不一致");
                }
            }
        }
        SellerStore sellerStore = sellerStoreService.getSellerStore(sellerId);
        
        if (orderType.intValue() == OrderTypeEnum.eatIn.value()) {
            // 如果是堂食订单
            if (sellerStore.getIsStop().intValue() == IsStop.NO.value()) {

                // 该订单不支持到店
                throw new BaseServiceException(ExceptionCode.STORE_ISSTOP_1,
                        "不支持到店");

            }
        } else if (orderType.intValue() == OrderTypeEnum.takeOut.value()) {
            if (sellerStore.getIsSend().intValue() == IsSend.NO.value()) {
                // 该商铺不支持派送
                throw new BaseServiceException(ExceptionCode.STORE_ISSEND_1,
                        "不支持派送");

            }
        }

        Long nowTime = System.currentTimeMillis();
        if (nowTime.longValue() <= sellerStore.getcBEndTime().longValue()
                && nowTime.longValue() >= sellerStore.getcBStartTime()
                .longValue()) {
            throw new BaseServiceException(ExceptionCode.STORE_CLOSE_BUSINESS,
                    "店铺非营业中无法下单");
        }
        Order order = null;
        if (orderType.intValue() == OrderTypeEnum.takeOut.value()) {
            UserReceivingInfo userReceivingInfo = new UserReceivingInfo();
            userReceivingInfo = receivingInfoService.findVo(receivingId,
                    userReceivingInfo);
            if (userReceivingInfo == null) {
                throw new BaseServiceException(ExceptionCode.NULL_EXCEPTION,
                        "收货地址null");
            }
            if (userReceivingInfo.getStatus() != Status.OK.value()) {
                throw new BaseServiceException(
                        ExceptionCode.RECEIVINGINFO_STATUS_ERROR,
                        "收货地址status错误");
            }
            if (userReceivingInfo.getUserId().longValue() != user.getDmId()
                    .longValue()) {
                throw new BaseServiceException(ExceptionCode.ORDER_SUB_ERROR,
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
                throw new BaseServiceException(ExceptionCode.ORDER_LT_SENDFEE,
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
            orderPrice = Arith.add(Arith.add(orderPrice, tempMealFee),
                    tempSendFee);
            order.setOrderPrice(orderPrice);
        }
        if (orderPrice >= 5000d) {
            throw new BaseServiceException(ExceptionCode.PRICE_ERROR, "单笔订单总金额需小于5000元");
        }
        if (orderPrice >= 1000d && user.getAuthen() == authenStatus.NOAuth.value()) {
            throw new BaseServiceException(ExceptionCode.PRICE_ERROR, "1000元以上。请实名认证后再消费");
        }
        try {
            getDao().addVo(order);
        } catch (BaseDaoException e) {
            throw new BaseServiceException("添加错误", e);
        }
        log.info("正在记录订单日志...");
        OrderLog save = new OrderLog();
        save.setOrderId(orderId);
        save.setStatus(PayStatusEnum.PAY_FAILURE.getId());// 初始订单日志未支付
        save.setTime(order.getCrtime());
        // 记录订单日志
        orderLogService.save(save);
        // 计算价格
        saveCalculatePaymentPrice(orderId, couponId, order.getUserId());
        return orderId;
    }

    private Long getOrderId(Long orderId) throws BaseServiceException {
        if (findVo(orderId, null) != null) {
        	 getOrderId(Long.parseLong(OrderPrefix.PP_USER_ORDER + String.valueOf(OrderIdFactory.getOrderId().longValue())));
        }
        return orderId;
    }

    @Override
    public Double saveCalculatePaymentPrice(Long orderId, Long couponId,
                                            Long userId) throws BaseServiceException {
        // TODO 此处计算支付金额时，有保存到数据库，后面优化代码，应该只单纯计算优惠后金额，在订单支付时，计算一次再最终保存至数据库
        if (orderId == null || userId == null) {
            throw new BaseServiceException(ExceptionCode.NULL_EXCEPTION,
                    "null exception");
        }
        Order order = findVo(orderId, null);
        if (order.getOrderStatus() != OrderStatus.unpay.getId()) {
            throw new BaseServiceException(ExceptionCode.ORDER_PAY_ERROR,
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
                throw new BaseServiceException(ExceptionCode.USER_NO_SUCH_COUPON,
                        "用户无此优惠卷");
            }
            Coupon coupon = new Coupon();
            coupon = couponService.findVo(couponId, coupon);

            if (coupon.getCouponRules().getMonetary() != null
                    && coupon.getCouponRules().getMonetary().doubleValue() > order
                    .getOrderPrice().doubleValue()) {
                throw new BaseServiceException(
                        ExceptionCode.COUPON_NOT_TO_USE_RULES, "优惠卷未达到使用规则");
            }
            boolean flag = false;
            if (coupon.getType().intValue() == CouponType.archLord.value()) {
                // 霸王卷
                price = Arith.sub(order.getOrderPrice().doubleValue(), coupon
                        .getCouponRules().getFavorableMoney().doubleValue());
                price = price <= 0 ? 0 : price;
                flag = true;

            } else if (coupon.getType().intValue() == CouponType.discount
                    .value()) {
                // 折扣劵
                double dis = Arith.div(coupon.getCouponRules()
                        .getFavorableMoney().doubleValue(), 10.0);
                price = Arith.mul(order.getOrderPrice().doubleValue(), dis);
                flag = true;
            } else if (coupon.getType().intValue() == CouponType.favorable
                    .value()) {
                // 优惠卷
                price = Arith.sub(order.getOrderPrice().doubleValue(), coupon
                        .getCouponRules().getFavorableMoney().doubleValue());
                flag = true;
            }
            if (flag) {
                // 有优惠卷计算 需要计算优惠金额
                price = Arith.round(price, 2, BigDecimal.ROUND_HALF_UP);
                Order update = new Order();
                update.setDmId(orderId);
                update.setCouponId(couponId);
                double couponPrice = Arith.sub(order.getOrderPrice(), price);
                update.setCouponPrice(couponPrice <= 0 ? 0 : couponPrice);
                modVoNotNull(update);
            }
        }
        Order update = new Order();
        update.setDmId(orderId);
        update.setPaymentPrice(price);
        modVoNotNull(update);
        return price;
    }

    @Override
    public void updateCancelCouponPay(Long orderId, Long couponId, Long userId)
            throws BaseServiceException {
        if (orderId == null || userId == null) {
            throw new BaseServiceException(ExceptionCode.NULL_EXCEPTION,
                    "null exception");
        }
        Order order = findVo(orderId, null);
        if (order == null) {
            throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.unpay.getId()) {
            throw new BaseServiceException(ExceptionCode.ORDER_PAY_ERROR,
                    "订单支付状态错误");
        }
        if (order.getUserId().longValue() != userId.longValue()) {
            throw new BaseServiceException(ExceptionCode.ORDER_USER_FALSENESS,
                    "下单用户不正确");
        }
        Double price = order.getOrderPrice();
        try {
            dao.updateCancelOrderCoupon(orderId, price);
        } catch (BaseDaoException e) {
            log.debug("取消失败.");
            throw new BaseServiceException(e);
        }
    }

    @Override
    public List<Map<Object, Object>> queryCountOrderMonth(Long userId, String month, int pageIndex, int pageSize) throws BaseServiceException {
        try {
            return dao.queryCountOrderMonth(userId, month, pageIndex, pageSize);
        } catch (BaseDaoException e) {
            throw new BaseServiceException(e);
        }
    }

    @Override
    public JSONArray getUsableCoupon(Long orderId, Long userId)
            throws BaseServiceException {
        if (orderId == null || userId == null) {
            throw new BaseServiceException(ExceptionCode.NULL_EXCEPTION,
                    "null exception");
        }
        Order order = findVo(orderId, null);
        if (order == null) {
            throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
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
                                           Integer pageIndex, Integer orderType) throws BaseServiceException {
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
            if(Order.OrderStatus.reorder.getId() == orderStatus.intValue()){
            	 page.setOrderType(orderType);
            }
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
        } catch (BaseServiceException e) {
            throw new BaseServiceException(e);
        } catch (BaseDaoException e) {
            throw new BaseServiceException(e);
        }
    }

    @Override
    public void updateReceivingOrder(Long orderId, Long sellerId) throws BaseServiceException {
        Order order = null;
        try {
            order = checkOrder(orderId, sellerId);
            if (order.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                throw new BaseServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态不正确");
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
            orderLogService.save(updateOrderLog);
            // 钱包划账
            if (System.currentTimeMillis() > order.getMakeTime().longValue()) {
                // 预约时间小于当前时间，则用当前时间 userWalletService
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), System.currentTimeMillis(), order.getPayType().intValue());
            } else {
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), order.getMakeTime(), order.getPayType().intValue());
            }
            //发送极光消息
            Map<String, String> extra = new HashMap<String, String>();
            extra.put("code", RespCode.order_receiving);
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("data", JSONObject.toJSONString(ex));
            this.jPushMessageService.sendMsg(RespCode.order_receiving, order.getSellerId(), order.getUserId(), extra);

        } finally {
            lock.unlock();
        }

    }

    private Order checkOrder(Long orderId, Long sellerId)
            throws BaseServiceException {
        Order order = lockOrder(orderId.longValue());
        if (order == null) {
            log.info("订单不存在.");
            throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        
        UserShop sellerUser = usersShopService.getUser(sellerId);
        
        if (sellerUser == null) {
            log.info("商家不存在.");
            throw new BaseServiceException(ExceptionCode.USER_NULL_EXCEPTION,
                    "用户不存在");
        }
        if (sellerUser.getUserType().intValue() == UserType.PERSON.value()) {
            log.info("账户类型不匹配,只有商家才能接单 拒单");
            throw new BaseServiceException(ExceptionCode.USERTYPE_DONT_MATCH,
                    "用户类型不匹配");
        }
        if (order.getSellerId().longValue() != sellerUser.getDmId().longValue()) {
            log.info("错误订单,该订单不是请求商家所属.[订单商家是" + order.getSellerId()
                    + ",实际接单商家是" + sellerId + "]");
            throw new BaseServiceException(ExceptionCode.ORDER_SELLER_ERROR,
                    "接单 拒单 有误");
        }
        if (order.getPayStatus().intValue() != PayStatusEnum.PAY_SUCCESS
                .getId()) {
            log.info("订单处于未支付状态或者处于退款状态不能接单.");
            throw new BaseServiceException(ExceptionCode.ORDER_PAY_ERROR_1, "订单未支付");
        }
        if (null != order.getSellerOrderStatus()
                && order.getSellerOrderStatus() != SellerOrderStatus.donHavaOrder
                .value()) {
            log.info("订单接单状态有误.");
            throw new BaseServiceException(
                    ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR,
                    "订单接单状态有误");
        }
        return order;
    }

    @Override
    public void updateRefusedOrder(Long orderId, Long sellerId)
            throws BaseServiceException {
        try {
            Order orders = checkOrder(orderId, sellerId);
            if (orders.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                throw new BaseServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态错误不能拒绝");
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
            orderLogService.save(updateOrderLog);
            // 商家拒绝订单钱包划账
            this.userWalletService.saveNoAccOrder(orderId + "", orders.getUserId());

            //发送极光消息
            Map<String, String> extra = new HashMap<String, String>();
            extra.put("code", RespCode.refuse_place_an_order);
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("data", JSONObject.toJSONString(ex));
            this.jPushMessageService.sendMsg(RespCode.refuse_place_an_order, orders.getSellerId(), orders.getUserId(), extra);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateCancelOrder(Long orderId, Long persionId)
            throws BaseServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order.getUserId().longValue() != persionId.longValue()) {
                throw new BaseServiceException(ExceptionCode.ORDER_USER_FALSENESS,
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
                orderLogService.save(orderLog);
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
                    orderLogService.save(saveOrderLog);
                    // 取消订单钱包操作
                    this.userWalletService.saveNoAccOrder(orderId + "", order.getUserId());
                    //发送极光消息
                    Map<String, String> extra = new HashMap<String, String>();
                    extra.put("code", RespCode.order_cancel);
                    MsgOutput ex = new MsgOutput();
                    ex.setId(orderId + "");
                    extra.put("data", JSONObject.toJSONString(ex));
                    this.jPushMessageService.sendMsg(RespCode.order_cancel, order.getUserId(), order.getSellerId(), extra);
                    return;
                } else {
                    throw new BaseServiceException(
                            ExceptionCode.ORDERS_STATUS_CANNOT_BE_CANCELLED,
                            "该订单状态无法取消");
                }

            }
        } finally {
            lock.unlock();
        }
        // if(order.getSellerOrderStatus()!=null){
        // throw new BaseServiceException("服务器异常.");
        // }

    }


    @Override
    public void updateConfirmOrder(Long orderId, Long persionId)
            throws BaseServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order == null) {
                log.debug("订单不存在.");
                throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
            }
            if (order.getOrderStatus().intValue() != OrderStatus.reorder.getId()) {
                log.debug("状态不正确");
                throw new BaseServiceException(ExceptionCode.ORDER_UNPAY, "订单未支付不能接单");
            }
            if (order.getUserId().longValue() != persionId.longValue()) {
                log.debug("订单买家不正确");
                throw new BaseServiceException(ExceptionCode.ORDER_USER_FALSENESS,
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
            orderLogService.save(saveOrderLog);
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
    public void updateApplyRefund(Long userId, Long orderId, String refundNote) throws BaseServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order == null) {
                log.debug("订单不存在");
                throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
            }
            if (order.getUserId().longValue() != userId.longValue()) {
                log.debug("下单用户不正确");
                throw new BaseServiceException(ExceptionCode.ORDER_USER_FALSENESS,
                        "下单用户不正确");
            }
            if (order.getOrderStatus().intValue() == OrderStatus.unpay.getId()
                    || order.getOrderStatus().intValue() == OrderStatus.failure
                    .getId()
                    || order.getOrderStatus().intValue() == OrderStatus.success
                    .getId()) {
                log.debug("不允许退款");
                throw new BaseServiceException(ExceptionCode.NO_REFUND, "不允许退款");
            }

            //检查订单是否还处理对账中，若存在可退款，若不存在不允许退款
            ReconciliationRecord record = new ReconciliationRecord();
            record.setSaleOrdersId(orderId.toString());
            List<ReconciliationRecord> records = reconciliationRecordService.findList(record);
            if (records == null || records.size() == 0) {
                log.debug("订单已完成，不能退款");
                throw new BaseServiceException(ExceptionCode.ORDERFINISHED_REJECTREIM, "订单已完成，不能退款");
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
            orderLogService.save(saveOrderLog);

            for (int i = 0; i < records.size(); i++) {
                ReconciliationRecord r = records.get(i);
                r.setOrderStatus(YesOrNoEnum.NO.getValue());//申请退款中
                reconciliationRecordService.modVoNotNull(r);
            }

            //发送极光消息
            Map<String, String> extra = new HashMap<String, String>();
            extra.put("code", RespCode.apply_for_refund);
            //extra.put("message", cm.getMessage());
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("data", JSONObject.toJSONString(ex));
            this.jPushMessageService.sendMsg(RespCode.apply_for_refund, order.getUserId(), order.getSellerId(), extra);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateAgreedRefund(Long sellerId, Long orderId, Integer stype,
                                   String message) throws BaseServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order == null) {
                log.debug("订单不存在");
                throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
            }
            if (order.getSellerId().longValue() != sellerId.longValue()) {
                log.debug("订单不属于商家");
                throw new BaseServiceException(ExceptionCode.ORDER_SELLER_ERROR,
                        "订单不属于商家");
            }
            if (order.getOrderStatus().intValue() != OrderStatus.applyRefund
                    .getId()) {
                log.debug("订单状态不是申请退款");
                throw new BaseServiceException(ExceptionCode.ORDER_STATUS_ERROR,
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
                orderLogService.save(saveOrderLog);

                for (int i = 0; i < records.size(); i++) {
                    ReconciliationRecord r = records.get(i);
                    r.setOrderStatus(YesOrNoEnum.YES.getValue());//未退款
                    reconciliationRecordService.modVoNotNull(r);
                }

                //自定义消息
                extra.put("code", RespCode.no_agree_to_refund);
                jPushMessageService.sendMsg(RespCode.no_agree_to_refund, order.getSellerId(), order.getUserId(), extra);
            } else {
                //检查订单是否还处理对账中，若存在可退款，若不存在不允许退款
                if (records == null || records.size() == 0) {
                    log.debug("订单已完成，不能退款");
                    throw new BaseServiceException(ExceptionCode.ORDERFINISHED_REJECTREIM, "订单已完成，不能退款");
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
                orderLogService.save(saveOrderLog);
                this.userWalletService.saveAccBackMoney(orderId + "", order.getUserId());

                //自定义消息
                extra.put("code", RespCode.agree_to_refund);
                jPushMessageService.sendMsg(RespCode.agree_to_refund, order.getSellerId(), order.getUserId(), extra);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Order> findOrderClose(Long ctime) throws BaseServiceException {

        try {
            return dao.findOrderClose(ctime);
        } catch (BaseDaoException e) {
            e.printStackTrace();
            throw new BaseServiceException(e);
        }
    }

    @Override
    public boolean queryOrderPaymentStatus(Long orderId, Integer paymentType,
                                           Integer resultDeal) throws BaseServiceException {
        boolean isPay = false;
        Order order = findVo(orderId, null);
        if (order == null) {
            throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        try {
            // 如果是未支付则去查询
            log.debug("订单支付状态" + order.getPayStatus());
            if (null != order.getPayType() && (-1 == paymentType || 13 == paymentType)) {
                paymentType = order.getPayType();
            }
            if(2 == paymentType){paymentType = 24;}
            if (order.getPayStatus() == PayStatusEnum.PAY_FAILURE.getId()) {
                if (paymentType == PaymentMethodEnum.ALIPAY.getValue()) {
                    log.debug("主动查询支付宝支付结果");
                    String respXML = alipayService.query(orderId);
                    Alipay alipayQueryXml = XmlUtil.XMLBean(respXML,Alipay.class);
                    log.debug("查询支付宝支付数据" + alipayQueryXml.toString());
                    if (alipayQueryXml.getIsSuccess().equals("T")) {
                        String trade_status = alipayQueryXml.getResponse().getRrade().getTrade_status();// 交易状态
                        if (trade_status.equals("TRADE_SUCCESS")) {
                            log.debug("查询支付宝交易状态成功支付");
                            isPay = true;
                            switch (resultDeal) {
                                case 1:
                                    paymentService.updateDealPayResult(
                                            Long.valueOf(alipayQueryXml
                                                    .getResponse().getRrade()
                                                    .getOut_trade_no()),
                                            alipayQueryXml.getResponse().getRrade()
                                                    .getTrade_no(),
                                            Double.parseDouble(alipayQueryXml
                                                    .getResponse().getRrade()
                                                    .getPrice()),
                                            PaymentMethodEnum.ALIPAY.getValue());
                                    break;
                                default:
                                    break;
                            }

                        } else {
                            throw new BaseServiceException(-100137, "订单未支付");
                        }
                    }
                } else if (paymentType == PaymentMethodEnum.WEIXIN.getValue()) {
                    log.debug("主动查询微信支付结果.");
                } else if (paymentType == PaymentMethodEnum.JDPAY.getValue()) {
                    String status = jdPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("2".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.YSPAY.getValue()) {
                    String status = ysPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("0".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.PAPAY.getValue()) {
                    String status = paPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("01".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.YEEPAY.getValue()) {
                    String status = yeePayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.ZFPAY.getValue()) {
                    String status = zfPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("00".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.LKLPAY.getValue()) {
                    String status = lakalaPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.CNPAY.getValue()) {
                    String status = cnPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单处理中,请稍候");
                    }
                } else if (paymentType == PaymentMethodEnum.MBPAY.getValue()) {
                    String status = mbPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER, PaymentMethodEnum.MBPAY.getValue());
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.MBPAY2.getValue()) {
                    String status = mbPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER, PaymentMethodEnum.MBPAY2.getValue());
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.GHTPAY.getValue()) {
                    String status = ghtPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.FASTPAY.getValue()) {
                    isPay = false;
                    throw new BaseServiceException(-100137, "订单处理中,请稍候");
                }else if(paymentType == PaymentMethodEnum.MSPAY.getValue()){
                	String status = msPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                }else if(paymentType == PaymentMethodEnum.ZXPAY.getValue()){
                	String status = zxPayService.queryOrder(orderId, PayOrderTypeEnum.ORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "处理中");
                    }
                }else if(paymentType == PaymentMethodEnum.LLPAY.getValue()){
                    if (llPayService.queryOrder(orderId)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "处理中");
                    }
                }
            }
            return isPay;
        } catch (Exception e) {
            throw new BaseServiceException(e);
        }
    }

    @Override
    public Map<String, Object> querySumTurnover(Long time)
            throws BaseServiceException {

        return null;
    }

    @Override
    public int findNewOrderCoupon(Integer orderType, Long userId)
            throws BaseServiceException {
        Order where = new Order();
        where.setOrderType(orderType);
        where.setSellerId(userId);
        where.setOrderStatus(OrderStatus.pay_dont_answer_sheet.getId());
        try {
            Long count = getDao().findCount(where);

            return Integer.parseInt((count == null ? 0 : count) + "");
        } catch (BaseDaoException e) {
            throw new BaseServiceException(e);
        }
    }

    @Override
    public List<Order> queryUser(Long sellerid, int pageIndex)
            throws BaseServiceException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sellerId", sellerid);
        map.put("pageIndex", (pageIndex - 1) * 20);

        try {
            return dao.querySellerCummer(map);
        } catch (BaseDaoException e) {
            e.printStackTrace();
            throw new BaseServiceException(e);
        }
    }

    @Override
    public List<Order> queryTempOrder() throws BaseServiceException {
        try {
            return dao.queryTempOrder();
        } catch (BaseDaoException e) {
            throw new BaseServiceException(e);
        }

    }

    @Override
    public List<Map<String, Object>> getOrdersToExport(Map<String, Object> param)
            throws BaseServiceException {
        try {
            return dao.getOrdersToExport(param);
        } catch (BaseDaoException e) {
            throw new BaseServiceException(e);
        }
    }

    @Override
    public PayOrderModel findAndCheckOrder(Long orderId) throws BaseServiceException {
        Order order = this.findVo(orderId, null);
        if (order == null) {
            throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        if (order.getOrderStatus() != Order.OrderStatus.unpay.getId()) {
            throw new BaseServiceException(ExceptionCode.ORDER_STATUS_ERROR, "订单已支付或关闭");
        }
        PayOrderModel model = new PayOrderModel();
        model.setOrderId(String.valueOf(orderId));
        model.setTime(order.getCrtime());
        model.setOrderName("胖胖生活订单");
//        model.setOrderType(PayOrderTypeEnum.ORDER);
        Double price = order.getPaymentPrice();
        model.setYuanPrice(Arith.moneyFormat(price));
        model.setFenPrice(String.valueOf(Arith.yuanToFen(price)));
        model.setPayType(order.getPayType());
        model.setThirdSn(order.getThreeSerialNumber());
        model.setUserId(order.getUserId());
        model.setSellerId(order.getSellerId());
        return model;
    }

	@Override
	public Order queryUserOrder(Long orderId) throws BaseServiceException {
		Order order = findVo(orderId, null);
		return order;
	}

	@Override
    public void receviceOrder(Long orderId, Integer isAccept,Long sellerId)
            throws BaseServiceException {
        if(Order.IsAccept.yes.value() == isAccept){
            this.acceptReceiveOrder(orderId, sellerId);
        }
        else if(Order.IsAccept.no.value() == isAccept){
            this.refuseReceiveOrder(orderId, sellerId);
        }
        
    }
	
	@Override
    public void applyRefund(Long orderId,Integer isAccept,
            Long sellerId,String message) throws BaseServiceException {
        try {
            Order order = lockOrder(orderId.longValue());
            if (order == null) {
                log.debug("订单不存在");
                throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
            }
            if (order.getSellerId().longValue() != sellerId.longValue()) {
                log.debug("订单不属于商家");
                throw new BaseServiceException(ExceptionCode.ORDER_SELLER_ERROR,
                        "订单不属于商家");
            }
            if (order.getOrderStatus().intValue() != OrderStatus.applyRefund
                    .getId()) {
                log.debug("订单状态不是申请退款");
                throw new BaseServiceException(ExceptionCode.ORDER_STATUS_ERROR,
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
                orderStatus = OrderStatus.reorder.getId();          //配送中
//                }else if(Order.OrderTypeEnum.eatIn.value() == orderType){
//                  orderStatus = OrderStatus.reorder.getId();          //待消费
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
                orderLogService.save(saveOrderLog);

                for (int i = 0; i < records.size(); i++) {
                    ReconciliationRecord r = records.get(i);
                    r.setOrderStatus(YesOrNoEnum.YES.getValue());//未退款
                    reconciliationRecordService.modVoNotNull(r);
                }

                //自定义消息
                extra.put("code", RespCode.no_agree_to_refund);
                jPushMessageService.sendMsg(RespCode.no_agree_to_refund, order.getSellerId(), order.getUserId(), extra);
            } else {
                //检查订单是否还处理对账中，若存在可退款，若不存在不允许退款
                if (records == null || records.size() == 0) {
                    log.debug("订单已完成，不能退款");
                    throw new BaseServiceException(ExceptionCode.ORDERFINISHED_REJECTREIM, "订单已完成，不能退款");
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
                orderLogService.save(saveOrderLog);
                this.userWalletService.saveAccBackMoney(orderId + "", order.getUserId());

                //自定义消息
                extra.put("code", RespCode.agree_to_refund);
                jPushMessageService.sendMsg(RespCode.agree_to_refund, order.getSellerId(), order.getUserId(), extra);
            }
        } finally {
            lock.unlock();
        }
        
    }

	
	//同意接单
	private void acceptReceiveOrder(Long orderId,Long sellerId,Integer orderType) throws BaseServiceException{
		Order order = null;
		try {
            order = checkOrder(orderId, sellerId);
            if (order.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                throw new BaseServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态不正确");
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
            orderLogService.save(updateOrderLog);
            // 钱包划账
            if (System.currentTimeMillis() > order.getMakeTime().longValue()) {
                // 预约时间小于当前时间，则用当前时间 userWalletService
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), System.currentTimeMillis(), order.getPayType().intValue());
            } else {
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), order.getMakeTime(), order.getPayType().intValue());
            }
            //发送极光消息
            Map<String, String> extra = new HashMap<String, String>();
            extra.put("code", RespCode.order_receiving);
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("data", JSONObject.toJSONString(ex));
            this.jPushMessageService.sendMsg(RespCode.order_receiving, order.getSellerId(), order.getUserId(), extra);

        } finally {
            lock.unlock();
        }
	}
	
	//同意接单
    private void acceptReceiveOrder(Long orderId,Long sellerId) throws BaseServiceException{
        Order order = null;
        try {
            order = checkOrder(orderId, sellerId);
            if (order.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                throw new BaseServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态不正确");
            }
            int orderStatus = 0;
//            if(Order.OrderTypeEnum.takeOut.value() == orderType){ 
            orderStatus = OrderStatus.reorder.getId();          //配送中
//            }else if(Order.OrderTypeEnum.eatIn.value() == orderType){
//              orderStatus = OrderStatus.reorder.getId();          //待消费
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
            orderLogService.save(updateOrderLog);
            // 钱包划账
            if (System.currentTimeMillis() > order.getMakeTime().longValue()) {
                // 预约时间小于当前时间，则用当前时间 userWalletService
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), System.currentTimeMillis(), order.getPayType().intValue());
            } else {
                this.userWalletService.saveAccectOrder(orderId + "", order.getPaymentPrice(), order.getSellerId(),
                        order.getUserId(), order.getMakeTime(), order.getPayType().intValue());
            }
            //发送极光消息
            Map<String, String> extra = new HashMap<String, String>();
            extra.put("code", RespCode.order_receiving);
            MsgOutput ex = new MsgOutput();
            ex.setId(orderId + "");
            extra.put("data", JSONObject.toJSONString(ex));
            this.jPushMessageService.sendMsg(RespCode.order_receiving, order.getSellerId(), order.getUserId(), extra);

        } finally {
            lock.unlock();
        }
    }
    
    //拒绝接单
    private void refuseReceiveOrder(Long orderId,Long sellerId) throws BaseServiceException{
         try {
                Order orders = checkOrder(orderId, sellerId);
                if (orders.getOrderStatus().intValue() != Order.OrderStatus.pay_dont_answer_sheet.getId()) {
                    throw new BaseServiceException(ExceptionCode.SELLER_RECEIVING_ORDER_STATUS_ERROR, "订单状态错误不能拒绝");
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
                orderLogService.save(updateOrderLog);
                // 商家拒绝订单钱包划账
                this.userWalletService.saveNoAccOrder(orderId + "", orders.getUserId());

                //发送极光消息
                Map<String, String> extra = new HashMap<String, String>();
                extra.put("code", RespCode.refuse_place_an_order);
                MsgOutput ex = new MsgOutput();
                ex.setId(orderId + "");
                extra.put("data", JSONObject.toJSONString(ex));
                this.jPushMessageService.sendMsg(RespCode.refuse_place_an_order, orders.getSellerId(), orders.getUserId(), extra);
            } finally {
                lock.unlock();
            }
    }
	@Override
	public Pager<Order, Long> getOrderListByIdOrNickname(Long sellerId,String nickname, Long orderId, Integer pageIndex) throws BaseServiceException {
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
		} catch (BaseServiceException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Pager<Order, Long> getOrderAndOrderGoods(List<Order> list,Pager<Order, Long> query) throws BaseServiceException{
		JSONArray orderaArray = new JSONArray();
		if (list == null || list.isEmpty()) {
			query.setList2(orderaArray);
			return query;
		}
		for (Order order : list) {
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
	public int findOrderCount(Integer orderType, Integer orderStatus, Long userId) throws BaseServiceException {
		return 0;
	}
	
}
