import { useEffect, useRef } from "react";
import { globalHomeStore } from "@/store/globalStore";

export default function DebugGlobalRedux() {
    const lastCountry = useRef(null);
    const lastCommerce = useRef(null);

    useEffect(() => {
        if (!globalHomeStore?.SubscribeToGlobalState) return;

        const unsubscribe = globalHomeStore.SubscribeToGlobalState(
            "aclaraciones",
            ({ configuration }) => {
                console.log("🧩 selectedTenant completo:", configuration?.selectedTenant);

                const country = configuration?.selectedTenant?.country?.name;
                const commerce = configuration?.selectedTenant?.commerce?.name;

                if (!country || !commerce) return;

                if (
                    (lastCountry.current && lastCountry.current !== country) ||
                    (lastCommerce.current && lastCommerce.current !== commerce)
                ) {
                    console.log("🔄 Tenant cambió:", { country, commerce });

                    window.dispatchEvent(
                        new CustomEvent("country-changed", {
                            detail: { country, commerce }
                        })
                    );
                }

                lastCountry.current = country;
                lastCommerce.current = commerce;
            }
        );

        return () => unsubscribe?.();
    }, []);

    return null;
}
