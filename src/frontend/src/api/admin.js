import request from './request'

export function adminLogin(data) {
    return request({
        url: '/admin/login',
        method: 'post',
        data
    })
}

export function adminLogout() {
    return request({
        url: '/admin/logout',
        method: 'get'
    })
}

export function getAdminInfo() {
    return request({
        url: '/admin/info',
        method: 'get'
    })
}