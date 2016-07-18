package com.qtz.ppsh.order.vo;
import java.io.Serializable;
import com.qtz.base.dto.order.PayStatusEnum;
import com.qtz.base.dto.order.PaymentMethodEnum;
/**
 * <p>Title:Order</p>
 * <p>Description:订单VO类</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-09-07
 */
public class Order implements Serializable {
	
	/**类的版本号*/
	private static final long serialVersionUID = 1308498390534144L;
    private Long dmId;
	/** 商家id */
	private Long sellerId;
	/** 使用优惠卷 没有 则为null */
	private Long couponId;
	/** 购买用户 */
	private Long userId;
	/** 创建时间 下单时间 */
	private Long crtime;
	/** 1 外卖订单 2 堂食订单 */
	private Integer orderType;
	/** 订单价格 */
	private Double orderPrice;
	/** 支付成功时间 */
	private Long chargeTime;
	/** 订单状态 */
	private Integer orderStatus;
	/** 第三方流水号 */
	private String threeSerialNumber;
	/** 优惠金额 */
	private Double couponPrice;
	/** 付款金额 */
	private Double paymentPrice;
	/** 收货手机号码 */
	private String receivingPhone;
	/** 收货名字 */
	private String receivingName;
	/** 收货地址 */
	private String receivingAddress;
	/**
	 * 收货门牌号
	 */
	private String houseNumber;
	/** 商品总数量 */
	private Integer goodsCount;
	/** 支付状态 0支付成功 1 未支付  3退款 */
	private Integer payStatus;
	/**
	 * 退款备注
	 */
	private String refundNote;
	/**
	 * 提交备注
	 */
	private String note;
	/**
	 * 预约时间
	 */
	private Long makeTime;
	
	/** 派送费 */
	private Double sendFee;
	/** 餐盒费 */
	private Double mealFee;
	/**
	 * 交易状态
	 */
	private Integer transactionStatus;
	
	/**
	 * 支付类型
	 */
	private Integer payType;
	//start luoshun 20160326
	/**
	 * 支付类型文本
	 */
	private String payTypeContent;
	/**点评状态,0:未评论,1:已评论**/
	private Integer reviews;
	//end luoshun 20160326
	public Order(){
		super();
	}
	

	public Order(Long dmId, Long sellerId, Long couponId, Long userId,
			Double orderPrice,Integer orderType,
			String receivingPhone, String receivingName,
			String receivingAddress, Integer goodsCount) {
		super();
		this.dmId = dmId;
		this.orderType=orderType;
		this.sellerId = sellerId;
		this.couponId = couponId;
		this.userId = userId;
		this.orderPrice = orderPrice;
		this.couponPrice = 0.0d;
		this.receivingPhone = receivingPhone;
		this.receivingName = receivingName;
		this.receivingAddress = receivingAddress;
		this.goodsCount = goodsCount;
		this.chargeTime=0L;
		this.crtime=System.currentTimeMillis();
		this.orderType=null;
		this.orderStatus=PayStatusEnum.PAY_FAILURE.getId();
		this.paymentPrice=0.0;
	}
	/**
	 * 预约时间	构造
	 * 构造函数
	 */
	public Order(Long dmId, Long sellerId, Long couponId, Long userId,
			Double orderPrice,Integer orderType,
			String receivingPhone, String receivingName,
			String receivingAddress, Integer goodsCount,Long makeTime,String houseNumber) {
		super();
		this.dmId = dmId;
		this.orderType=orderType;
		this.sellerId = sellerId;
		this.couponId = couponId;
		this.userId = userId;
		this.orderPrice = orderPrice;
		this.couponPrice = 0.0d;
		this.receivingPhone = receivingPhone;
		this.receivingName = receivingName;
		this.receivingAddress = receivingAddress;
		this.goodsCount = goodsCount;
		this.chargeTime=0L;
		this.crtime=System.currentTimeMillis();
		this.orderStatus=OrderStatus.unpay.id;
		this.paymentPrice=0.0;
		this.makeTime=makeTime;
		this.payStatus=PayStatusEnum.PAY_FAILURE.getId();
		this.houseNumber=houseNumber;
		this.sellerOrderStatus = SellerOrderStatus.donHavaOrder.value();
	}
	

