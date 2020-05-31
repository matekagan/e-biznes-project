import { getAuthToken } from './auth';

export const SERVER_PATH = 'http://localhost:8888';

const createRequest = (url, method = 'GET', data = null) => {
    const headers = {
        'Csrf-Token': getAuthToken()
    };
    if (data) {
        headers['Content-Type'] = 'application/json';
    }
    const config = {
        headers,
        mode: 'cors',
        method,
        credentials: 'include'
    };
    if (data) {
        config.body = JSON.stringify(data);
    }
    return fetch(`${SERVER_PATH}/${url}`, config);
};

export const ajaxGet = url => createRequest(url, 'GET');

export const ajaxPost = (url, data) => createRequest(url, 'POST', data);

export const ajaxDelete = (url, data) => createRequest(url, 'DLETE', data);

export const QUERY_STATUS = {
    FINISHED: 'finished',
    IN_PROGRESS: 'in progress',
    ERROR: 'error'
};
