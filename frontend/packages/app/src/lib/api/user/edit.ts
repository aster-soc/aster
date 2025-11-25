import Https from "../../utils/https.ts";
import {User} from "aster-common";

export default async (id: string, values: any) => {
    return await Https.post(`/api/user/${id}`, true, values) as User | undefined;
}
