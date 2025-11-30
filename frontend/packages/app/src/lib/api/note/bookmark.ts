import Https from "../../utils/https.ts";
import {Note} from "aster-common";

export default async function (id: string) {
    return await Https.post("/api/note/" + id + "/bookmark", true) as Note | undefined;
}
