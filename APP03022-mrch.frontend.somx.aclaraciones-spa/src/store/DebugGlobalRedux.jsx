import { useEffect, useRef } from "react";
import { globalHomeStore } from "@/store/globalStore";

export default function DebugGlobalRedux() {
    const lastCountry = useRef(null);

    useEffect(() => {
        if (!globalHomeStore?.SubscribeToGlobalState) return;

        const unsubscribe = globalHomeStore.SubscribeToGlobalState(
            "aclaraciones",
            ({ configuration }) => {
                const country = configuration?.selectedTenant?.country?.name;

                if (!country) return;

                if (lastCountry.current && lastCountry.current !== country) {
                    console.log("🌎 País cambió:", country);

                    // 👇 emitir evento global para que las pantallas recarguen su data
                    window.dispatchEvent(new CustomEvent("country-changed", {
                        detail: { country }
                    }));
                }

                lastCountry.current = country;
            }
        );

        return () => unsubscribe?.();
    }, []);

    return null;
}
