import React from 'react';
import { Button, Layout } from 'antd';
import { SERVER_PATH } from '../utils/ajax';

const { Content, Header } = Layout;

const createProviderButton = provider => (
    <div className="provider" key={provider}>
        <Button size="large" type="primary" icon={provider} href={`${SERVER_PATH}/authenticate/${provider}`}>
            {provider[0].toUpperCase() + provider.substring(1)}
        </Button>
    </div>
);

export default ({ providers = [] }) => {
    const buttons = providers.map(createProviderButton);
    return (
        <Layout>
            <Content>
                <Header>
                    <div className="products-header">
                        <div>Sign In</div>
                    </div>
                </Header>
                <div className="auth-providers-container">
                    <h2>You can Sign In using the following accounts:</h2>
                    {buttons}
                </div>
            </Content>
        </Layout>
    );
};
