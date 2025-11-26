import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconUserQuestion} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import Input from "../lib/components/Input.tsx";
import Container from "../lib/components/Container.tsx";
import Button from "../lib/components/Button.tsx";
import {useForm} from "@tanstack/react-form";
import passwordReset from "../lib/api/passwordReset.ts";
import {useState} from "react";
import Info from "../lib/components/Info.tsx";

export const Route = createFileRoute('/forgot-password')({
    component: RouteComponent,
})

function RouteComponent() {
    const [success, setSuccess] = useState(false);

    const form = useForm({
        defaultValues: {
            code: "",
            password: ""
        },
        onSubmit: async (values) => {
            console.log(values);
            await passwordReset(values.value.code, values.value.password).then(() => {
                setSuccess(true)
            })
        }
    });

    return (
        <>
            <PageHeader icon={<IconUserQuestion size={18}/>} title="Forgot Password"/>
            <PageWrapper padding={"full"} center>
                <Container align={"center"}>
                    <p>
                        Resetting a password requires a code from a moderator
                    </p>
                    {success ? (
                        <Info type={"success"}>
                            Your password was reset
                        </Info>
                    ) : null}
                    <form
                        onSubmit={async (e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            await form.handleSubmit();
                        }}
                    >
                        <Container gap={"md"} align={"center"}>
                            <form.Field
                                name={"code"}
                                children={(field) => {
                                    return (
                                        <Input
                                            id={field.name}
                                            name={field.name}
                                            label={"Reset code"}
                                            type={"text"}
                                            value={field.state.value}
                                            onBlur={field.handleBlur}
                                            onChange={(e) => field.handleChange(e.target.value)}
                                        />
                                    )
                                }}
                            />
                            <form.Field
                                name={"password"}
                                children={(field) => {
                                    return (
                                        <Input
                                            id={field.name}
                                            name={field.name}
                                            label={"New password"}
                                            type={"password"}
                                            value={field.state.value}
                                            onBlur={field.handleBlur}
                                            onChange={(e) => field.handleChange(e.target.value)}
                                        />
                                    )
                                }}
                            />
                            <Container gap={"md"} align={"horizontal"}>
                                <form.Subscribe
                                    selector={(state) => [state.canSubmit, state.isSubmitting]}
                                    children={([canSubmit, isSubmitting]) => (
                                        <Button type="submit" disabled={!canSubmit}>
                                            {isSubmitting ? '...' : 'Reset'}
                                        </Button>
                                    )}
                                />
                            </Container>
                        </Container>
                    </form>
                </Container>
            </PageWrapper>
        </>
    )
}
