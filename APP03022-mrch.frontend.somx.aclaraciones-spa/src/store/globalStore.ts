import {
  AuthToken,
  Configuration,
} from '@rtl/mrch.frontend.cross.common-interfaces';
import { GlobalStore, IGlobalStore } from 'redux-micro-frontend';
import { APP_DEV } from '../shared/constants/environment';

export const globalHomeStore: IGlobalStore = GlobalStore.Get(APP_DEV);

export interface GlobalStoreState {
  authentication: AuthToken;
  configuration: Configuration;
}