	/**
	 * 
	 * <p>Title:OrderStatus</p>
	 * <p>Description:(订单状态)</p>
	 * <p>Copyright: Copyright (c) 2015</p>
	 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
	 * @author 涂鑫 - changbo.li
	 * @version v1.0 2015年9月7日
	 */
	public enum OrderStatus{
		/**
		 * 交易成功
		 */
		success(0,"成功 交易关闭"), 
		
		/**
		 * 未支付  刚下单 
		 */
		unpay(1,"下单 未支付"),
		/**
		 * 同意退款 	
		 */
		refund(2,"退款"), 
		/**
		 * 拒绝订单	
		 */
		refused(3,"拒绝接单"), 
		/**
		 * 接单	
		 */
		reorder(4,"已经支付"), 
		/**
		 * 支付 未接单
		 */
		pay_dont_answer_sheet(5,"支付后 "), 
		/**
		 * 交易关闭失败
		 */
		failure(6,"交易失败"), 
		/**
		 * 申请退款
		 */
		applyRefund(7,"申请退款"), 
		/**
		 * 不同意退款
		 */
		applyUnAgrenRefund(8,"不同意退款"); 
		
		private int  id;//id
		private String desc;//描述
		private OrderStatus(int id,String desc){
			this.id=id;
			this.desc=desc;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getDesc() {
			return desc;
		}
		public void setDesc(String desc) {
			this.desc = desc;
		}
	}

	/**
	 * 商家接单状态
	 */
	private Integer sellerOrderStatus;
	/**
	 * 退款状态
	 */
	private Integer refundStatus;
	/**
	 * 收货状态
	 */
	private Integer receivingStatus;
	/**
	 * 
	 * <p>Title:sellerOrderStatus</p>
	 * <p>Description:(商家接单情况)</p>
	 * <p>Copyright: Copyright (c) 2015</p>
	 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
	 * @author 涂鑫 - changbo.li
	 * @version v1.0 2015年9月16日
	 */
	public enum SellerOrderStatus{
		//已接单
		havaOrder(0),
		/**
		 * 未接单
		 */
		donHavaOrder(1),
		/**
		 * 已拒绝
		 */
		refusedOrder(2);
		private int value;
		private SellerOrderStatus(int value){
			this.value=value;
		}
		public int value(){
			return value;
		}
	}
	/**
	 * 
	 * <p>Title:RefundStatus</p>
	 * <p>Description:(退款状态)</p>
	 * <p>Copyright: Copyright (c) 2015</p>
	 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
	 * @author 涂鑫 - changbo.li
	 * @version v1.0 2015年9月16日
	 */
	public enum RefundStatus{
		/**
		 * 未申请退款
		 */
		dontApplyRefund(0),
		/**
		 * 申请退款
		 */
		applyRefund(1),
		/**
		 * 同意退款
		 */
		agreedRefund(2),
		/**
		 * 拒绝退款
		 */
		refusedRefund(3);
		private int value;
		private RefundStatus(int value){
			this.value=value;
		}
		public int value(){
			return value;
		}
	}
	
	/**
	 * 
	 * <p>Title:ReceivingStatus</p>
	 * <p>Description:(收货状态)</p>
	 * <p>Copyright: Copyright (c) 2015</p>
	 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
	 * @author 涂鑫 - changbo.li
	 * @version v1.0 2015年9月16日
	 */
	public enum ReceivingStatus{
		/**
		 * 0 未消费 	未收货
		 */
		notSpending(0),
		/**
		 * 已经消费	已收货
		 */
		hasSpending(1);
		private int value;
		private ReceivingStatus(int value){
			this.value=value;
		}
		public int value(){
			return value;
		}
	}
	/**
	 * 
	 * <p>Title:transactionStatus</p>
	 * <p>Description:(交易状态	这个交易状态属于用户与商家直接的交易)</p>
	 * <p>Copyright: Copyright (c) 2015</p>
	 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
	 * @author 涂鑫 - changbo.li
	 * @version v1.0 2015年9月16日
	 */
	public enum TransactionStatus{
		/**
		 * 成功
		 */
		success(0),
		/**
		 * 失败
		 */
		failure(1);
		private int value;
		private TransactionStatus(int value){
			this.value=value;
		}
		public int value(){
			return value;
		}
	}
	/**
	 * 
	 * <p>Title:OrderTypeEnum</p>
	 * <p>Description:(订单类型)</p>
	 * <p>Copyright: Copyright (c) 2015</p>
	 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
	 * @author 涂鑫 - changbo.li
	 * @version v1.0 2015年9月2日
	 */
	public enum OrderTypeEnum{
		/**
		 * 1 外卖订单(配送订单) 	2堂食订单 (到店订单)
		 * 
		 */
		takeOut(1),eatIn(2),;
		private int value;
		private OrderTypeEnum(int value){
			this.value=value;
		}
		public int value(){
			return this.value;
		}
	}
	
