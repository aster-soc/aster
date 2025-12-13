import {createFileRoute} from "@tanstack/react-router";
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconFolder, IconPlus} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import {useInfiniteQuery} from "@tanstack/react-query";
import localstore from "../lib/utils/localstore.ts";
import DriveFile from "../lib/components/DriveFile.tsx";
import Loading from "../lib/components/Loading.tsx";
import Error from "../lib/components/Error.tsx";
import Button from "../lib/components/Button.tsx";
import type {ChangeEvent} from "react";
import {Api} from 'aster-common'
import Timeline from "../lib/components/Timeline.tsx";

export const Route = createFileRoute('/drive')({
    component: RouteComponent,
})

function RouteComponent() {
    const query = useInfiniteQuery({
        queryKey: [`drive_${localstore.getSelf()?.id}`],
        queryFn: ({pageParam}) => Api.getDrive(pageParam),
        initialPageParam: undefined,
        getNextPageParam: (lastPage) => {
            return lastPage ? lastPage?.at(-1)?.createdAt : undefined
        }
    });

    function upload(e: ChangeEvent<HTMLInputElement>) {
        console.log(e.target.files)

        let data = new FormData()

        for (let filesKey of Array.from(e.target.files)) {
            data.append("files", filesKey)
        }

        Api.upload(data)
    }

    return (
        <>
            <PageHeader
                icon={<IconFolder size={18}/>}
                title={"Drive"}
            >
                <Button nav onClick={() =>
                    document.getElementById("upload-input")?.click()
                }>
                    <IconPlus size={18}/>
                </Button>
                <input
                    type={"file"}
                    id={"upload-input"}
                    multiple hidden
                    onChange={(e) => upload(e)}
                />
            </PageHeader>
            <PageWrapper padding={"full"} center={false}>
                {query.isPending ? (
                    <Loading fill/>
                ) : query.error ? (
                    <Error error={query.error} retry={query.refetch}/>
                ) : (
                    <Timeline query={query} Component={DriveFile} grid/>
                )}
            </PageWrapper>
        </>
    )
}
