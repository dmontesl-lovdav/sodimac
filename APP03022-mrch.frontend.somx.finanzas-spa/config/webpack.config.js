const HtmlWebPackPlugin = require('html-webpack-plugin');
const Dotenv = require('dotenv-webpack');
const path = require('path');

module.exports = (_env, argv) => ({
    entry: path.resolve(__dirname, '../src/index.tsx'),
    output: {
        path: path.resolve(__dirname, '../dist'),
        filename: 'bundle.[contenthash].js',
        publicPath: '/',
        clean: true
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.jsx', '.js'],
        alias: {
            '@': path.resolve(__dirname, '../src'),
            '@assets': path.resolve(__dirname, '../src/assets'),
            '@shared': path.resolve(__dirname, '../src/shared'),
            '@features': path.resolve(__dirname, '../src/features'),
            '@configuration': path.resolve(__dirname, '../src/configuration'),
            '@domain': path.resolve(__dirname, '../src/domain'),
            '@models': path.resolve(__dirname, '../src/models'),
            '@security': path.resolve(__dirname, '../src/security'),
            '@store': path.resolve(__dirname, '../src/store'),
            '@types': path.resolve(__dirname, '../src/types')
        }
    },
    module: {
        rules: [
            {
                test: /\.[jt]sx?$/,
                exclude: /node_modules/,
                use: 'babel-loader'
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    {
                        loader: 'css-loader',
                        options: { importLoaders: 1 }
                    },
                    {
                        loader: 'postcss-loader',
                        options: {
                            postcssOptions: { plugins: [require('@tailwindcss/postcss')] }
                        }
                    }
                ]
            },
            {
                test: /\.(png|jpe?g|gif|svg|ico)$/i,
                type: 'asset/resource',
                generator: { filename: 'assets/[name][hash][ext]' }
            }
        ]
    },
    plugins: [
        new Dotenv(),
        new HtmlWebPackPlugin({
            template: path.resolve(__dirname, '../public/index.html')
        })
    ],
    devServer: {
        port: 3702,
        historyApiFallback: true,
        hot: true, 
    },
    devtool: argv.mode === 'development' ? 'eval-source-map' : 'source-map'
});
