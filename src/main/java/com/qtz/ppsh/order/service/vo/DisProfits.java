package com.qtz.ppsh.order.service.vo;
/**
 * 分润
 * <p>Title:DisProfits</p>
 * <p>Description:(用一句话描述该文件做什么)</p>
 * <p>Copyright: Copyright (c) 2015</p>
 * <p>Company: 深圳市爱免费信息科技有限公司</p>
 * @author 李昌波 - lichangbo
 * @version v1.0 2015年12月8日
 */
public class DisProfits {
	
	private double sellerDis=9;//商家折扣
	
	private double backGloadPercen=50;//金币返现比例
	
	private double backGloadMax=5000;//金币最高返现金额
	
	private double givingGloadPercen=50;//金币赠送比例
	
	private double givingGloadMax=5000;//最高赠送数量


	public double getSellerDis() {
		return sellerDis;
	}

	public void setSellerDis(double sellerDis) {
		this.sellerDis = sellerDis;
	}

	public double getBackGloadPercen() {
		return backGloadPercen;
	}

	public void setBackGloadPercen(double backGloadPercen) {
		this.backGloadPercen = backGloadPercen;
	}

	public double getGivingGloadPercen() {
		return givingGloadPercen;
	}

	public void setGivingGloadPercen(double givingGloadPercen) {
		this.givingGloadPercen = givingGloadPercen;
	}

	public double getBackGloadMax() {
		return backGloadMax;
	}

	public void setBackGloadMax(double backGloadMax) {
		this.backGloadMax = backGloadMax;
	}

	public double getGivingGloadMax() {
		return givingGloadMax;
	}

	public void setGivingGloadMax(double givingGloadMax) {
		this.givingGloadMax = givingGloadMax;
	}
	
	
	
	
	
}
