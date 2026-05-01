const LOGIN_URL = process.env.LOGIN_URL || "http://localhost:3000/login";
const APP_NAME = process.env.APP_HOME || "fiscal";
const STORE_DEBUG = process.env.STORE_DEBUG === "true";
const AUTH_CONFIG_CLIENT = process.env.AUTH_CONFIG_CLIENT || "portal";
const APP_DEV: boolean = process.env.APP_DEV === "true";

export { LOGIN_URL, APP_NAME, STORE_DEBUG, AUTH_CONFIG_CLIENT, APP_DEV };