import request from "@/utils/request.js";

const userApi = {
    list(userQuery) {
        return request.get("/user", {params: userQuery});
    },
    //修改用户状态(启用/禁用)
    updateStatus(id, status) {
        return request.put(`/user/${id}/status/${status}`)
    },
    deleteById(id) {
        return request.delete(`/user/${id}`);
    },
    deleteAll(ids) {
        // axios 的 delete 第2个参数是 config，请求体必须放在 data 字段里
        return request.delete("/user", {data: ids})
    }
}

export default  userApi
