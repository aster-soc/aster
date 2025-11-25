import Https from "../../utils/https.ts";
import {User} from "aster-common";

export default async (id: string) => {
    return await Https.get(`/api/user/${id}`, true) as User | undefined;
}
