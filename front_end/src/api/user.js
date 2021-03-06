import { axios } from '@/utils/request'

const api = {
    userPre: '/api/user'
}
export function loginAPI(data){
    return axios({
        url:`${api.userPre}/login`,
        method: 'POST',
        data
    })
}
export function registerAPI(data){
    return axios({
        url: `${api.userPre}/register`,
        method: 'POST',
        data
    })
}
export function getUserInfoAPI(id){
    return axios({
        url: `${api.userPre}/${id}/getUserInfo`,
        method: 'GET'
    })
}

export function getAccountByEmailAPI(data){
    return axios({
        url: `${api.userPre}/getAccountByEmail`,
        method: 'POST',
        data
    })
}

export function updateUserInfoAPI(data) {
    return axios({
        url: `${api.userPre}/${data.id}/userInfo/update`,
        method: 'POST',
        data
    })
}

export function creditSetAPI(data) {
    return axios({
        url: `${api.userPre}/creditSet`,
        method: 'POST',
        data
    })
}

export function lvSetAPI(data) {
    return axios({
        url: `${api.userPre}/lvSet`,
        method: 'POST',
        data
    })
}

export function updateAvatarAPI(userid,data) {
    return axios({
        url: `${api.userPre}/${userid}/updateUserImg`,
        method: 'POST',
        data
    })
}