# Proxy service for Finanzas RESTAPI

This proxy works as a transparent proxy for the Finanzas backend service.

It validates that each incoming request contains an `Authorization: Bearer ...` header and checks that token against the Keycloak public signature before forwarding the request.

## Requirements

- Node.js
- npm

## Installation

```bash
npm install
```

## Environment configuration

Create a `.env` file in the project root with:

```env
REMOTE_URL=https://service.remote:8443/awesome_service
LOCAL_PORT=8888
LOCAL_CONTEXT=/awesome_proxy
HEALTH_PATH=/some_health_path
AUTH_PUBLIC_KEY=MIIClzCCAX8CBgF...0KYKBj1iX92F87k01ytA==
DOMAIN_OPENAPI=your-endpoint-domain
KEYCLOAK=https://your-keycloak-domain
JWKS_URL=https://your-keycloak-domain/auth/realms/corp/protocol/openid-connect/certs
```

### Variables

- `REMOTE_URL`: Real Finanzas backend service URL.
- `LOCAL_PORT`: Local port where this proxy listens.
- `LOCAL_CONTEXT`: Optional context path for the proxy.
- `HEALTH_PATH`: Health endpoint path. Default is `/health` if not provided.
- `AUTH_PUBLIC_KEY`: Public RSA X509 Keycloak certificate in PEM or DER format. If PEM is used, remove the header and footer delimiters.
- `DOMAIN_OPENAPI`: Domain used by the OpenAPI / Google Endpoints spec.
- `KEYCLOAK`: Base Keycloak URL used in the OpenAPI security definition.
- `JWKS_URL`: JWKS endpoint used in the OpenAPI security definition.

## Run the proxy

```bash
npm start
```

## Generate the final OpenAPI file

The OpenAPI definition is maintained in modular files under:

```text
cloud-endpoint/src/
```

To generate the final bundled file, run:

```bash
npm run openapi:bundle
```

This command bundles all modular files from:

```text
cloud-endpoint/src/root.yaml
```

into:

```text
cloud-endpoint/openapi.yaml
```

Internally it runs:

```bash
npx swagger-cli bundle cloud-endpoint/src/root.yaml --outfile cloud-endpoint/openapi.yaml --type yaml
```

## Deploy to Google Endpoints

```bash
gcloud endpoints services deploy cloud-endpoint/openapi.yaml
```
