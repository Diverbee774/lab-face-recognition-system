import axios from 'axios'
import router from '@/router'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = 'Bearer ' + token
    }
    return config
})

request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code && res.code !== 200) {
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
        return res
    },
    error => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            router.push('/login')
        } else if (error.response?.status === 403) {
            alert('无权限')
        } else {
            alert('请求失败: ' + (error.response?.data?.msg || error.message))
        }
        return Promise.reject(error)
    }
)

export default request