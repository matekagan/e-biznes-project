import React from 'react';
import { Rate, Form, Button, Input, Comment } from 'antd';

const { TextArea } = Input;

const DEFAULT_STATE = {
    rating: 3,
    comment: ''
};

export default class OpinionForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = DEFAULT_STATE;
    }

    handleRatingChange = newValue => this.setState(state => ({ ...state, rating: newValue }))

    handleCommentChange = (e) => {
        const { target: { value } } = e;
        this.setState(state => ({ ...state, comment: value }));
    }

    handleformSubmit = () => {
        const { rating, comment } = this.state;
        this.props.createOpinion(rating, comment);
        this.setState(DEFAULT_STATE);
    }

    render() {
        const { rating, comment } = this.state;
        const { disabled = false } = this.props;
        return (
            <Comment
                content={(
                    <div className="comment-content">
                        <Rate value={rating} onChange={this.handleRatingChange} className="comment-rating" />
                        <Form.Item>
                            <TextArea rows={4} onChange={this.handleCommentChange} value={comment} />
                        </Form.Item>
                        <Form.Item>
                            <Button type="primary" onClick={this.handleformSubmit} disabled={disabled}>
                                Add Comment
                            </Button>
                            {disabled && <span className="not-signed-message">You need be Signed Up in order to post opinions</span>}
                        </Form.Item>
                    </div>
                )}
            />
        );
    }
}
