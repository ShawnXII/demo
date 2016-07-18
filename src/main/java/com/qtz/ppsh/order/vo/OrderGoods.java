package com.qtz.ppsh.order.vo;
import java.io.Serializable;
/**
 * <p>Title:OrderGoods</p>
 * <p>Description:订单对应商品VO类</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市擎天柱信息科技有限公司</p>
 * @author 涂鑫 -xin.tu
 * @version v1.0 2015-09-02
 */
public class OrderGoods implements Serializable {
	
	/**类的版本号*/
	private static final long serialVersionUID = 1301400735860736L;
    private Long dmId;
	/** 订单id */	private Long orderId;	/** 商品名字 */	private String goodsName;	/** 单个商品数量 */	private Integer goodsNum;	/** 商品总额 */	private Double goodsTotalPrice;	/** 商品单价 */	private Double goodsPrice;	/** 商品id */	private Long goodsId;
	/**
	 * 商品图片
	 */
	private String imgs;
	
	public Long getDmId() {	    return this.dmId;	}
	public void setDmId(Long dmId) {	    this.dmId=dmId;	}	public Long getOrderId() {	    return this.orderId;	}	public void setOrderId(Long orderId) {	    this.orderId=orderId;	}	public String getGoodsName() {	    return this.goodsName;	}	public void setGoodsName(String goodsName) {	    this.goodsName=goodsName;	}	public Integer getGoodsNum() {	    return this.goodsNum;	}	public void setGoodsNum(Integer goodsNum) {	    this.goodsNum=goodsNum;	}	public Double getGoodsTotalPrice() {	    return this.goodsTotalPrice;	}	public void setGoodsTotalPrice(Double goodsTotalPrice) {	    this.goodsTotalPrice=goodsTotalPrice;	}	public Double getGoodsPrice() {	    return this.goodsPrice;	}	public void setGoodsPrice(Double goodsPrice) {	    this.goodsPrice=goodsPrice;	}	public Long getGoodsId() {	    return this.goodsId;	}	public void setGoodsId(Long goodsId) {	    this.goodsId=goodsId;	}
		public String getImgs() {
		return imgs;
	}
	public void setImgs(String imgs) {
		this.imgs = imgs;
	}
	public String toString() {	    return "[" + "dmId:" + getDmId() +"," + "orderId:" + getOrderId() +"," + "goodsName:" + getGoodsName() +"," + "goodsNum:" + getGoodsNum() +"," + "goodsTotalPrice:" + getGoodsTotalPrice() +"," + "goodsPrice:" + getGoodsPrice() +"," + "goodsId:" + getGoodsId() +"]";	}
}