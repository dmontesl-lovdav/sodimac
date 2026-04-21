const path = require('path');
const HtmlWebPackPlugin = require('html-webpack-plugin');
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const Dotenv = require('dotenv-webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const deps = require('../package.json').dependencies;

module.exports = (_, argv) => {
  const isProduction = argv.mode === 'production';

  return {
    output: {
      publicPath: isProduction ? '/' : 'http://localhost:3703/',
      path: path.resolve(__dirname, '../dist'),
      filename: '[name].[contenthash].js',
      clean: true,
    },

    resolve: {
      extensions: ['.tsx', '.ts', '.jsx', '.js', '.json'],
      alias: {
        '@': path.resolve(__dirname, '../src'),
        '@shared': path.resolve(__dirname, '../src/shared'),
        '@features': path.resolve(__dirname, '../src/features'),
        '@store': path.resolve(__dirname, '../src/store'),
        '@services': path.resolve(__dirname, '../src/services'),
        '@utils': path.resolve(__dirname, '../src/utils'),
      },
    },

    devServer: {
      port: 3703,
      historyApiFallback: true,
      hot: true,
      headers: {
        'Access-Control-Allow-Origin': '*',
      },
    },

    module: {
      rules: [
        {
          test: /\.(css)$/,
          use: ['style-loader', 'css-loader', 'postcss-loader'],
        },
        {
          test: /\.(ts|tsx|js|jsx)$/,
          exclude: /node_modules/,
          use: {
            loader: 'babel-loader',
          },
        },
        {
          test: /\.(png|svg|jpg|jpeg|gif)$/i,
          type: 'asset/resource',
        },
      ],
    },

    plugins: [
      new ModuleFederationPlugin({
        name: 'catalogos',
        filename: 'remoteEntry.js',
        remotes: {},
        exposes: {
          './App': './src/App.tsx',
          './Card': './src/Card.tsx',
        },
        shared: {
          ...deps,
          react: {
            singleton: true,
            requiredVersion: deps.react,
          },
          'react-dom': {
            singleton: true,
            requiredVersion: deps['react-dom'],
          },
          'react-router-dom': {
            singleton: true,
            requiredVersion: deps['react-router-dom'],
          },
        },
      }),
      new HtmlWebPackPlugin({
        template: './src/index.html',
      }),
      new Dotenv({
        path: './.env.development',
        safe: false,
        systemvars: true,
        defaults: false,
      }),
      new CopyWebpackPlugin({
        patterns: [
          {
            from: 'public',
            to: '',
            globOptions: {
              ignore: ['**/index.html'],
            },
            noErrorOnMissing: true,
          },
        ],
      }),
    ],
  };
};








