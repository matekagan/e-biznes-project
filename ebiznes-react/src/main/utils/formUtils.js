export const EMAIL_CONFIG = {
    rules: [
        {
            type: 'email',
            message: 'The input is not valid E-mail!'
        },
        {
            required: true,
            message: 'Please input your E-mail!'
        }
    ]
};

export const PHONE_CONFIG = {
    rules: [
        {
            required: true,
            message: 'Please input your phone number!'
        }
    ]
};

export const NAME_CONFIG = {
    rules: [
        {
            required: true,
            message: 'Please input your name!',
            whitespace: true
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

export const DELIVERY_CHECKBOX_CONFIG = {
    valuePropName: 'checked'
};