	/**
	 * 同意或拒接
	 * <p>Title:IsAccept</p>
	 * <p>Description:</p>
	 * <p>Copyright: Copyright (c) 2016</p>
	 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
	 * @author 张玉兵
	 * @version v1.0 2016年6月8日
	 */
	public enum IsAccept{
		yes(0),no(1);
		private int value;
		private IsAccept(int value){
			this.value=value;
		}
		public int value(){
			return this.value;
		}
	}

	/**
	 * 平安银行订单号
	 */
	private String paOrdersId;
	
	public String getPaOrdersId() {
		return paOrdersId;
	}

	public void setPaOrdersId(String paOrdersId) {
		this.paOrdersId = paOrdersId;
	}


	public Integer getTransactionStatus() {
		return transactionStatus;
	}


	public void setTransactionStatus(Integer transactionStatus) {
		this.transactionStatus = transactionStatus;
	}


	public Long getDmId() {
	    return this.dmId;
	}
	public void setDmId(Long dmId) {
	    this.dmId=dmId;
	}
	public Long getSellerId() {
	    return this.sellerId;
	}
	public void setSellerId(Long sellerId) {
	    this.sellerId=sellerId;
	}
	public Long getCouponId() {
	    return this.couponId;
	}
	public void setCouponId(Long couponId) {
	    this.couponId=couponId;
	}
	public Long getUserId() {
	    return this.userId;
	}
	public void setUserId(Long userId) {
	    this.userId=userId;
	}
	public Long getCrtime() {
	    return this.crtime;
	}
	public void setCrtime(Long crtime) {
	    this.crtime=crtime;
	}
	public Integer getOrderType() {
	    return this.orderType;
	}
	public void setOrderType(Integer orderType) {
	    this.orderType=orderType;
	}
	public Double getOrderPrice() {
	    return this.orderPrice;
	}
	public void setOrderPrice(Double orderPrice) {
	    this.orderPrice=orderPrice;
	}
	public Long getChargeTime() {
	    return this.chargeTime;
	}
	public void setChargeTime(Long chargeTime) {
	    this.chargeTime=chargeTime;
	}
	public Integer getOrderStatus() {
	    return this.orderStatus;
	}
	public void setOrderStatus(Integer orderStatus) {
	    this.orderStatus=orderStatus;
	}
	public String getThreeSerialNumber() {
	    return this.threeSerialNumber;
	}
	public void setThreeSerialNumber(String threeSerialNumber) {
	    this.threeSerialNumber=threeSerialNumber;
	}
	public Double getCouponPrice() {
	    return this.couponPrice;
	}
	public void setCouponPrice(Double couponPrice) {
	    this.couponPrice=couponPrice;
	}
	public Double getPaymentPrice() {
	    return this.paymentPrice;
	}
	public void setPaymentPrice(Double paymentPrice) {
	    this.paymentPrice=paymentPrice;
	}
	public String getReceivingPhone() {
	    return this.receivingPhone;
	}
	public void setReceivingPhone(String receivingPhone) {
	    this.receivingPhone=receivingPhone;
	}
	public String getReceivingName() {
	    return this.receivingName;
	}
	public void setReceivingName(String receivingName) {
	    this.receivingName=receivingName;
	}
	public String getReceivingAddress() {
	    return this.receivingAddress;
	}
	public void setReceivingAddress(String receivingAddress) {
	    this.receivingAddress=receivingAddress;
	}
	public Integer getGoodsCount() {
	    return this.goodsCount;
	}
	public void setGoodsCount(Integer goodsCount) {
	    this.goodsCount=goodsCount;
	}
	public Integer getPayStatus() {
	    return this.payStatus;
	}
	public void setPayStatus(Integer payStatus) {
	    this.payStatus=payStatus;
	}
	public String toString() {
	    return "[" + "dmId:" + getDmId() +"," + "sellerId:" + getSellerId() +"," + "couponId:" + getCouponId() +"," + "userId:" + getUserId() +"," + "crtime:" + getCrtime() +"," + "orderType:" + getOrderType() +"," + "orderPrice:" + getOrderPrice() +"," + "chargeTime:" + getChargeTime() +"," + "orderStatus:" + getOrderStatus() +"," + "threeSerialNumber:" + getThreeSerialNumber() +"," + "couponPrice:" + getCouponPrice() +"," + "paymentPrice:" + getPaymentPrice() +"," + "receivingPhone:" + getReceivingPhone() +"," + "receivingName:" + getReceivingName() +"," + "receivingAddress:" + getReceivingAddress() +"," + "goodsCount:" + getGoodsCount() +"," + "payStatus:" + getPayStatus() +"]";
	}


