import request from "@/utils/request.js";

const userApi = {
    //用户登录，成功后返回token（网关白名单接口）
    login(loginInfo) {
        return request.post("/user/login", loginInfo)
    },
    //用户注册（网关白名单接口）
    register(user) {
        return request.post("/user/register", user)
    },
    //当前登录用户的个人信息
    userInfo() {
        return request.get("/user/userInfo")
    },
    //修改个人信息
    update(user) {
        return request.put("/user", user)
    }
}

export default userApi
