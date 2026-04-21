const HtmlWebPackPlugin = require('html-webpack-plugin');
const { ModuleFederationPlugin } = require('webpack').container;
const Dotenv = require('dotenv-webpack');
const path = require('path');
const http = require('http');
const deps = require('../package.json').dependencies;
const CopyWebpackPlugin = require('copy-webpack-plugin');

const mode = process.env.NODE_ENV || 'development';
const envFile = path.join(__dirname, `../.env.${mode}`);

require('dotenv').config({ path: envFile });

const { APP_PORT, APP_URL, APP_NAME, AUTHENTICATION_APP } = process.env;

module.exports = () => ({
  output: {
    publicPath: 'auto',
    uniqueName: APP_NAME || 'finanzas',
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
      '@configuration': path.resolve(__dirname, '../src/configuration'),
      '@domain': path.resolve(__dirname, '../src/domain'),
      '@models': path.resolve(__dirname, '../src/models'),
      '@security': path.resolve(__dirname, '../src/security'),
      '@store': path.resolve(__dirname, '../src/store'),
      '@types': path.resolve(__dirname, '../src/types'),
    },
  },

  devServer: {
    port: APP_PORT || 3702,
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
    setupMiddlewares: (middlewares, devServer) => {
      devServer.app.post('/api/finanzas-payment', (req, res) => {
        let body = '';
        req.on('data', (chunk) => {
          body += chunk;
        });

        req.on('end', () => {
          const options = {
            hostname: 'localhost',
            port: 8091,
            path: '/api/finanzas-payment',
            method: 'GET',
            headers: {
              'Content-Type': 'application/json',
              'Content-Length': Buffer.byteLength(body),
            },
          };

          const backendReq = http.request(options, (backendRes) => {
            res.status(backendRes.statusCode);
            Object.entries(backendRes.headers).forEach(([key, val]) => {
              if (val) res.setHeader(key, val);
            });
            backendRes.pipe(res);
          });

          backendReq.on('error', (err) => {
            console.error('[finanzas-proxy] Backend error:', err.message);
            res.status(502).json({ error: 'Backend finanzas no disponible: ' + err.message });
          });

          backendReq.write(body);
          backendReq.end();
        });
      });

      return middlewares;
    },
    proxy: [
      {
        context: ['/api'],
        target: 'http://localhost:8091',
        changeOrigin: true,
        secure: false,
      },
      {
        context: ['/suppliers', '/catalogos', '/centers', '/status-train', '/supplier-blocks'],
        target: 'http://localhost:8083',
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
        use: { loader: 'babel-loader' },
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
      name: APP_NAME || 'finanzas',
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
        react: { singleton: true, eager: true, requiredVersion: deps['react'] },
        'react-dom': { singleton: true, eager: true, requiredVersion: deps['react-dom'] },
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