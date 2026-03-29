import request from './request'

export function getLabList() {
    return request({
        url: '/lab/list',
        method: 'get'
    })
}

export function addLab(data) {
    return request({
        url: '/lab/add',
        method: 'post',
        data
    })
}

export function updateLab(data) {
    return request({
        url: '/lab/update',
        method: 'put',
        data
    })
}

export function deleteLab(id) {
    return request({
        url: `/lab/${id}`,
        method: 'delete'
    })
}