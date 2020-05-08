import React from 'react';
import { ajaxGet } from '../utils/ajax';


const createAdvert = ({ id, link, text }) => (
    <a className="advert" key={id} href={link}>
        {text}
    </a>
);

export default class Adverts extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            adverts: []
        };
    }

    fetchAds = () => {
        ajaxGet('adverts/random')
            .then(response => response.json())
            .then(adverts => this.setState(state => ({ ...state, adverts })));
    }

    componentDidMount() {
        this.fetchAds();
    }

    render() {
        const { adverts } = this.state;
        const ads = adverts.map(createAdvert);
        return (
            <div className="adverts-container">
                {ads}
            </div>
        );
    }
}
