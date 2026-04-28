import * as r from "@/repositories/healthcheck.repo.js";

export async function list() {
    return r.findAll({}, 1);
}