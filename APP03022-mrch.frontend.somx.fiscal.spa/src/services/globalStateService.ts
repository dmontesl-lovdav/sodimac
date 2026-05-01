import { globalHomeStore, GlobalStoreState } from "../store/globalStore";
import { localHomeStore } from "../store/localStore";
import { logInAction } from "../store/slices/authSlice";
import { configAction } from "../store/slices/configSlice";

const handleSubscribeToGlobalAuthenticationChange = () => {
    return globalHomeStore.SubscribeToGlobalState(
        "fiscal",
        ({ authentication }: GlobalStoreState) => {
            if (
                authentication?.token !== localHomeStore.getState().authentication?.token &&
                authentication?.token !== undefined &&
                authentication?.token?.length > 0
            ) {
                localHomeStore.dispatch(logInAction(authentication));
            }
        }
    );
};

const handleSubscribeToGlobalConfigurationChange = () => {
    return globalHomeStore.SubscribeToGlobalState(
        "fiscal",
        ({ configuration }: GlobalStoreState) => {
            if (configuration) {
                localHomeStore.dispatch(configAction(configuration));
            }
        }
    );
};

export {
    handleSubscribeToGlobalAuthenticationChange,
    handleSubscribeToGlobalConfigurationChange,
};