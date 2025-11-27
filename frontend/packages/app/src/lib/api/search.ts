import Https from "../utils/https.ts";

export default async function (query: string) {
    return await Https.get("/api/search?q=" + query, true);
}
