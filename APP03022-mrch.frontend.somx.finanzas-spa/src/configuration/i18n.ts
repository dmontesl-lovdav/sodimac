import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import { TRANSLATIONS_CN } from "../translations/cn/translations";
import { TRANSLATIONS_EN } from "../translations/en/translations";
import { TRANSLATIONS_ES } from "../translations/es/translations";

import { globalHomeStore } from "../store/globalStore";
import { handleLanguageChange } from "../services/languageService";
import { APP_DEV } from "../constants/environment";

const currentGlobalLanguage: string =
    globalHomeStore.GetGlobalState().configuration?.language ?? "ES";

i18n
    .use(initReactI18next)
    .init({
        resources: {
            EN: { translation: TRANSLATIONS_EN },
            ES: { translation: TRANSLATIONS_ES },
            CN: { translation: TRANSLATIONS_CN },
        },
        lng: currentGlobalLanguage,
        fallbackLng: "ES",
        debug: APP_DEV,
        react: { useSuspense: false },
        interpolation: {
            escapeValue: false,
        },
    })
    .then();

handleLanguageChange(i18n);

export default i18n;