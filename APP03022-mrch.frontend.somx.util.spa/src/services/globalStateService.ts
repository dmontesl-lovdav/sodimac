// src/services/globalStateService.ts
import { globalHomeStore, GlobalStoreState } from "../store/globalStore";
import { localHomeStore } from "../store/localStore";
import { logInAction } from "../store/slices/authSlice";
import { configAction } from "../store/slices/configSlice";

const APP_NAME = "util";

const hydrateAuthentication = (authentication?: GlobalStoreState["authentication"]) => {
    console.log("🌍 GLOBAL AUTH:", authentication);
    console.log("🏠 LOCAL AUTH BEFORE:", localHomeStore.getState().authentication);

    const globalToken = authentication?.token;
    const localToken = localHomeStore.getState().authentication?.token;

    if (globalToken && globalToken !== localToken) {
        localHomeStore.dispatch(logInAction(authentication));

        console.log("✅ LOCAL AUTH AFTER:", localHomeStore.getState().authentication);
    }
};

const hydrateConfiguration = (configuration?: GlobalStoreState["configuration"]) => {
    console.log("🌍 GLOBAL CONFIG:", configuration);
    console.log("🏠 LOCAL CONFIG BEFORE:", localHomeStore.getState().configuration);

    if (configuration) {
        localHomeStore.dispatch(configAction(configuration));

        console.log("✅ LOCAL CONFIG AFTER:", localHomeStore.getState().configuration);
    }
};

const hydrateInitialGlobalState = () => {
    try {
        const globalState = (globalHomeStore as any).GetGlobalState?.() as
            | GlobalStoreState
            | undefined;

        console.log("🚀 INITIAL GLOBAL STATE:", globalState);

        hydrateAuthentication(globalState?.authentication);
        hydrateConfiguration(globalState?.configuration);
    } catch (error) {
        console.warn("⚠️ Could not hydrate initial global state:", error);
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