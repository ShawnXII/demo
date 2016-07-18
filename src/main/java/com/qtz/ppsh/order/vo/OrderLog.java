package com.qtz.ppsh.order.vo;

import java.io.Serializable;

/**

 * <p>Title:OrderLog</p>

 * <p>Description:订单VO类</p>

 * <p>Copyright: Copyright (c) 2015</p>

 * <p>Company: 深圳市擎天柱信息科技有限公司</p>

 * @author 涂鑫 -xin.tu

 * @version v1.0 2015-09-22

 */

public class OrderLog implements Serializable {

	

	/**类的版本号*/

	private static final long serialVersionUID = 1329561157453824L;



	
	/**  */
	private Long dmId;
	/** 订单id */
	private Long orderId;
	/** 下单时间 */
	private Long time;
	/** 订单状态 */
	/**
	 * 0：订单交易成功，1：未支付，2：商家同意退款，3：商家拒绝接单，4：商家已经确认接单，5：订单支付成功(未接单)，6：订单取消，7：申请退款中，8：拒绝退款
	 */
	private Integer status;
	/**
	 * 备注
	 */
	private String notes;
	public Long getDmId() {
	    return this.dmId;
	}
	public void setDmId(Long dmId) {
	    this.dmId=dmId;
	}
	public Long getOrderId() {
	    return this.orderId;
	}
	public void setOrderId(Long orderId) {
	    this.orderId=orderId;
	}
	public Long getTime() {
	    return this.time;
	}
	public void setTime(Long time) {
	    this.time=time;
	}
	public Integer getStatus() {
	    return this.status;
	}
	public void setStatus(Integer status) {
	    this.status=status;
	}
	
	public String getNotes() {
    return notes;
  }
  public void setNotes(String notes) {
    this.notes = notes;
  }
}