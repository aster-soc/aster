import Https from "../utils/https.ts";

export default async function (code: string, password: string) {
    return await Https.post("/api/password-reset", true, {
        code: code,
        password: password,
    });
}
