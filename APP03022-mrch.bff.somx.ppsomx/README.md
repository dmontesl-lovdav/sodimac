# Proxy service for Aclaraciones RESTAPI
Please note this proxy can be used as a generic proxy service. No more class nor type declaration needed. Even there is no need to add routes.
This proxy works as a transparent proxy, BUT it checks if every new connection has the `Authorization: Bearer...` header against KeyCloack public signature.

## Usage
### Installing:
```
npm i
```

### Configuring:
Write (`.env` file) or declare the following variables:
```
REMOTE_URL=https://service.remote:8443/awesome_service
LOCAL_PORT=8888
LOCAL_CONTEXT=/awesome_proxy
HEALTH_PATH=/some_health_path
AUTH_PUBLIC_KEY=MIIClzCCAX8CBgF...0KYKBj1iX92F87k01ytA==
```
Where:
- `REMOTE_URL` is the actual REST API service
- `LOCAL_PORT` is the local port where this proxy should listen connections.
- `LOCAL_CONTEXT` is an optional parameter to add a new context for this proxy.
- `HEALTH_PATH` sets the health path for this service. Default value is `/health` if no setted.
- `AUTH_PUBLIC_KEY` is the public RSA X509 KeyCloack certificate in PEM or DER format. If PEM is used, please remove both header and footer delimiters.

### Execution:
```
npm start
```
