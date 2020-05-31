import React from 'react';
import { Layout, Menu, Empty, Icon, message, notification } from 'antd';
import Categories from './Categories';
import Products from './Products';
import Cart from './Cart';
import { ajaxPost, ajaxGet, ajaxDelete } from '../utils/ajax';
import ProductView from './ProductView';
import Adverts from './Adverts';
import SignInPage from './SignInPage';
import { isAuthenticated } from '../utils/auth';

const { Content, Sider } = Layout;
const { SubMenu } = Menu;

const CART_ID = 'cartID';
const VIEWS = {
    CATEGORIES: 'categories',
    PRODUCTS: 'products',
    CART: 'cart',
    SIGN_IN: 'signIn'
};

const AUTH_PROVIDERES = ['github', 'google'];

const createCartFromList = cartProducts => cartProducts.reduce(
    (cart, { product, amount }) => ({ ...cart, [product.id]: { product, amount } }),
    {}
);

const prepareCartRequest = cart => Object.values(cart)
    .map(({ product: { id }, amount }) => ({ productID: id, amount }));


export default class MainPage extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedView: null,
            cart: {},
            selectedProduct: null,
            selectedCategory: null
        };
    }

    addProductToCart = (product) => {
        const { cart } = this.state;
        cart[product.id] = cart[product.id] ? {
            ...cart[product.id],
            amount: cart[product.id].amount + 1
        } : { product, amount: 1 };
        this.setState(state => ({ ...state, cart }), () => message.success('Product added to cart'));
    }

    incrementProduct = (id, increment = 1) => {
        const { cart } = this.state;
        const newValue = cart[id].amount + increment;
        cart[id] = { ...cart[id], amount: Math.max(newValue, 0) };
        this.setState(state => ({ ...state, cart }));
    }

    removeFromCart = (id) => {
        const { cart } = this.state;
        if (cart[id]) {
            delete cart[id];
        }
        this.setState(state => ({ ...state, cart }));
    }

    onSelect = (e) => {
        this.setState(state => ({
            ...state,
            selectedView: e.key,
            loading: true,
            selectedProduct: null,
            selectedCategory: null
        }));
    }

    selectCategory = (selectedCategory) => {
        this.setState(state => ({
            ...state,
            selectedCategory,
            selectedView: VIEWS.PRODUCTS
        }));
    }

    selectProduct = (selectedProduct) => {
        this.setState(state => ({
            ...state,
            selectedProduct,
            selectedView: null
        }));
    }

    recoverCart = (cartUUID) => {
        if (!cartUUID) {
            return;
        }
        ajaxGet(`cart/${cartUUID}/products`)
            .then(response => response.json())
            .then(createCartFromList)
            .then((cart) => {
                if (Object.keys(cart).length > 0) {
                    this.setState(state => ({ ...state, cart, cartUUID }));
                    window.localStorage.setItem(CART_ID, cartUUID);
                    notification.success({
                        message: 'Cart items sucessfully recovered'
                    });
                }
            });
    }

    saveCart = () => {
        const data = prepareCartRequest(this.state.cart);
        ajaxPost('cart/create', data)
            .then(response => response.json())
            .then(this.afterCartSaved)
            .catch(() => message.error('An error occured during query'));
    }

    afterCartSaved = ({ status, id }) => {
        if (status === 'FAILED') {
            message.error('Failed to save cart');
        } else if (status === 'CREATED') {
            window.localStorage.setItem(CART_ID, id);
            notification.success({
                message: 'Cart saved',
                description: `You cart has been saved, you can use the following idetifier to rocover it in the future: ${id}`
            });
        }
    }

    logOut = () => {
        ajaxGet('sign-out')
            .then((response) => {
                if (response.ok) {
                    notification.success({ message: 'Succefully signed out!' });
                } else {
                    notification.error({ message: 'Error during logout' });
                }
                this.forceUpdate();
            });
    }

    submitOder = (orderData, callback) => {
        const data = {
            ...orderData,
            orderProducts: prepareCartRequest(this.state.cart)
        };
        ajaxPost('orders/create', data)
            .then(response => response.json())
            .then(result => this.afterOrderSubmit(result, callback));
    }

    afterOrderSubmit = ({ status, id }, callback) => {
        if (callback) {
            callback({ status, id });
        }
        if (status === 'FAILED') {
            notification.error({
                message: 'Failed do submit Order'
            });
        } else if (status === 'CREATED' && id) {
            window.localStorage.removeItem(CART_ID);
            notification.success({
                message: 'Order Saved',
                description: `You order has been created, details will be sent to your e-mail,\n your order id = ${id}`
            });
            if (this.state.cartUUID) {
                ajaxDelete(`carts/${this.state.cartUUID}/delete`);
            }
            this.setState(state => ({ ...state, cart: {}, cartUUID: null }));
        }
    }

    componentDidMount() {
        this.recoverCart(window.localStorage.getItem(CART_ID));
    }

    render() {
        const { selectedView, cart, selectedCategory, selectedProduct } = this.state;
        const authenticated = isAuthenticated();
        return (
            <Layout style={{ minHeight: '100vh' }}>
                <Sider className="fixed-siders">
                    <div className="logo" > INSERT LOGO HERE</div>
                    <Menu theme="dark" mode="inline" selectedKeys={[selectedView]} defaultOpenKeys={[VIEWS.SIGN_IN]}>
                        <Menu.Item key={VIEWS.SIGN_IN} onClick={this.onSelect} disabled={authenticated}>
                            <div className="menu-component">
                                <span>Sign In</span>
                                <Icon type="login" className="icon-large" />
                            </div>
                        </Menu.Item>
                        <Menu.Item key="signOut" onClick={this.logOut} disabled={!authenticated}>
                            <div className="menu-component">
                                <span>Log Out</span>
                                <Icon type="logout" className="icon-large" />
                            </div>
                        </Menu.Item>
                        <SubMenu key="browse" title="Browse">
                            <Menu.Item key={VIEWS.PRODUCTS} onClick={this.onSelect}>
                                Products
                            </Menu.Item>
                            <Menu.Item key={VIEWS.CATEGORIES} onClick={this.onSelect}>
                                Categories
                            </Menu.Item>
                        </SubMenu>
                        <Menu.Item key={VIEWS.CART} onClick={this.onSelect} >
                            <div className="menu-component">
                                <span>Cart</span>
                                <Icon type="shopping-cart" className="icon-large" />
                            </div>
                        </Menu.Item>
                    </Menu>
                    <Adverts />
                </Sider>
                <Layout>
                    <Content className="static-page-content">
                        {(() => {
                            if (selectedView === VIEWS.PRODUCTS) {
                                return (<Products
                                    addToCart={this.addProductToCart}
                                    category={selectedCategory}
                                    selectProduct={this.selectProduct}
                                />);
                            } else if (selectedView === VIEWS.CATEGORIES) {
                                return <Categories selectCategory={this.selectCategory} />;
                            } else if (selectedView === VIEWS.CART) {
                                return (<Cart
                                    cart={cart}
                                    incrementAmount={this.incrementProduct}
                                    remove={this.removeFromCart}
                                    saveCart={this.saveCart}
                                    loadCart={this.recoverCart}
                                    submitOder={this.submitOder}
                                />);
                            } else if (selectedProduct && !selectedView) {
                                return <ProductView product={selectedProduct} addToCart={this.addProductToCart} />;
                            } else if (selectedView === VIEWS.SIGN_IN) {
                                return <SignInPage providers={AUTH_PROVIDERES} />;
                            }
                            return <Empty />;
                        })()}
                    </Content>
                </Layout>
            </Layout>
        );
    }
}
