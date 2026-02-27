import { globalHomeStore, GlobalStoreState } from "../store/globalStore";
import { localHomeStore } from "../store/localStore";
import { logInAction } from "../store/slices/authSlice";
import { configAction } from "../store/slices/configSlice";

const handleSubscribeToGlobalAuthenticationChange = () => {
  return globalHomeStore.SubscribeToGlobalState(
    "aclaraciones",
    ({ authentication }: GlobalStoreState) => {
      if (
        authentication.token !==
        localHomeStore.getState().authentication.token &&
        authentication.token !== undefined &&
        authentication.token?.length > 0
      ) {
        localHomeStore.dispatch(logInAction(authentication));
      }
    }
  );
};

const handleSubscribeToGlobalConfigurationChange = () => {
  return globalHomeStore.SubscribeToGlobalState(
    "aclaraciones",
    ({ configuration }: GlobalStoreState) => {
      if (localHomeStore.getState().configuration) {
        localHomeStore.dispatch(configAction(configuration));
      }
    }
  );
};

export { handleSubscribeToGlobalAuthenticationChange, handleSubscribeToGlobalConfigurationChange };
