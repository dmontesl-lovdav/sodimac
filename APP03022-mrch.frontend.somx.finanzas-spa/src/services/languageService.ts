import { APP_NAME, CONFIGURATION_STORE_NAME } from "../constants/environment";
import type { i18n } from "i18next";
import { globalHomeStore } from "../store/globalStore";
import type { Configuration } from "../types/configuration";

const handleLanguageChange = (i18nInstance: i18n) => {
    globalHomeStore.SubscribeToPartnerState(
        APP_NAME,
        CONFIGURATION_STORE_NAME,
        (configuration: Configuration) => {
            if (configuration.language !== i18nInstance.language) {
                return i18nInstance.changeLanguage(configuration.language);
            }
        }
    );
};

export { handleLanguageChange };