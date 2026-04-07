import request from './request'

export function recognizeFace(data) {
    return request({
        url: '/access/recognize',
        method: 'post',
        data
    })
}