const APP_NAME = process.env.APP_HOME ?? "layout";
const STORE_DEBUG = process.env.STORE_DEBUG === "true";
const APP_DEV: boolean = process.env.APP_DEV === "true";
const CONFIGURATION_STORE_NAME =
    process.env.CONFIGURATION_STORE_NAME ?? "configuration";

export { APP_NAME, STORE_DEBUG, APP_DEV, CONFIGURATION_STORE_NAME };