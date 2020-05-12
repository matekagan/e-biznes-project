import React from 'react';
import { Row, Col, Layout, Empty, Alert, Input, Button, Icon } from 'antd';
import Spinner from './Spinner';
import { reshapeList } from '../utils/arrayUtils';
import Product from './Product';
import { ajaxGet, QUERY_STATUS } from '../utils/ajax';

const { Search } = Input;
const { Content, Header } = Layout;

const COLS_NUM = 4;

export default class Products extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            products: [],
            queryStatus: QUERY_STATUS.FINISHED,
            searchText: ''
        };
    }

    handleSearch = (text) => {
        if (!text || text === '') {
            return;
        }
        this.setState(state => ({ ...state, searchText: text }), this.fetchData);
    }

    createCol = product => (
        <Col key={product.id} span={24 / COLS_NUM}>
            <Product
                {...product}
                addToCart={() => this.props.addToCart(product)}
                selectProduct={() => this.props.selectProduct(product)}
            />
        </Col>
    );

    createRow = (cols = []) => (
        <Row gutter={[16, 16]}>
            {cols.map(this.createCol)}
        </Row>
    );


    updateData = (data) => {
        this.setState(state => ({
            ...state,
            queryStatus: QUERY_STATUS.FINISHED,
            products: data
        }));
    }

    clearSearchResult = () => {
        this.setState(state => ({ ...state, searchText: '' }), this.fetchData);
    }

    fetchData = () => {
        const { searchText } = this.state;
        const { category } = this.props;
        this.setState(state => ({
            ...state,
            queryStatus: QUERY_STATUS.IN_PROGRESS
        }));
        let dataPath;
        if (category) {
            dataPath = `categories/${category.id}/products`;
        } else {
            dataPath = `products/${(!searchText || searchText === '') ? 'list' : `search?name=${searchText}`}`;
        }
        ajaxGet(dataPath)
            .then(response => response.json())
            .then(this.updateData)
            .catch(() => this.setState({ queryStatus: QUERY_STATUS.ERROR }));
    }

    componentDidMount() {
        this.fetchData();
    }

    prepareMainContent = () => {
        const { products, queryStatus } = this.state;
        let mainContent = <Empty />;
        if (queryStatus === QUERY_STATUS.FINISHED && products.length > 0) {
            mainContent = (
                <div>
                    {reshapeList(products, COLS_NUM).map(this.createRow)}
                </div>
            );
        } else if (queryStatus === QUERY_STATUS.IN_PROGRESS) {
            mainContent = <Spinner />;
        } else if (queryStatus === QUERY_STATUS.ERROR) {
            mainContent = <Alert type="error" message="error" />;
        }
        return mainContent;
    }

    render() {
        const { searchText } = this.state;
        const { category } = this.props;

        let title = 'Products';
        if (searchText) {
            title = 'Search results';
        } else if (category) {
            title = `Category: ${category.name}`;
        }
        return (
            <Layout>
                <Content>
                    <Header>
                        <div className="products-header">
                            <div>{title}</div>
                            {searchText && <Button onClick={this.clearSearchResult} type="danger">Clear<Icon type="close" /></Button>}
                            {category ? null : <Search
                                placeholder="search for product"
                                enterButton
                                style={{ margin: '10px 0' }}
                                className="sharp-search-box"
                                size="large"
                                onSearch={val => this.handleSearch(val)}
                            />}
                        </div>
                    </Header>
                    {this.prepareMainContent()}
                </Content>
            </Layout>
        );
    }
}
