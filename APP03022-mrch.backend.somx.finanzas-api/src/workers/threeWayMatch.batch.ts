// src/workers/threeWayMatch.batch.ts
import { initDataSource } from "@/config/typeorm-datasource.js";
import { runThreeWayMatch } from "@/services/threeWayMatch.service.js";

export async function execute() {
    await initDataSource(); // ← OBLIGATORIO

    const fechaBase = new Date();
    fechaBase.setDate(fechaBase.getDate() - 1);

    await runThreeWayMatch(fechaBase, 1);
}

if (process.argv.includes("--run")) {
    execute()
        .then(() => {
            console.log("ThreeWayMatch batch OK");
            process.exit(0);
        })
        .catch((e) => {
            console.error("ThreeWayMatch batch ERROR", e);
            process.exit(1);
        });
}
