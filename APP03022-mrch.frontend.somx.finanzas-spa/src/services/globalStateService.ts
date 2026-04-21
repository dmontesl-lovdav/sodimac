// src/services/globalStateService.ts
import { globalHomeStore, GlobalStoreState } from "../store/globalStore";
import { localHomeStore } from "../store/localStore";
import { logInAction } from "../store/slices/authSlice";
import { configAction } from "../store/slices/configSlice";

const handleSubscribeToGlobalAuthenticationChange = () => {
    return globalHomeStore.SubscribeToGlobalState(
        "finanzas",
        ({ authentication }: GlobalStoreState) => {
            console.log("🌍 GLOBAL AUTH:", authentication);
            console.log("🏠 LOCAL AUTH BEFORE:", localHomeStore.getState().authentication);

            if (
                authentication.token !== localHomeStore.getState().authentication.token &&
                authentication.token !== undefined &&
                authentication.token?.length > 0
            ) {
                localHomeStore.dispatch(logInAction(authentication));
                console.log("✅ LOCAL AUTH AFTER:", localHomeStore.getState().authentication);
            }
        }
    );
};

const handleSubscribeToGlobalConfigurationChange = () => {
    return globalHomeStore.SubscribeToGlobalState(
        "finanzas",
        ({ configuration }: GlobalStoreState) => {
            if (localHomeStore.getState().configuration) {
                localHomeStore.dispatch(configAction(configuration));
            }
        }
    );
};

export {
    handleSubscribeToGlobalAuthenticationChange,
    handleSubscribeToGlobalConfigurationChange,
};