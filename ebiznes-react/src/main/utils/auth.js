import Cookie from 'js-cookie';

export const getAuthToken = () => Cookie.get('csrfToken');

export const isAuthenticated = () => !!getAuthToken();
