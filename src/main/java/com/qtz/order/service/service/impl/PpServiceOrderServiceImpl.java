package com.qtz.order.service.service.impl;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qtz.base.common.OrderPrefix;
import com.qtz.base.common.log.LogTool;
import com.qtz.base.dao.BizDao;
import com.qtz.base.dto.order.PayOrderModel;
import com.qtz.base.dto.order.PayOrderTypeEnum;
import com.qtz.base.dto.order.PayStatusEnum;
import com.qtz.base.dto.order.PaymentMethodEnum;
import com.qtz.base.dto.order.PpServiceGoods;
import com.qtz.base.dto.order.PpServiceOrder;
import com.qtz.base.dto.user.User;
import com.qtz.base.dto.user.UserType;
import com.qtz.base.exception.BaseDaoException;
import com.qtz.base.exception.BaseServiceException;
import com.qtz.base.exception.ExceptionCode;
import com.qtz.base.service.FifteenLongId;
import com.qtz.base.service.impl.BaseServiceImpl;
import com.qtz.base.util.Alipay;
import com.qtz.base.util.Arith;
import com.qtz.base.util.XmlUtil;
import com.qtz.goods.spi.service.PpServiceGoodsService;
import com.qtz.member.spi.store.service.SellerStoreService;
import com.qtz.member.spi.user.service.UsersShopService;
import com.qtz.order.service.dao.PpServiceOrderDao;
import com.qtz.order.spi.service.PpServiceOrderService;
import com.qtz.payment.spi.service.AlipayService;
import com.qtz.payment.spi.service.CnPayService;
import com.qtz.payment.spi.service.JdPayService;
import com.qtz.payment.spi.service.LakalaPayService;
import com.qtz.payment.spi.service.MbPayService;
import com.qtz.payment.spi.service.MsPayService;
import com.qtz.payment.spi.service.PAPayService;
import com.qtz.payment.spi.service.PaymentService;
import com.qtz.payment.spi.service.YeePayService;
import com.qtz.payment.spi.service.YsPayService;
import com.qtz.payment.spi.service.ZfPayService;
import com.qtz.payment.spi.service.ZxPayService;


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
        BaseServiceImpl<PpServiceOrder,Long> implements
        PpServiceOrderService {
    /**
     * 初始化日志对象
     */
    private static LogTool log = LogTool.getInstance(PpServiceOrderServiceImpl.class);
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
    private PaymentService paymentService;
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private JdPayService jdPayService;
    @Autowired
    private YsPayService ysPayService;
    @Autowired
    private ZfPayService zfPayService;
    @Autowired
    private PAPayService paPayService;
    @Autowired
    private YeePayService yeePayService;
    @Autowired
    private LakalaPayService lakalaPayService;
    @Autowired
    private SellerStoreService sellerStoreService;
    @Autowired
    private CnPayService cnPayService;
    @Autowired
    private MbPayService mbPayService;
    @Autowired
	private FifteenLongId fifteenLongIdImpl;
    @Autowired
    private MsPayService msPayService;
    @Autowired
    private ZxPayService zxPayService;
    

    /**
     * 【取得】业务DAO对象
     *
     * @return 业务DAO对象
     */
    @Override
    protected BizDao<PpServiceOrder, Long> getDao() {
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

    @Override
    public Long saveSubSellerStoreOrder(String pgId, Long reqUserId, Integer payType) throws BaseServiceException {
        log.info("正在提交订单..");
        if (pgId == null || reqUserId == null) {
            throw new BaseServiceException(ExceptionCode.NULL_EXCEPTION, "null exception");
        }
        PpServiceGoods ppServiceGoods = serviceGoodsService.findVo(Long.valueOf(pgId), null);
        if (ppServiceGoods == null) {
            throw new BaseServiceException(ExceptionCode.NULL_EXCEPTION, " 不存在的服务");
        }
        if (ppServiceGoods.getCode().contains(PpServiceGoods.store_prefix + ".sy")) {
            if (this.queryIsTrialPpStore(reqUserId.longValue())) {
                throw new BaseServiceException(ExceptionCode.HAVE_TO_TRY_PP_STORE, "已经试用过店铺");
            }
        }
        User reqUser = usersShopService.getUser(reqUserId);
        if (reqUser == null) {
            throw new BaseServiceException(ExceptionCode.ORDER_SUB_SELLER_ERROR, "订单提交有误  商家不存在");
//    	reqUser = usersShopService.getUser(reqUserId);
        }
        if (reqUser.getUserType().intValue() == UserType.PERSON.value()) {
            throw new BaseServiceException(ExceptionCode.ORDER_SUB_SELLER_ERROR, "订单提交有误  个人用户不能够下单");
        }
        try {
            if (sellerStoreService.getSellerStore(reqUserId) != null) {
                throw new BaseServiceException(ExceptionCode.SELLER_STORE_EXIST_VALID, "店铺存在并且有效不能付款.");
            }
        } catch (BaseServiceException e) {
            log.error(e);
        }

        Long orderId = Long.parseLong(OrderPrefix.PP_SHOP_ORDER + fifteenLongIdImpl.nextId());
        
        PpServiceOrder ppServiceOrder =
                new PpServiceOrder(Long.valueOf(pgId), reqUserId, ppServiceGoods.getOriginalPrice(), ppServiceGoods.toString(), payType.intValue(), ppServiceGoods.getPresentPrice());
       
        ppServiceOrder.setDmId(orderId);
        
		try {
			
			 getDao().addVo(ppServiceOrder);
			 
		} catch (BaseDaoException e) {
			throw new BaseServiceException(e);
		}
        
        return orderId;
    }

    @Override
    public boolean queryOrderPaymentStatus(Long orderId, Integer paymentType) throws BaseServiceException {
        boolean isPay = false;
        PpServiceOrder order = findVo(orderId, null);
        if (order == null) {
            log.debug("订单不存在  orderId = " + orderId);
            throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        try {
            // 如果是未支付则去查询
            if (null != order.getPayType() && (-1 == paymentType || 13 == paymentType)) {
                paymentType = order.getPayType();
            }
            log.debug("订单支付状态" + order.getPayStatus());
            if (order.getPayStatus() == PayStatusEnum.PAY_FAILURE.getId()) {
                if (paymentType == PaymentMethodEnum.ALIPAY.getValue()) {
                    log.debug("主动查询支付宝支付结果");
                    try {
                        log.debug("主动查询支付宝支付结果");
                        String respXML = alipayService.query(orderId);
                        Alipay alipayQueryXml = XmlUtil.XMLBean(respXML,Alipay.class);
                        log.debug("查询支付宝支付数据" + alipayQueryXml.toString());
                        if (alipayQueryXml.getIsSuccess().equals("T")) {
                            String trade_status = alipayQueryXml.getResponse()
                                    .getRrade().getTrade_status();// 交易状态
                            if (trade_status.equals("TRADE_SUCCESS")) {
                                log.debug("查询支付宝交易状态成功支付");
                                isPay = true;
                                paymentService.updateDealPayResultByPpService(Long.valueOf(alipayQueryXml
                                        .getResponse().getRrade()
                                        .getOut_trade_no()), alipayQueryXml.getResponse().getRrade()
                                        .getTrade_no(), Double.parseDouble(alipayQueryXml
                                        .getResponse().getRrade()
                                        .getPrice()), PaymentMethodEnum.ALIPAY.getValue());
                            }

                        }
                    } catch (Exception e) {
                        throw new BaseServiceException(e);
                    }
                } else if (paymentType == PaymentMethodEnum.WEIXIN.getValue()) {
                    log.debug("主动查询微信支付结果.");
                } else if (paymentType == PaymentMethodEnum.JDPAY.getValue()) {
                    String status = jdPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("2".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.YSPAY.getValue()) {
                    String status = ysPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("0".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.PAPAY.getValue()) {
                    String status = paPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("01".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.YEEPAY.getValue()) {
                    String status = yeePayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.ZFPAY.getValue()) {
                    String status = zfPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("00".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.LKLPAY.getValue()) {
                    String status = lakalaPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.CNPAY.getValue()) {
                    String status = cnPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单处理中,请稍候");
                    }
                } else if (paymentType == PaymentMethodEnum.MBPAY.getValue()) {
                    String status = mbPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER, paymentType);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                } else if (paymentType == PaymentMethodEnum.MBPAY2.getValue()) {
                    String status = mbPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER, paymentType);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                }else if(paymentType == PaymentMethodEnum.MSPAY.getValue()){
                	String status = msPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                }else if(paymentType == PaymentMethodEnum.ZXPAY.getValue()){
                	String status = zxPayService.queryOrder(orderId, PayOrderTypeEnum.PPORDER);
                    if ("1".equals(status)) {
                        isPay = true;
                    } else {
                        throw new BaseServiceException(-100137, "订单未支付");
                    }
                }
            }
            return isPay;
        } catch (Exception e) {
            if (e instanceof BaseServiceException) {
                throw new BaseServiceException(((BaseServiceException) e).getErrorType(), ((BaseServiceException) e).getErrorTitle());
            } else {
                throw new BaseServiceException(e);
            }
        }
    }

    @Override
    public boolean queryIsTrialPpStore(Long userId) throws BaseServiceException {
        try {
            int result = dao.queryIsTrialPpStore(userId);
            if (result > 0) {
                return true;
            }
        } catch (BaseDaoException e) {
            throw new BaseServiceException(e);
        }
        return false;
    }

    @Override
    public PpServiceOrder getLockOrder(Long orderId) throws BaseServiceException {
        try {
            return dao.getLockOrder(orderId);
        } catch (BaseDaoException e) {
            throw new BaseServiceException(e);
        }
    }

    @Override
    public PayOrderModel findAndCheckOrder(Long orderId) throws BaseServiceException {

        PpServiceOrder order = this.findVo(orderId, null);
        if (order == null) {
            throw new BaseServiceException(ExceptionCode.ORDER_INEXISTENCE, "订单不存在");
        }
        if (1 != order.getPayStatus()) {
            throw new BaseServiceException(ExceptionCode.ORDER_STATUS_ERROR, "订单已支付或关闭");
        }
        PayOrderModel model = new PayOrderModel();
        model.setOrderId(String.valueOf(orderId));
        model.setTime(order.getCrtime());
        model.setOrderName("胖胖生活年费");
//        model.setOrderType(PayOrderTypeEnum.PPORDER);
        Double price = order.getToPayAmount();
        model.setYuanPrice(Arith.moneyFormat(price));
        model.setFenPrice(String.valueOf(Arith.yuanToFen(price)));
        model.setPayType(order.getPayType());
        model.setThirdSn(order.getThreeSerialNumber());
        return model;
    }
}
