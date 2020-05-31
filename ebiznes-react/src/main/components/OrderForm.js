import React from 'react';
import {
    Form,
    Input,
    Button,
    Modal,
    Result
} from 'antd';
import {
    PHONE_CONFIG,
    ADDRESS_CONFIG
} from '../utils/formUtils';

const { TextArea } = Input;

const ORDER_STATUS = {
    CREATING: 'CREATING',
    CREATED: 'CREATED',
    FAILED: 'FAILED',
    NEW: 'NEW'
};

const RESULT_TITLE = {
    CREATED: 'Order has been succesfully created',
    FAILED: 'Error during processing of the order'
};


class OrderForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            confirmLoading: false,
            orderStatus: ORDER_STATUS.NEW
        };
    }

    handleSubmit = (e) => {
        e.preventDefault();
        this.props.form.validateFieldsAndScroll((err, values) => {
            if (!err) {
                this.props.submit(
                    values,
                    this.handleOrderCreate
                );
            }
        });
    };

    handleOrderCreate = ({ status, id }) => {
        this.setState(state => ({ ...state, orderStatus: status, orderID: id }));
    }

    createForm = () => {
        const { getFieldDecorator } = this.props.form;
        return (
            <Form onSubmit={this.handleSubmit}>
                <Form.Item label="Phone Number">
                    {getFieldDecorator('phone', PHONE_CONFIG)(<Input />)}
                </Form.Item>
                <Form.Item label="Address">
                    {getFieldDecorator('address', ADDRESS_CONFIG)(<TextArea rows={4} />)}
                </Form.Item>
                <Form.Item >
                    <Button type="primary" htmlType="submit">
                        Submit Order
                    </Button>
                </Form.Item>
            </Form>
        );
    }

    createresult() {
        const { orderStatus, orderID } = this.state;
        let subTitle = 'An error occured during processing of your order, please, try again later';
        if (orderStatus === ORDER_STATUS.CREATED && orderID) {
            subTitle = `Your order has been succesfully created. Your order ID = ${orderID}. You will receive confirmation and further instructions via email.`;
        }
        return (
            <Result
                status={orderStatus === ORDER_STATUS.CREATED ? 'success' : 'error'}
                title={RESULT_TITLE[orderStatus]}
                subTitle={subTitle}
            />
        );
    }

    render() {
        const { confirmLoading, orderStatus } = this.state;
        const { visible, hide } = this.props;
        const orderCreateFinished = orderStatus === ORDER_STATUS.FAILED || orderStatus === ORDER_STATUS.CREATED;
        const content = orderCreateFinished ? this.createresult() : this.createForm();
        return (
            <Modal
                title="Submit Order"
                visible={visible}
                onOk={hide}
                confirmLoading={confirmLoading}
                onCancel={hide}
                width={720}
                okButtonProps={{ style: { display: 'none' } }}
            >
                {content}
            </Modal>
        );
    }
}

export default Form.create({ name: 'register' })(OrderForm);
