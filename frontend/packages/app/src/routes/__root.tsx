import {createRootRoute, Outlet} from '@tanstack/react-router';
import {QueryClientProvider} from "@tanstack/react-query";
import queryClient from "../lib/queryClient.ts";
import Sidebar from "../lib/components/Sidebar.tsx";
import alert from '../lib/utils/alert.ts'
import {useState} from "react";
import Container from "../lib/components/Container.tsx";
import {IconAlertTriangle, IconCheck, IconInfoCircle, IconX} from "@tabler/icons-react";

export const Route = createRootRoute({
    component: RootComponent,
})

function RootComponent() {
    const [alerts, setAlerts] = useState()

    alert.state.subscribe((e) => setAlerts(e))

    function renderAlerts() {
        return (
            <div className={"alerts"}>
                {alerts ? alerts.map((alert) => (
                    <div className={"alert " + alert.type}>
                        {alert.type === "info" ? (
                            <IconInfoCircle size={18}/>
                        ) : alert.type === "warn" ? (
                            <IconAlertTriangle size={18}/>
                        ) : alert.type === "error" ? (
                            <IconX size={18}/>
                        ) : alert.type === "success" ? (
                            <IconCheck size={18}/>
                        ) : null}
                        <Container align={"left"}>
                            {alert.title === "" ? <b>{alert.title}</b> : null}
                            <span>{alert.message}</span>
                        </Container>
                    </div>
                )) : null}
            </div>
        )
    }

    return (
        <>
            <QueryClientProvider client={queryClient}>
                {renderAlerts()}
                <Sidebar left/>
                <main>
                    <Outlet/>
                </main>
                <Sidebar right/>
            </QueryClientProvider>
        </>
    )
}
