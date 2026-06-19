import { createBreaker } from "./circuitBreaker.service.js";
import { getCache } from "@/services/cache.service.js"
import { GenericCatalogDetails } from '@/response/GenericCatalogDetails.dto.js';

const breakers = new Map<string, any>();

export function getBreaker(key: string, fn: (...args: any[]) => Promise<any>, type: string) {

  if (!breakers.has(key)) {
    console.log(`🆕 creando breaker para → ${key}`);

    const breaker = createBreaker(fn);
  
    // ✅ fallback dinámico
    breaker.fallback(getFallbackByType(type));

    // logs por endpoint 🔥
    breaker.on("open", () => console.warn(`🔴 OPEN → ${key}`));
    breaker.on("halfOpen", () => console.warn(`🟡 HALF → ${key}`));
    breaker.on("close", () => console.warn(`🟢 CLOSED → ${key}`));
    breaker.on("reject", () => console.warn(`⛔ REJECT → ${key}`));

    breakers.set(key, breaker);
  }

  return breakers.get(key);
}


function getFallbackByType(type: string) {
  return (url: string) => {

    console.warn(`⚠️ fallback ejecutado → ${type}`);
    const typesWithErrorFallback = new Set(["catalog", "supplier", "status"]);
    const cached = getCache(url);

    if (typesWithErrorFallback.has(type)) {
      if (cached) {
        console.warn("✅ usando cache");
        return cached;
      }
      return buildDefaultCatalog("error", url);
    }

    return buildDefaultCatalog("empty", url);

  };
}

function buildDefaultCatalog(type: "error" | "empty", url?: string): GenericCatalogDetails {
    return {
        key: type,
        description: `Sin información (${url ?? "unknown"})`,
        value: '',
        color: '',
        externalKey: '',
        internalStatus: 0,
        success : false,
    };
}