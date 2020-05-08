import React from 'react';
import { Layout, Collapse, Row, Col, Button, Comment, Rate, message } from 'antd';
import { createExtra } from './Product';
import { ajaxGet, ajaxPost } from '../utils/ajax';
import CommentForm from './CommentForm';

const { Header, Content } = Layout;
const { Panel } = Collapse;


const createOpinion = ({ id, rating, comment, timestamp }) => (
    <Comment
        key={id}
        author="Anonymous"
        content={(
            <div className="comment-content">
                <Rate disabled defaultValue={rating} className="comment-rating" />
                <p>{comment}</p>
            </div>
        )}
        datetime={timestamp}
    />
);

export default class ProductView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            opinions: []
        };
    }

    getOpinions = () => {
        const { product: { id } } = this.props;
        const url = `products/${id}/opinions`;
        ajaxGet(url)
            .then(response => response.json())
            .then(data => this.setState(state => ({ ...state, opinions: data })));
    }

    createOpinion = (product, rating, comment) => {
        const data = {
            product,
            rating,
            comment
        };
        ajaxPost('opinions/create', data)
            .then(() => {
                message.success('New Comment created');
                this.getOpinions();
            }).catch(() => message.error('Could not create new comment'));
    }

    componentDidMount() {
        this.getOpinions();
    }

    render() {
        const { product, addToCart } = this.props;
        const { opinions } = this.state;
        const { id, name, description, price, discount = {} } = product;
        return (
            <Layout>
                <Header>
                    <div className="product-view-header">
                        <div className="product-view-header-product">
                            <h2 className="white-font-header">{name}</h2>
                        </div>
                        <div className="product-view-header-buttons">
                            {createExtra(price, discount)}
                            <Button type="primary" onClick={() => addToCart(product)}>Add to Cart</Button>
                        </div>
                    </div>
                </Header>
                <Content>
                    <Collapse>
                        <Panel header="Product Description" key="1">
                            <Row gutter={16}>
                                <Col span={6}>
                                    <img
                                        alt="example"
                                        src="https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png"
                                    />
                                </Col>
                                <Col span={18}>
                                    <p>{description}</p>
                                </Col>
                            </Row>
                        </Panel>
                        <Panel header="Opinions" key="2">
                            {opinions.map(createOpinion)}
                            <CommentForm key="comment-form" createOpinion={(rating, comment) => this.createOpinion(id, rating, comment)} />
                        </Panel>
                    </Collapse>
                </Content>
            </Layout>
        );
    }
}
