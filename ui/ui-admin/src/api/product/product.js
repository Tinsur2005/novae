import request from "@/utils/request.js";

const productApi = {
    list(productQuery) {
        return request.get("/product", {params: productQuery})
    },
    deleteById(id) {
        return request.delete(`/product/${id}`)
    },
    deleteAll(ids) {
        // axios 的 delete 第2个参数是 config，请求体必须放在 data 字段里
        return request.delete("/product", {data: ids})
    },
    add(product) {
        //product以JSON形式传递到后台
        return request.post("/product", product)
    },
    selectById(id) {
        return request.get(`/product/${id}`)
    },
    update(id, product) {
        return request.put(`/product/${id}`, product)
    },
    //修改商品状态(上架/下架)
    updateStatus(id, status) {
        return request.put(`/product/${id}/status/${status}`)
    }
}

export default productApi