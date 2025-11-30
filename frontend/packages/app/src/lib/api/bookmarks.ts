import Https from "../utils/https.ts";
import {Note} from "aster-common";

export default async function () {
    return await Https.get("/api/bookmarks", true) ?? [] as Note[];
}
