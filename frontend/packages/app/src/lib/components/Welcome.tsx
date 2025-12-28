import "./Welcome.scss";
import AccountWidget from "./widgets/Account.tsx";
import {useQuery} from "@tanstack/react-query";
import {Api} from "aster-common";
import Loading from "./Loading.tsx";
import Error from "./Error.tsx";
import UserCard from "./UserCard.tsx";

function Welcome() {
    const {isLoading, isError, error, data} = useQuery({
        queryKey: ['meta'],
        queryFn: () => Api.getMeta(),
    });

    function renderStaff() {
        if (data?.staff?.admin.length > 1) return null;

        return (
            <div className={"staff"}>
                <b>Administrated by</b>
                {data?.staff?.admin?.map((item) => (
                    <UserCard data={item} />
                ))}
            </div>
        )
    }

    return (
        <div className={"welcome"}>
            {isLoading ? (
                <Loading />
            ) : isError ? (<Error error={error} />) : <>
                <img alt={"Aster logo"} src={"/uikit/branding/favicon.png"} height={"50px"}/>
                <h1>{data?.name ?? "Aster"}</h1>
                <p>Running Aster {data?.version?.aster ?? "Unknown"}</p>
                {renderStaff()}
            </>}

            <AccountWidget />
        </div>
    )
}

export default Welcome
