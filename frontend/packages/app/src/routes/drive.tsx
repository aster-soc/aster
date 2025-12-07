import {createFileRoute} from "@tanstack/react-router";
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconFolder} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import {useQuery} from "@tanstack/react-query";
import localstore from "../lib/utils/localstore.ts";
import DriveFile from "../lib/components/DriveFile.tsx";
import Container from "../lib/components/Container.tsx";
import Loading from "../lib/components/Loading.tsx";
import Error from "../lib/components/Error.tsx";
import {Api} from 'aster-common'

export const Route = createFileRoute('/drive')({
    component: RouteComponent,
})

function RouteComponent() {
    const {data, error, isPending, isFetching, refetch} = useQuery({
        queryKey: [`drive_${localstore.getSelf()?.id}`],
        queryFn: () => Api.getDrive(),
    });

    return (
        <>
            <PageHeader
                icon={<IconFolder size={18}/>}
                title={"Drive"}
            />
            <PageWrapper padding={"full"} center={false}>
                {isPending || isFetching ? (
                    <Loading fill/>
                ) : error ? (
                    <Error error={error} retry={refetch}/>
                ) : (
                    <Container gap={"md"} align={"horizontal"}>
                        {data?.map((file) => (
                            <DriveFile data={file}/>
                        ))}
                    </Container>
                )}
            </PageWrapper>
        </>
    )
}
