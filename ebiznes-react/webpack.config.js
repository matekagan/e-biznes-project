const path = require('path');

module.exports = {
    entry: './src/app.js',
    output: {
        path: path.join(__dirname, 'public'),
        filename: 'bundle.js'
    },
    module: {
        rules: [
            {
                loader: 'babel-loader',
                test: /\.js$/,
                exclude: /node_modules/
            },
            {
                test: /\.s?css$/,
                use: [
                    'style-loader',
                    'css-loader',
                    'sass-loader'
                ]
            }
        ]
    },
    devtool: 'cheap-module-eval-source-map',
    devServer: {
        contentBase: path.join(__dirname, 'public')
        // disableHostCheck: true,
        // allowedHosts: [
        //     'http://localhost:8888'
        // ],
        // proxy: {
        //     '/api': {
        //         target: 'http://localhost:8888',
        //         pathRewrite: { '^/api': '' },
        //         changeOrigin: true,
        //         headers: {
        //             Host: 'http://localhost:8888'
        //         }
        //     }
        // }
    }
};
