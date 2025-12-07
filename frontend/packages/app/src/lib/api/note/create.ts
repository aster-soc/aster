import Https from "../../utils/https.ts";
import {Note} from "aster-common";

export default async function createNote(data: any) {
    return await Https.post("/api/note", true, data) as Note | undefined;
}
