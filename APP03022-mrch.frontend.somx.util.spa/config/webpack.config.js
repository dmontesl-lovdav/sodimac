const HtmlWebPackPlugin = require('html-webpack-plugin');
const { ModuleFederationPlugin } = require('webpack').container;
const Dotenv = require('dotenv-webpack');
const path = require('path');
const deps = require('../package.json').dependencies;
const CopyWebpackPlugin = require('copy-webpack-plugin');
const babelConfig = require('../babel.config.js');

const mode = process.env.NODE_ENV || 'development';
const envFile = path.join(__dirname, `../.env.${mode}`);

require('dotenv').config({ path: envFile });

const { APP_PORT, APP_NAME, AUTHENTICATION_APP, BACKEND_URL } = process.env;

module.exports = () => ({
  entry: './src/main.tsx',

  output: {
    publicPath: 'auto',
    uniqueName: APP_NAME || 'util',
    path: path.resolve(process.cwd(), 'dist'),
    clean: true,
    assetModuleFilename: 'images/[hash][ext][query]',
  },

  devtool: mode === 'production' ? false : 'source-map',

  resolve: {
    extensions: ['.tsx', '.ts', '.jsx', '.js', '.json'],
    alias: {
      '@': path.resolve(__dirname, '../src'),
      '@shared': path.resolve(__dirname, '../src/shared'),
      '@features': path.resolve(__dirname, '../src/features'),
      '@assets': path.resolve(__dirname, '../src/assets'),
    },
  },

  devServer: {
    port: APP_PORT || 3701,
    historyApiFallback: true,
    hot: false,
    liveReload: true,
    static: {
      directory: path.resolve(__dirname, '../public'),
    },
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, PATCH, OPTIONS',
      'Access-Control-Allow-Headers':
        'X-Requested-With, content-type, Authorization',
    },
    proxy: [
      {
        context: ['/api'],
        target: BACKEND_URL || 'http://localhost:3712',
        changeOrigin: true,
        secure: false,
      },
    ],
  },

  module: {
    rules: [
      {
        test: /\.m?js$/,
        type: 'javascript/auto',
        resolve: { fullySpecified: false },
      },
      {
        test: /\.(xlsx|xlsm?)$/,
        type: 'asset/resource',
        generator: { filename: 'static/templates/[name][ext]' },
      },
      {
        test: /\.(css|s[ac]ss)$/i,
        use: ['style-loader', 'css-loader', 'postcss-loader'],
      },
      {
        test: /\.(ts|tsx|js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            ...babelConfig,
            cacheDirectory: true,
          },
        },
      },
      {
        test: /\.(png|jp(e*)g|svg|gif)$/i,
        type: 'asset/resource',
      },
      {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
        generator: { filename: 'fonts/[hash][ext][query]' },
      },
    ],
  },

  plugins: [
    new Dotenv({ path: envFile }),

    new ModuleFederationPlugin({
      name: APP_NAME || 'util',
      filename: 'remoteEntry.js',
      remotes: {
        authentication:
          AUTHENTICATION_APP ||
          'authentication@http://localhost:3001/remoteEntry.js',
      },
      exposes: {
        './App': './src/App.tsx',
        './Card': './src/Card.tsx',
      },
      shared: {
        react: {
          singleton: true,
          eager: true,
          requiredVersion: deps['react'],
        },
        'react-dom': {
          singleton: true,
          eager: true,
          requiredVersion: deps['react-dom'],
        },
        'single-spa': {
          singleton: true,
          requiredVersion: deps['single-spa'],
        },
        'single-spa-react': {
          singleton: true,
          requiredVersion: deps['single-spa-react'],
        },
      },
    }),

    new CopyWebpackPlugin({
      patterns: [
        {
          from: path.resolve(__dirname, '../public'),
          to: '.',
          noErrorOnMissing: true,
          globOptions: {
            ignore: ['**/index.html'],
          },
        },
      ],
    }),

    new HtmlWebPackPlugin({ template: './src/index.html' }),
  ],
});