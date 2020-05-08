import React from 'react';
import { Row, Col, Card, Button, Layout, Empty, Alert, Icon } from 'antd';
import { reshapeList } from '../utils/arrayUtils';
import Spinner from './Spinner';
import { ajaxGet, QUERY_STATUS } from '../utils/ajax';

const { Content, Header } = Layout;

const COLS_NUM = 4;


export default class Categories extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            categories: [],
            queryStatus: QUERY_STATUS.FINISHED
        };
    }

    updateData = (data) => {
        this.setState(state => ({
            ...state,
            queryStatus: QUERY_STATUS.FINISHED,
            categories: data
        }));
    }

    fetchData = () => {
        this.setState(state => ({
            ...state,
            queryStatus: QUERY_STATUS.IN_PROGRESS
        }));
        ajaxGet('categories/list')
            .then(response => response.json())
            .then(this.updateData)
            .catch(() => this.setState({ queryStatus: QUERY_STATUS.ERROR }));
    }

    createCol = el => (
        <Col key={el.id} span={24 / COLS_NUM}>
            <Card
                className="card-empty"
                cover={
                    <img
                        alt="example"
                        src="https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png"
                    />
                }
                actions={[
                    <Button type="primary" onClick={() => this.props.selectCategory(el)}>
                        <Icon type="arrow-right" key="view" />
                    </Button>
                ]}
                title={el.name}
            />
        </Col>
    );

    createRow = (cols = []) => (
        <Row gutter={[16, 16]}>
            {cols.map(this.createCol)}
        </Row>
    );

    componentDidMount() {
        this.fetchData();
    }

    render() {
        const { categories, queryStatus } = this.state;
        const rows = reshapeList(categories, COLS_NUM);
        let mainContent = <Empty />;
        if (queryStatus === QUERY_STATUS.FINISHED && categories.length > 0) {
            mainContent = (
                <div>
                    {rows.map(this.createRow)}
                </div>
            );
        } else if (queryStatus === QUERY_STATUS.IN_PROGRESS) {
            mainContent = <Spinner />;
        } else if (queryStatus === QUERY_STATUS.ERROR) {
            mainContent = <Alert type="error" message="error" />;
        }
        return (
            <Layout>
                <Content>
                    <Header>
                        <div>
                            <h2 style={{ color: 'white' }}>Categories</h2>
                        </div>
                    </Header>
                    {mainContent}
                </Content>
            </Layout>
        );
    }
}
