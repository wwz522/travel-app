import axios from "axios"
import store from "@/store"
import router from "@/router"
import { Message } from "element-ui"

const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || "/api",
  timeout: 60000, // 增加到60秒，AI规划需要更长时间
})

service.interceptors.request.use(
  (config) => {
    const token = store.state.token
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果没有 code 字段，可能是直接返回的数据（不应该发生，但容错处理）
    if (res && typeof res.code === 'undefined') {
      console.warn('响应数据没有 code 字段:', res)
      return res
    }
    
    if (res.code !== 200) {
      // 对于评论接口，不显示错误提示（可能只是没有评论）
      const url = response.config?.url || ''
      if (!url.includes('/interaction/comments')) {
        Message.error(res.message || "请求失败")
      }
      if (res.code === 401) {
        store.dispatch("logout")
        router.push("/login")
      }
      const error = new Error(res.message || "请求失败")
      error.response = response
      error.code = res.code
      return Promise.reject(error)
    }
    return res
  },
  (error) => {
    // 对于评论接口的 404 或其他错误，不显示提示
    const url = error.config?.url || ''
    if (!url.includes('/interaction/comments')) {
      Message.error(error.message || "网络错误")
    }
    return Promise.reject(error)
  }
)

export default service
