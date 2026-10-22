import request from "@/utils/request.js";

const seckillApi = {
    //已发布且未结束的活动（含未开始）+ 商品 + Redis 实时库存
    listPublished() {
        return request.get("/activity/published")
    },
    //秒杀下单，预扣成功返回订单号（订单由 order-service 异步落库）
    seckill(seckillQuery) {
        return request.post("/seckill", seckillQuery)
    }
}

export default seckillApi
