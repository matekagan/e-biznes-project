
export const PHONE_CONFIG = {
    rules: [
        {
            required: true,
            message: 'Please input your phone number!'
        }
    ]
};

export const ADDRESS_CONFIG = {
    rules: [
        {
            required: true,
            message: 'Please input your current address!',
            whitespace: true
        }
    ]
};

export const EMAIL_CONFIG = {
    rules: [
        {
            required: true,
            message: 'Please input your email!'
        }
    ]
};

export const PASSWORD_CONFIG = {
    rules: [
        {
            required: true,
            message: 'Please input your Password!'
        }
    ]
};
