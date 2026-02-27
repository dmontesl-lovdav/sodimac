import {
  AuthToken,
  TokenParsed,
} from "@rtl/mrch.frontend.cross.common-interfaces";

export const AuthTokenStateDefault: AuthToken = {
  idToken: undefined,
  refreshToken: undefined,
  token: undefined,
  isLogged: false,
  tokenDecoded: {} as TokenParsed,
};

export const AuthTokenStateMockDefault: AuthToken = {
  idToken:
    "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvWlhST0Q1dU9ldFp3TUJWSHo0ODd3ZnJEdy1wT3ZENUVzdjgxU2NYYWNnIn0.eyJleHAiOjE2NTI4MjIxOTQsImlhdCI6MTY1MjgyMTg5NCwiYXV0aF90aW1lIjoxNjUyODE4NTcxLCJqdGkiOiJmYWY2YjliNS1kMTQ4LTRhMjctYmRhMy1mMzk3YzkzYzU3ZWIiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvZm91bmRhdGlvbmFsIiwiYXVkIjoicG9ydGFsIiwic3ViIjoiOTBlZjczYWUtNzk5Ni00NzE0LTlmZTAtZTcyYTI3YzY0ZWMyIiwidHlwIjoiSUQiLCJhenAiOiJwb3J0YWwiLCJub25jZSI6ImMzMzNkYjA1LWJkZjItNDU3Ni04MWI5LWU0OTdjMDFmNDAzOCIsInNlc3Npb25fc3RhdGUiOiI2YjhiMDFlYi0wMWI4LTQ3N2UtYmFhMi04OGJhMTcwMDQ1MTIiLCJhdF9oYXNoIjoieHZNUHd1STJxZjcxRUZqbkFOcHFudyIsImFjciI6IjAiLCJzaWQiOiI2YjhiMDFlYi0wMWI4LTQ3N2UtYmFhMi04OGJhMTcwMDQ1MTIiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImZvcmVpZ25fdHJhZGUiLCJwdXJjaGFzZV9vcmRlcnMiLCJvZmZsaW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtZm91bmRhdGlvbmFsLWRldiIsInVtYV9hdXRob3JpemF0aW9uIiwicHJvZHVjdF9jYXRhbG9nIiwicHJvdmlkZXJzIl19LCJuYW1lIjoiQ2FybG9zIFJvamFzIiwiZ3JvdXBzIjpbXSwicHJlZmVycmVkX3VzZXJuYW1lIjoiY2pyb2phc2IiLCJnaXZlbl9uYW1lIjoiQ2FybG9zIiwiZmFtaWx5X25hbWUiOiJSb2phcyIsImVtYWlsIjoiY2pyb2phc2JAZmFsYWJlbGxhLmNsIn0.HwT-iWv_k-UTLAXxXSRVrXy8h014aeQU2a9MplcMGSjQJX4OBodGODdO8kEbvapqekSezXn6o7kI7edaFwOog67V8fTxcu2Ap5FmZfWeyY72jaJBbY8oxxJXIkBzaCp1Ntex6_0cqGhmBPFa5JxsCfPKVvEi66MX4gK4_wbwvVduz9X7gHXz-kl0LsgSbGp-li_81GZPoCF28d2vCnIs45AfGd8XewpVtqTUN6ojIExRr31djNjBlRWtsvHl2wTksqwOrIJuZyC9hX-0077DerZ1yYB_nwCJN0_5vwOvjWtApiASe-xJJLVTkWGbaK5_cQAiS5aSAGdngGjj1by-uA",
  refreshToken:
    "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIzZmU4MzU3NS1jN2NhLTQzOTEtOGQ4Mi1jZDExNWJjYzZkNzMifQ.eyJleHAiOjE2NTI4MjM2OTQsImlhdCI6MTY1MjgyMTg5NCwianRpIjoiNDU0YjNhN2YtYzY2ZC00NTIwLWE3Y2YtOWMyMjJlZGMxOTE2IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL2F1dGgvcmVhbG1zL2ZvdW5kYXRpb25hbCIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9hdXRoL3JlYWxtcy9mb3VuZGF0aW9uYWwiLCJzdWIiOiI5MGVmNzNhZS03OTk2LTQ3MTQtOWZlMC1lNzJhMjdjNjRlYzIiLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoicG9ydGFsIiwibm9uY2UiOiJjMzMzZGIwNS1iZGYyLTQ1NzYtODFiOS1lNDk3YzAxZjQwMzgiLCJzZXNzaW9uX3N0YXRlIjoiNmI4YjAxZWItMDFiOC00NzdlLWJhYTItODhiYTE3MDA0NTEyIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsInNpZCI6IjZiOGIwMWViLTAxYjgtNDc3ZS1iYWEyLTg4YmExNzAwNDUxMiJ9.cz14qkYKnGkFevQj9aT24a6DIoyU2_RaoBVm5UCyw6M",
  token:
    "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvWlhST0Q1dU9ldFp3TUJWSHo0ODd3ZnJEdy1wT3ZENUVzdjgxU2NYYWNnIn0.eyJleHAiOjE2NTI4MjIxOTQsImlhdCI6MTY1MjgyMTg5NCwiYXV0aF90aW1lIjoxNjUyODE4NTcxLCJqdGkiOiJhZDEzYjNmNi1hNjhlLTQ2ZTktOThmZS1hZWI1OTYyNmUyOGUiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvZm91bmRhdGlvbmFsIiwic3ViIjoiOTBlZjczYWUtNzk5Ni00NzE0LTlmZTAtZTcyYTI3YzY0ZWMyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicG9ydGFsIiwibm9uY2UiOiJjMzMzZGIwNS1iZGYyLTQ1NzYtODFiOS1lNDk3YzAxZjQwMzgiLCJzZXNzaW9uX3N0YXRlIjoiNmI4YjAxZWItMDFiOC00NzdlLWJhYTItODhiYTE3MDA0NTEyIiwiYWNyIjoiMCIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Nyb3NzLWFwcC1zaGVsbC4zNS4yNDQuMTc3LjEyNi5uaXAuaW8iLCJodHRwOi8vbG9jYWxob3N0OjMwMDAiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImZvcmVpZ25fdHJhZGUiLCJwdXJjaGFzZV9vcmRlcnMiLCJvZmZsaW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtZm91bmRhdGlvbmFsLWRldiIsInVtYV9hdXRob3JpemF0aW9uIiwicHJvZHVjdF9jYXRhbG9nIiwicHJvdmlkZXJzIl19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwic2lkIjoiNmI4YjAxZWItMDFiOC00NzdlLWJhYTItODhiYTE3MDA0NTEyIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJDYXJsb3MgUm9qYXMiLCJncm91cHMiOltdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJjanJvamFzYiIsImdpdmVuX25hbWUiOiJDYXJsb3MiLCJmYW1pbHlfbmFtZSI6IlJvamFzIiwiZW1haWwiOiJjanJvamFzYkBmYWxhYmVsbGEuY2wifQ.Kwofka8yKuKzb0OTqni0YpQFZfmos-RmCDVNAMk2dYHkFSLsaw3Qwc6BwP57NV40dKK7CMs4xqBJsm4JA70cnV_k9Van1fqXszesgCcXoM2DZ4qZDK4MIK_O0VRRsgOzye0jxG2DfiODISLeQegLDGCWDaFELDG-Usjvm9bbRhj88gx8qaJHv9tfbUDmXm-e2WHCQlzjcCohuigjsxfanCTG61fqUNvUAXIUVVcvKnRe0x5oqajrS6yPCvNgs0PC5PPFIDM7PxN-x_WjlY1E5CwlbIcQ2Xm02x3Xvqa4QZNh5P-8WIakXnXAOCjSEBPAfPpVzO0NHGbwzs6X8TxLHw",
  isLogged: true,
  tokenDecoded: {
    alg: "none",
    auth_time: 1652818571,
    azp: "portal",
    nonce: "c333db05-bdf2-4576-81b9-e497c01f4038",
    session_state: "6b8b01eb-01b8-477e-baa2-88ba17004512",
    acr: "0",
    "allowed-origins": [
      "https://cross-app-shell.35.244.177.126.nip.io",
      "http://localhost:3000",
    ],
    realm_access: {
      roles: [
        "foreign_trade",
        "purchase_orders",
        "offline_access",
        "default-roles-foundational-dev",
        "uma_authorization",
        "product_catalog",
        "providers",
        "invitation",
      ],
    },
    resource_access: {
      account: {
        roles: [
          "foreign_trade",
          "purchase_orders",
          "offline_access",
          "default-roles-foundational-dev",
          "uma_authorization",
          "product_catalog",
          "providers",
          "invitation",
        ],
      },
    },
    scope: "openid profile email",
    sid: "6b8b01eb-01b8-477e-baa2-88ba17004512",
    email_verified: true,
    name: "Carlos Rojas",
    preferred_username: "cjrojasb",
    given_name: "Carlos",
    locale: "ES",
    family_name: "Rojas",
    email: "cjrojasb@falabella.cl",
  },
};
