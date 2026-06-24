// src/services/globalStateService.ts
import { globalHomeStore, GlobalStoreState } from "../store/globalStore";
import { localHomeStore } from "../store/localStore";
import { logInAction } from "../store/slices/authSlice";
import { configAction } from "../store/slices/configSlice";

const APP_NAME = "fiscal";

const hydrateAuthentication = (authentication?: GlobalStoreState["authentication"]) => {
    const globalToken = authentication?.token;
    const localToken = localHomeStore.getState().authentication?.token;

    if (globalToken && globalToken !== localToken) {
        localHomeStore.dispatch(logInAction(authentication));
    }
};

const hydrateConfiguration = (configuration?: GlobalStoreState["configuration"]) => {
    if (configuration) {
        localHomeStore.dispatch(configAction(configuration));
    }
};

const hydrateInitialGlobalState = () => {
    try {
        const globalState = (globalHomeStore as any).GetGlobalState?.() as
            | GlobalStoreState
            | undefined;

        hydrateAuthentication(globalState?.authentication);
        hydrateConfiguration(globalState?.configuration);
    } catch (error) {
        console.warn("Could not hydrate initial global state:", error);
    }
};

const handleSubscribeToGlobalAuthenticationChange = () => {
    hydrateInitialGlobalState();

    return globalHomeStore.SubscribeToGlobalState(
        APP_NAME,
        ({ authentication }: GlobalStoreState) => {
            hydrateAuthentication(authentication);
        }
    );
};

const handleSubscribeToGlobalConfigurationChange = () => {
    hydrateInitialGlobalState();

    return globalHomeStore.SubscribeToGlobalState(
        APP_NAME,
        ({ configuration }: GlobalStoreState) => {
            hydrateConfiguration(configuration);
        }
    );
};

export {
    handleSubscribeToGlobalAuthenticationChange,
    handleSubscribeToGlobalConfigurationChange,
};
