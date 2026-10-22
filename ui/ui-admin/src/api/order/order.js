import request from "@/utils/request.js";

const orderApi = {
    //分页查询订单，每项包含订单商品orderItemList
    list(orderQuery) {
        return request.get("/order/page", {params: orderQuery});
    },
    //发货
    send(orderNo) {
        return request.put(`/order/${orderNo}/send`)
    },
    //关闭订单，下单时扣掉的库存会加回去
    close(orderNo) {
        return request.put(`/order/${orderNo}/close`)
    },
    //后台修改订单（实付金额/运费/支付方式/状态）
    update(order) {
        return request.put(`/order/${order.orderNo}`, order)
    },
    //删除订单（逻辑删除）
    deleteById(orderNo) {
        return request.delete(`/order/${orderNo}`);
    }
}

export default orderApi