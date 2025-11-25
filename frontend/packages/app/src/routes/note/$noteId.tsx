import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../../lib/components/PageHeader.tsx";
import {IconNote} from "@tabler/icons-react";
import PageWrapper from "../../lib/components/PageWrapper.tsx";
import {useQuery} from '@tanstack/react-query'
import getNote from '../../lib/api/note/get.ts'
import Note from "../../lib/components/Note.tsx";
import Loading from "../../lib/components/Loading.tsx";
import Error from "../../lib/components/Error.tsx";
import Container from "../../lib/components/Container.tsx";
import Tab from "../../lib/components/Tab.tsx";
import {useState} from "react";
import Timeline from "../../lib/components/Timeline.tsx";
import NoteSimple from "../../lib/components/NoteSimple.tsx";
import UserCard from "../../lib/components/UserCard.tsx";

export const Route = createFileRoute('/note/$noteId')({
    component: RouteComponent,
})

function RouteComponent() {
    const {noteId} = Route.useParams()

    const [tab, setTab] = useState(0)

    const {isPending, isFetching, error, data, refetch} = useQuery({
        queryKey: ['note_' + noteId],
        queryFn: () => getNote(noteId)
    });

    console.log(data)

    function renderTab() {
        switch (tab) {
            case 0:
                return (
                    <>
                        <p>No replies</p>
                    </>
                )
            case 1:
                return (
                    <>
                        <Timeline data={data.repeats} Component={NoteSimple}/>
                    </>
                )
            case 2:
                return (
                    <>
                        <Timeline data={data.likes} Component={UserCard}/>
                    </>
                )
        }
    }

    return (
        <>
            <PageHeader
                icon={<IconNote size={18}/>}
                title={`Note ${data ? ("by " + (data?.user?.displayName ?? data?.user?.username)) : ""}`}
            />
            <PageWrapper padding={"none"} center={false}>
                {isPending || isFetching ? (
                    <Loading fill/>
                ) : error ? (
                    <Error error={error} retry={refetch}/>
                ) : (
                    <>
                        <Container padding={"12px 12px 0 12px"}>
                            <Note data={data}/>
                        </Container>
                        <Container gap={"md"} align={"horizontal"} border={"bottom"} padding={"0 12px"}>
                            <Tab
                                selected={tab === 0}
                                onClick={() => setTab(0)}
                            >
                                Replies
                            </Tab>
                            <Tab
                                selected={tab === 1}
                                onClick={() => setTab(1)}
                            >
                                Repeats
                            </Tab>
                            <Tab
                                selected={tab === 2}
                                onClick={() => setTab(2)}
                            >
                                Likes
                            </Tab>
                        </Container>
                        <Container padding={"12px"}>
                            {renderTab()}
                        </Container>
                    </>
                )}
            </PageWrapper>
        </>
    )
}
