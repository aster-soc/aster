import Https from "../utils/https.ts";
import * as Common from "aster-common";

export default async function (code: string, password: string) {
    return await Https.post("/api/password-reset", true, {
        code: code,
        password: password,
    }) as Common.PasswordResetCodeResponse | undefined;
}
