import request from "@/utils/request.js";

const orderApi = {
    //根据购物车中已勾选的商品生成订单 {shippingId}
    create(order) {
        return request.post("/order", order)
    },
    //当前用户的订单列表，每项包含订单商品orderItemList
    list() {
        return request.get("/order")
    },
    //订单详情，包含订单商品明细
    detail(orderNo) {
        return request.get("/order/" + orderNo)
    },
    //根据订单号查询订单（秒杀下单后轮询订单是否落库，未落库返回null）
    selectByOrderNo(orderNo) {
        return request.get("/order/" + orderNo)
    },
    //虚拟支付，支付后订单变为待发货
    pay(orderNo) {
        return request.put("/order/" + orderNo + "/pay")
    },
    //选择收货地址，只允许未付款订单（秒杀订单支付前必须先选地址）
    setShipping(orderNo, shippingId) {
        return request.put(`/order/${orderNo}/shipping/${shippingId}`)
    },
    //确认收货，已发货订单确认后变为交易成功
    confirm(orderNo) {
        return request.put("/order/" + orderNo + "/confirm")
    }
}

export default orderApi