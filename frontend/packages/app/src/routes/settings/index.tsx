import {createFileRoute, useNavigate} from '@tanstack/react-router'
import PageHeader from "../../lib/components/PageHeader.tsx";
import PageWrapper from "../../lib/components/PageWrapper.tsx";
import {IconDeviceDesktop, IconSettings, IconUser} from "@tabler/icons-react";
import Tab from "../../lib/components/Tab.tsx";
import Container from "../../lib/components/Container.tsx";
import {useState} from "react";
import Input from "../../lib/components/Input.tsx";

export const Route = createFileRoute('/settings/')({
    component: RouteComponent,
})

function RouteComponent() {
    const navigate = useNavigate()

    const [tab, setTab] = useState(0)

    function renderTab() {
        switch (tab) {
            case 0:
                return (
                    <>
                        <Input type={"checkbox"} label={"Hide repeat count"} setting={"hide_repeat_count"}/>
                        <Input type={"checkbox"} label={"Hide like count"} setting={"hide_like_count"}/>
                    </>
                )
            case 1:
                return (
                    <>
                        <Input type={"checkbox"} label={"Show rounded avatars"} setting={"rounded_avatars"}/>
                        <Input type={"checkbox"} label={"Always show content warning input in compose box"}
                               setting={"always_show_cw_compose"}/>
                    </>
                )
            case 2:
                return (
                    <>
                    </>
                )
        }
    }

    return (
        <>
            <PageHeader icon={<IconSettings size={18}/>} title={"Settings"}>
                <Tab
                    selected={true}
                >
                    <IconDeviceDesktop size={18}/>
                    Client
                </Tab>
                <Tab
                    selected={false}
                    onClick={() => navigate({to: "/settings/account"})}
                >
                    <IconUser size={18}/>
                    Account
                </Tab>
            </PageHeader>
            <PageWrapper padding={"none"} center={false}>
                <Container align={"horizontal"} padding={"0 12px"} border={"bottom"}>
                    <Tab
                        selected={tab === 0}
                        onClick={() => setTab(0)}
                    >
                        General
                    </Tab>
                    <Tab
                        selected={tab === 1}
                        onClick={() => setTab(1)}
                    >
                        Appearance
                    </Tab>
                    <Tab
                        selected={tab === 2}
                        onClick={() => setTab(2)}
                    >
                        Accessibility
                    </Tab>
                </Container>
                <Container gap={"md"} padding={"12px"}>
                    {renderTab()}
                </Container>
            </PageWrapper>
        </>
    )
}
