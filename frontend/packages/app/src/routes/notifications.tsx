import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconBell} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import localstore from "../lib/utils/localstore.ts";
import {useQuery} from "@tanstack/react-query";
import Loading from "../lib/components/Loading.tsx";
import Error from "../lib/components/Error.tsx";
import Timeline from "../lib/components/Timeline.tsx";
import Notification from "../lib/components/Notification.tsx";
import {Api} from 'aster-common'

export const Route = createFileRoute('/notifications')({
    component: RouteComponent,
})

function RouteComponent() {
    const token = localstore.getParsed('token');

    if (token) {
        const {isPending, error, data, isFetching, refetch} = useQuery({
            queryKey: ['Notifications'],
            queryFn: async () => await Api.getNotifications(),
        })

        return <>
            <PageHeader title="Notifications" icon={<IconBell size={18}/>}>
                {/*
            <Tab onClick={() => {
            }} selected={true}
                 aria-label={"All notifications"}>
                <IconBell size={18}/> All
            </Tab>
            <Tab onClick={() => {
            }} selected={false}
                 aria-label={"Mentions only"}>
                <IconAt size={18}/> Mentions
            </Tab>
            <Tab onClick={() => {
            }} selected={false}
                 aria-label={"Mentions only"}>
                <IconMail size={18}/> Direct
            </Tab>
                */}
            </PageHeader>
            <PageWrapper padding={"timeline"} center={false}>
                {isPending || isFetching ? (
                    <Loading fill/>
                ) : error ? (
                    <Error error={error} retry={refetch}/>
                ) : (
                    <Timeline data={data} Component={Notification}/>
                )}
            </PageWrapper>
        </>
    }
}
