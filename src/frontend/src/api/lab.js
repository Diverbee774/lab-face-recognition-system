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

export function getLabStudents(labId) {
    return request({
        url: `/lab/${labId}/students`,
        method: 'get'
    })
}

export function addStudentToLab(labId, studentId) {
    return request({
        url: `/lab/${labId}/students/${studentId}`,
        method: 'post'
    })
}

export function removeStudentFromLab(labId, studentId) {
    return request({
        url: `/lab/${labId}/students/${studentId}`,
        method: 'delete'
    })
}

export function getAllStudents() {
    return request({
        url: '/lab/students',
        method: 'get'
    })
}