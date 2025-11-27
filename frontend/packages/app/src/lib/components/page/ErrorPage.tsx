import PageHeader from "../PageHeader.tsx";
import PageWrapper from "../PageWrapper.tsx";
import Error from "../Error.tsx";
import {IconAlertTriangle} from "@tabler/icons-react";

function ErrorPage({error}: { error: any }) {
    return (
        <>
            <PageHeader icon={<IconAlertTriangle size={18}/>} title={"Something went wrong"}/>
            <PageWrapper padding={"full"} center={true}>
                <Error error={error}/>
            </PageWrapper>
        </>
    )
}

export default ErrorPage
