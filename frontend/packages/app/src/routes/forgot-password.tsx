import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconUserQuestion} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import Input from "../lib/components/Input.tsx";

export const Route = createFileRoute('/forgot-password')({
    component: RouteComponent,
})

function RouteComponent() {
    return (
        <>
            <PageHeader icon={<IconUserQuestion size={18}/>} title="Forgot Password"/>
            <PageWrapper padding={"full"} center>
                <p>
                    Resetting a password requires a code from a moderator
                </p>
                <Input
                    type="text"
                    label="Username"
                />
                <Input
                    type="text"
                    label="Reset code"
                />
            </PageWrapper>
        </>
    )
}
