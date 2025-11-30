import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconBookmark} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import {useQuery} from "@tanstack/react-query";
import bookmarks from "../lib/api/bookmarks.ts";
import Loading from "../lib/components/Loading.tsx";
import Error from "../lib/components/Error.tsx";
import Timeline from "../lib/components/Timeline.tsx";
import Note from "../lib/components/Note.tsx";

export const Route = createFileRoute('/bookmarks')({
    component: RouteComponent,
})

function RouteComponent() {
    const {isPending, error, data, isFetching, refetch} = useQuery({
        queryKey: ['bookmarks'],
        queryFn: async () => await bookmarks(),
    })

    return (
        <>
            <PageHeader icon={<IconBookmark size={18}/>} title="Bookmarks"/>
            <PageWrapper padding={"timeline"} center={false}>
                {isPending || isFetching ? (
                    <Loading fill/>
                ) : error ? (
                    <Error error={error} retry={refetch}/>
                ) : (
                    <Timeline data={data} Component={Note}/>
                )}
            </PageWrapper>
        </>
    )
}