	public Long getMakeTime() {
		return makeTime;
	}

	public void setMakeTime(Long makeTime) {
		this.makeTime = makeTime;
	}


	public Double getSendFee() {
		return sendFee;
	}


	public void setSendFee(Double sendFee) {
		this.sendFee = sendFee;
	}


	public Double getMealFee() {
		return mealFee;
	}


	public void setMealFee(Double mealFee) {
		this.mealFee = mealFee;
	}


	public String getHouseNumber() {
		return houseNumber;
	}


	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}


	public Integer getSellerOrderStatus() {
		return sellerOrderStatus;
	}


	public void setSellerOrderStatus(Integer sellerOrderStatus) {
		this.sellerOrderStatus = sellerOrderStatus;
	}


	public Integer getRefundStatus() {
		return refundStatus;
	}


	public void setRefundStatus(Integer refundStatus) {
		this.refundStatus = refundStatus;
	}


	public Integer getReceivingStatus() {
		return receivingStatus;
	}


	public void setReceivingStatus(Integer receivingStatus) {
		this.receivingStatus = receivingStatus;
	}


	public String getNote() {
		return note;
	}


	public void setNote(String note) {
		this.note = note;
	}


	public Integer getPayType() {
		return payType;
	}


	public void setPayType(Integer payType) {
		this.payType = payType;
	}


	public String getRefundNote() {
		return refundNote;
	}


	public void setRefundNote(String refundNote) {
		this.refundNote = refundNote;
	}


	public String getPayTypeContent() {
		
		if (this.getPayType() != null) {
			//start luoshun 20160506
			/*if (this.getPayType() == PaymentMethodEnum.ALIPAY.getValue() ) {
				return PaymentMethodEnum.ALIPAY.getText();
			} else if (this.getPayType() == PaymentMethodEnum.WEIXIN.getValue()) {
				return PaymentMethodEnum.WEIXIN.getText();
			} else if (this.getPayType() == PaymentMethodEnum.RUNSUB.getValue()) {
				return PaymentMethodEnum.RUNSUB.getText();
			} else if (this.getPayType() == PaymentMethodEnum.CASHBACK.getValue()) {
				return PaymentMethodEnum.CASHBACK.getText();
			} else if (this.getPayType() == PaymentMethodEnum.REIMBURSE.getValue()) {
				return PaymentMethodEnum.REIMBURSE.getText();
			} else if (this.getPayType() == PaymentMethodEnum.UNIONPAY.getValue()) {
				return PaymentMethodEnum.UNIONPAY.getText();
			}else if(this.getPayType() == PaymentMethodEnum.FASTPAY.getValue()){
				return PaymentMethodEnum.FASTPAY.getText();
			}else if(this.getPayType() == PaymentMethodEnum.JDPAY.getValue()){
				return PaymentMethodEnum.JDPAY.getText();
			}else if(this.getPayType() == PaymentMethodEnum.YSPAY.getValue()){
				return PaymentMethodEnum.YSPAY.getText();
			}else if(this.getPayType() == PaymentMethodEnum.PAPAY.getValue()){
				return PaymentMethodEnum.PAPAY.getText();
			}*/
			return PaymentMethodEnum.getTextByValue(this.getPayType());
			//end luoshun 20160506
			
		}
		
		return payTypeContent;
	}


	public Integer getReviews() {
		return reviews;
	}


	public void setReviews(Integer reviews) {
		this.reviews = reviews;
	}


	public void setPayTypeContent(String payTypeContent) {
		this.payTypeContent = payTypeContent;
	}
	
}