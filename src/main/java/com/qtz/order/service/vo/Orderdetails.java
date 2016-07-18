package com.qtz.order.service.vo;

import java.io.Serializable;

import com.qtz.base.dto.order.StoreGoods;

public class Orderdetails implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3849602211638959080L;
	
	/**  */
	private Long dmId;
	/** 订单id */
	private Long orderId;
	/** 商品名字 */
	private String goodsName;
	/** 单个商品数量 */
	private Integer goodsNum;
	/** 商品总额 */
	private Double goodsTotalPrice;
	/** 商品单价 */
	private Double goodsPrice;
	/** 商品id */
	private Long goodsId;
	
	/** 商品 */
	private StoreGoods storeGoods;
	
	/** 下单时间 */
	private Long crtime;
	/** 接单时间 */
	private Long reOrderTime;
	/** 订单成功时间 */
	private Long successTime;
	/** 退款时间 */
	private Long refundTime;
	/** 订单关闭时间 */
	private Long closeTime;
	/** 订单状态 */
	private Integer status;
	/** 付款时间 */
	private Long paymentTime;
	/**
	 * 拒绝订单时间
	 */
	private Long refusedTime;
	/**
	 * 取消订单时间
	 */
	private Long cancelTime;
	
	/** 订单 */
	private Order order;
	
	public Long getDmId() {
		return dmId;
	}
	public void setDmId(Long dmId) {
		this.dmId = dmId;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public Integer getGoodsNum() {
		return goodsNum;
	}
	public void setGoodsNum(Integer goodsNum) {
		this.goodsNum = goodsNum;
	}
	public Double getGoodsTotalPrice() {
		return goodsTotalPrice;
	}
	public void setGoodsTotalPrice(Double goodsTotalPrice) {
		this.goodsTotalPrice = goodsTotalPrice;
	}
	public Double getGoodsPrice() {
		return goodsPrice;
	}
	public void setGoodsPrice(Double goodsPrice) {
		this.goodsPrice = goodsPrice;
	}
	public Long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}
	public StoreGoods getStoreGoods() {
		return storeGoods;
	}
	public void setStoreGoods(StoreGoods storeGoods) {
		this.storeGoods = storeGoods;
	}
	public Long getCrtime() {
		return crtime;
	}
	public void setCrtime(Long crtime) {
		this.crtime = crtime;
	}
	public Long getReOrderTime() {
		return reOrderTime;
	}
	public void setReOrderTime(Long reOrderTime) {
		this.reOrderTime = reOrderTime;
	}
	public Long getSuccessTime() {
		return successTime;
	}
	public void setSuccessTime(Long successTime) {
		this.successTime = successTime;
	}
	public Long getRefundTime() {
		return refundTime;
	}
	public void setRefundTime(Long refundTime) {
		this.refundTime = refundTime;
	}
	public Long getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(Long closeTime) {
		this.closeTime = closeTime;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getPaymentTime() {
		return paymentTime;
	}
	public void setPaymentTime(Long paymentTime) {
		this.paymentTime = paymentTime;
	}
	public Long getRefusedTime() {
		return refusedTime;
	}
	public void setRefusedTime(Long refusedTime) {
		this.refusedTime = refusedTime;
	}
	public Long getCancelTime() {
		return cancelTime;
	}
	public void setCancelTime(Long cancelTime) {
		this.cancelTime = cancelTime;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
}
