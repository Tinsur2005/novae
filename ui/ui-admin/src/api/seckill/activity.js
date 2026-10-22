import request from "@/utils/request.js";

const activityApi = {
    //分页查询活动列表（后台管理）
    list(activityQuery) {
        return request.get("/activity", {params: activityQuery})
    },
    //已发布且未结束的活动（含未开始）+ 商品 + Redis 实时库存
    listPublished() {
        return request.get("/activity/published")
    },
    selectById(id) {
        return request.get(`/activity/${id}`)
    },
    add(activityDTO) {
        return request.post("/activity", activityDTO)
    },
    update(id, activityDTO) {
        return request.put(`/activity/${id}`, activityDTO)
    },
    deleteById(id) {
        return request.delete(`/activity/${id}`)
    },
    //活动下全部秒杀商品（编辑回显）
    listProduct(activityId) {
        return request.get(`/activity-product/${activityId}`)
    }
}
export default activityApi
