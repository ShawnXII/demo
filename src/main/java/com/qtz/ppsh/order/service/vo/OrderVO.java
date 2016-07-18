package com.qtz.ppsh.order.service.vo;

import java.util.List;

public class OrderVO {

	//购买用户 
	private Long userId;
	//派送费
	private Double sendFee;
	//餐盒费
	private Double mealFee;
	//订单总额
	private Double orderPrice;
	//优惠金额
	private Double couponPrice;
	//付款金额
	private Double paymentPrice;
	//收货人
	private String receivingName;
	//收货手机号码--联系方式
	private String receivingPhone;
	//收货地址
	private String receivingAddress;
	//支付成功时间--付款时间
	private Long chargeTime;
	//预约时间--期望到达时间
	private Long makeTime;
	//支付类型--付款方式
	private String payType;
	//订单号
	private Long dmId;
	//备注
	private String note;
	//退款备注
	private String refundNote;
	//订单商品
	private List<OrderGoodsVo> goods;
	
	public class OrderGoodsVo{
		//商品图片
		private String imgs;
		//商品名称
		private String goodsName;
		//商品单价
		private Double goodsPrice;
		//商品数量
		private int goodsCount;
		
		public String getImgs() {
			return imgs;
		}
		public void setImgs(String imgs) {
			this.imgs = imgs;
		}
		public String getGoodsName() {
			return goodsName;
		}
		public void setGoodsName(String goodsName) {
			this.goodsName = goodsName;
		}
		public Double getGoodsPrice() {
			return goodsPrice;
		}
		public void setGoodsPrice(Double goodsPrice) {
			this.goodsPrice = goodsPrice;
		}
		public int getGoodsCount() {
			return goodsCount;
		}
		public void setGoodsCount(int goodsCount) {
			this.goodsCount = goodsCount;
		}
	}
	
	public List<OrderGoodsVo> getGoods() {
		return goods;
	}
	public void setGoods(List<OrderGoodsVo> goods) {
		this.goods = goods;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
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
	public Double getOrderPrice() {
		return orderPrice;
	}
	public void setOrderPrice(Double orderPrice) {
		this.orderPrice = orderPrice;
	}
	public Double getCouponPrice() {
		return couponPrice;
	}
	public void setCouponPrice(Double couponPrice) {
		this.couponPrice = couponPrice;
	}
	public Double getPaymentPrice() {
		return paymentPrice;
	}
	public void setPaymentPrice(Double paymentPrice) {
		this.paymentPrice = paymentPrice;
	}
	public String getReceivingName() {
		return receivingName;
	}
	public void setReceivingName(String receivingName) {
		this.receivingName = receivingName;
	}
	public String getReceivingPhone() {
		return receivingPhone;
	}
	public void setReceivingPhone(String receivingPhone) {
		this.receivingPhone = receivingPhone;
	}
	public String getReceivingAddress() {
		return receivingAddress;
	}
	public void setReceivingAddress(String receivingAddress) {
		this.receivingAddress = receivingAddress;
	}
	public Long getChargeTime() {
		return chargeTime;
	}
	public void setChargeTime(Long chargeTime) {
		this.chargeTime = chargeTime;
	}
	public Long getMakeTime() {
		return makeTime;
	}
	public void setMakeTime(Long makeTime) {
		this.makeTime = makeTime;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public Long getDmId() {
		return dmId;
	}
	public void setDmId(Long dmId) {
		this.dmId = dmId;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getRefundNote() {
		return refundNote;
	}
	public void setRefundNote(String refundNote) {
		this.refundNote = refundNote;
	}
}
