import React, { useState } from 'react';
import { Table, Icon, Button, Layout, Input } from 'antd';
import { parsePrice } from '../utils/numberUtils';
import OrderForm from './OrderForm';
import { ajaxPost } from '../utils/ajax';

const { Header, Content } = Layout;
const { Search } = Input;

const createColumns = (onMinus, onPlus, remove) => [
    {
        title: '',
        key: 'actions',
        render: (text, { id }) => <Button type="danger" onClick={() => remove(id)}><Icon type="close" /></Button>
    },
    {
        title: 'Product',
        dataIndex: 'product',
        key: 'product'
    },
    {
        title: 'Price',
        dataIndex: 'price',
        key: 'price',
        render: parsePrice
    },
    {
        title: 'Discount',
        dataIndex: 'discount',
        key: 'discount',
        render: parsePrice
    },
    {
        title: 'Amount',
        dataIndex: 'amount',
        key: 'amount',
        render: (text, { id, amount }) => (
            <span className="amount-item">
                <button className="amount-button"><Icon type="minus-square" theme="twoTone" onClick={() => onMinus(id)} /></button>
                <span>{amount}</span>
                <button className="amount-button"><Icon type="plus-square" theme="twoTone" onClick={() => onPlus(id)} /></button>
            </span>
        )
    },
    {
        title: 'Total',
        dataIndex: 'total',
        key: 'total',
        render: parsePrice
    }
];

const createDataSourceItem = ({ product: { id, name, price, discount = {} }, amount }) => {
    const disc = discount.discount || 0;
    const priceAdjusted = price - disc;
    return {
        id,
        product: name,
        price: priceAdjusted,
        amount,
        discount: disc,
        total: priceAdjusted * amount
    };
};

export default ({ cart, incrementAmount, remove, saveCart, loadCart, submitOder }) => {
    const [formVisible, setFormVisivility] = useState(false);

    const dataSource = Object.values(cart).map(createDataSourceItem);
    const columns = createColumns(
        id => incrementAmount(id, -1),
        id => incrementAmount(id, 1),
        remove
    );
    const total = dataSource.map(row => row.total).reduce((val1, val2) => val1 + val2, 0);
    const footer = () => (
        <div className="cart-table-footer">
            <div>
                <Button type="primary" className="table-footer-button" onClick={() => saveCart()}>Save Cart</Button>
                <Button type="primary" className="table-footer-button" onClick={() => setFormVisivility(true)}>Check Out</Button>
            </div>
            <span>Total cart value: {parsePrice(total)}</span>
        </div>
    );
    return (
        <Layout>
            <Header>
                <div className="cart-header">
                    <h2 className="white-font-header">Cart</h2>
                    <div className="search-container">
                        <Search
                            placeholder="Retrieve saved cart"
                            size="default"
                            enterButton={<Icon type="cloud-download" />}
                            onSearch={val => loadCart(val)}
                        />
                    </div>
                </div>
            </Header>
            <Content>
                <div className="cart">
                    <div className="cart-container">
                        <div className="cart-table-container">
                            <Table
                                dataSource={dataSource}
                                columns={columns}
                                pagination={false}
                                size="middle"
                                footer={footer}
                            />
                            <OrderForm
                                visible={formVisible}
                                hide={() => setFormVisivility(false)}
                                submit={orderData => submitOder(orderData, () => setFormVisivility(false))}
                            />
                        </div>
                    </div>
                </div>
            </Content>
        </Layout>
    );
};
