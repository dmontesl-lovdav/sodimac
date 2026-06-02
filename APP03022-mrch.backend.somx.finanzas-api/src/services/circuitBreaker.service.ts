import CircuitBreaker from "opossum";

export function createBreaker<T>(fn: (...args: any[]) => Promise<T>) {
  const breaker = new CircuitBreaker(fn, {
    timeout: 8000,
    errorThresholdPercentage: 50,
    resetTimeout: 10000,   
  });

  breaker.on("open", () => console.warn("🔴 Circuit OPEN"));
  breaker.on("halfOpen", () => console.warn("🟡 HALF-OPEN"));
  breaker.on("close", () => console.warn("🟢 CLOSED"));
  breaker.on("reject", () => console.warn("⛔ REJECTED (OPEN)"));

  return breaker;
}
