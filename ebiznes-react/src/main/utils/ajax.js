
const createRequest = (url, method = 'GET', data) => {
    const headers = {};
    if (data) {
        headers['Content-Type'] = 'application/json';
    }
    const config = {
        headers,
        mode: 'cors',
        method
    };
    if (data) {
        config.body = JSON.stringify(data);
    }
    return fetch(`http://localhost:8888/${url}`, config);
};

export const ajaxGet = url => createRequest(url, 'GET');

export const ajaxPost = (url, data) => createRequest(url, 'POST', data);

export const ajaxDelete = (url, data) => createRequest(url, 'DLETE', data);

export const QUERY_STATUS = {
    FINISHED: 'finished',
    IN_PROGRESS: 'in progress',
    ERROR: 'error'
};
