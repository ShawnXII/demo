package com.qtz.ppsh.order.service.consts;


/**
 * description:<br>
 *
 * @author Kevin Chang
 */

public class MsgCode
{
    /**
     * 接单
     */
    public static final String order_receiving="109115";
    /**
     * 拒绝接单
     */
    public static final String refuse_place_an_order="109116";
    /**
     * 支付未接单  退款
     */
    public static final String order_cancel="109200";
    
    /**
     * 申请退款
     */
    public static final String apply_for_refund="109117";
    
    /**
     * 同意退款
     */
    public static final String agree_to_refund="109118";
    
    /**
     * 不同意退款
     */
    public static final String no_agree_to_refund="109119";
}
