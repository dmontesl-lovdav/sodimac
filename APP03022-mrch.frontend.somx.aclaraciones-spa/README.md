# Aclaraciones SPA
Front-end Single Page Application for Aclaraciones (AKA Help Center).

## Usage
### Installing:
```
npm i
```

### Configuring:
Write (`.env` file) or declare the following variables:
```
APP_PORT=3701
APP_URL=http://localhost:3701/
APP_NAME=aclaraciones
LOGIN_URL=http://localhost:3000/login
AUTHENTICATION_APP=authentication@http://localhost:3001/remoteEntry.js
STORE_DEBUG=true
AUTH_CONFIG_CLIENT=portal
APP_DEV=true

API_BASE_URL=http://localhost:8888
```
Where:
- `API_BASE_URL` is the actual Aclaraciones REST API service URL.
- `APP_PORT` is the local port where this SPA (via Webpack) should listen connections.
- `APP_URL` is the actual SPA URL (via Webpack).
- `APP_NAME` is the name of this package. Should be the same as in `package.json` name.
- `LOGIN_URL` is the URL of the authentication app name in the SPA parcel.
- `AUTHENTICATION_APP` is the entry file path (`remoteEntry.js`) of the authentication Single SPA parcel.
- `STORE_DEBUG` ???
- `AUTH_CONFIG_CLIENT` ???
- `APP_DEV` ???

### Execution:
```
npm start
```
