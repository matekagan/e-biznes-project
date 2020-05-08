import React from 'react';
import { Card, Button, Badge, Icon } from 'antd';
import { parsePrice } from '../utils/numberUtils';

export const createExtra = (price, discount = {}) => {
    const priceAdjusted = price - (discount.discount || 0);
    const disc = Math.round((price - priceAdjusted) / (price / 100));
    const text = disc === 0 ? '' : `-${disc} %`;
    return (
        <Badge count={text}>
            <span className="price">
                {parsePrice(priceAdjusted)}
            </span>
        </Badge>
    );
};

export default ({ name, price, discount = {}, addToCart, selectProduct }) => (
    <Card
        className="card-empty"
        cover={
            <img
                alt="example"
                src="https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png"
            />
        }
        actions={[
            <Button type="primary" onClick={() => addToCart()}><Icon type="plus" key="add" /></Button>,
            <Button type="primary" onClick={() => selectProduct()}><Icon type="arrow-right" key="view" /></Button>
        ]}
        title={name}
        extra={createExtra(price, discount)}
    />
);
