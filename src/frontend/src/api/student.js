import request from './request'

export function registerStudent(data) {
    return request({
        url: '/student/register',
        method: 'post',
        data
    })
}

export function getStudentList(params) {
    return request({
        url: '/student/list',
        method: 'get',
        params
    })
}

export function getStudent(id) {
    return request({
        url: `/student/${id}`,
        method: 'get'
    })
}

export function updateStudent(data) {
    return request({
        url: '/student/update',
        method: 'put',
        data
    })
}

export function deleteStudent(id) {
    return request({
        url: `/student/${id}`,
        method: 'delete'
    })
}