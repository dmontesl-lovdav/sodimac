import { GlobalStore, IGlobalStore } from "redux-micro-frontend";
import { APP_DEV } from "../shared/constant/environment";

export const globalHomeStore: IGlobalStore = GlobalStore.Get(APP_DEV);

export interface AuthToken {
    token?: string;
    tokenDecoded?: any;
    [key: string]: any;
}

export interface Configuration {
    [key: string]: any;
}

export interface GlobalStoreState {
    authentication: AuthToken;
    configuration: Configuration;
}