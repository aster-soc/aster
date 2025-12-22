import {createFileRoute} from '@tanstack/react-router'
import {useQuery} from "@tanstack/react-query";
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconInfoCircle} from "@tabler/icons-react";
import PageWrapper from '../lib/components/PageWrapper.tsx';
import {Api} from 'aster-common'

export const Route = createFileRoute('/about')({
    component: RouteComponent,
})

function RouteComponent() {
    const {data} = useQuery({
        queryKey: ['meta'],
        queryFn: () => Api.getMeta(),
    });

    return (
        <>
            <PageHeader
                icon={<IconInfoCircle size={18}/>}
                title={"About"}
            />
            <PageWrapper padding={"full"} center={true}>
                <img alt={"Aster logo"} src={"/uikit/branding/favicon.png"} height={"50px"}/>

                <p className={"centerText"}>
                    Aster {data?.version?.aster}
                </p>

                <p className={"centerText"}>
                    Kotlin {data?.version?.kotlin}<br/>
                    {data?.version?.java}<br/>
                    {data?.version?.system}
                </p>
            </PageWrapper>
        </>
    )
}
