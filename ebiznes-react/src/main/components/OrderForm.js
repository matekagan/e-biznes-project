import React from 'react';
import {
    Form,
    Input,
    Checkbox,
    Button,
    Modal
} from 'antd';
import {
    EMAIL_CONFIG,
    PHONE_CONFIG,
    NAME_CONFIG,
    ADDRESS_CONFIG,
    DELIVERY_CHECKBOX_CONFIG
} from '../utils/formUtils';

const { TextArea } = Input;

class OrderForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            confirmLoading: false
        };
    }

    handleSubmit = (e) => {
        e.preventDefault();
        this.props.form.validateFieldsAndScroll((err, values) => {
            if (!err) {
                console.log('Received values of form: ', values);
                this.props.submit({...values, homeDelivery: values.homeDelivery || false});
            }
        });
    };


    render() {
        const { getFieldDecorator } = this.props.form;
        const { confirmLoading } = this.state;
        const { visible, hide } = this.props;

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
                <Form onSubmit={this.handleSubmit}>
                    <Form.Item label="First name">
                        {getFieldDecorator('firstName', NAME_CONFIG)(<Input />)}
                    </Form.Item>
                    <Form.Item label="Last name">
                        {getFieldDecorator('lastName', NAME_CONFIG)(<Input />)}
                    </Form.Item>
                    <Form.Item label="E-mail">
                        {getFieldDecorator('email', EMAIL_CONFIG)(<Input />)}
                    </Form.Item>
                    <Form.Item label="Phone Number">
                        {getFieldDecorator('phone', PHONE_CONFIG)(<Input />)}
                    </Form.Item>
                    <Form.Item label="Address">
                        {getFieldDecorator('address', ADDRESS_CONFIG)(<TextArea rows={4} />)}
                    </Form.Item>
                    <Form.Item>
                        {getFieldDecorator('homeDelivery', DELIVERY_CHECKBOX_CONFIG)(<Checkbox>Home Devilery</Checkbox>)}
                    </Form.Item>
                    <Form.Item >
                        <Button type="primary" htmlType="submit">
                            Submit Order
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        );
    }
}

const WrappedOrderForm = Form.create({ name: 'register' })(OrderForm);

export default WrappedOrderForm;
