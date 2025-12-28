import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconUserPlus} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import {useForm} from "@tanstack/react-form";
import Input from "../lib/components/Input.tsx";
import Container from "../lib/components/Container.tsx";
import Button from "../lib/components/Button.tsx";
import Info from '../lib/components/Info.tsx';
import localstore from "../lib/utils/localstore.ts";
import router from "../lib/router.tsx";
import {useQuery} from "@tanstack/react-query";
import {Api} from 'aster-common'

export const Route = createFileRoute('/register')({
    component: RouteComponent,
    onEnter: async () => {
        const token = localstore.getParsed('token');

        if (token)
            router.navigate({
                href: "/"
            });
    }
})

function RouteComponent() {
    const {isLoading, isError, error, data} = useQuery({
        queryKey: ['meta'],
        queryFn: () => Api.getMeta(),
    });

    const inviteParam = new URLSearchParams(window.location.search).get('invite');

    const form = useForm({
        defaultValues: {
            invite: inviteParam ?? "",
            username: '',
            password: ''
        },
        onSubmit: async (values) => {
            console.log(values);
            await Api.register(values.value.username, values.value.password, values.value.invite).then((result) => {
                if (result.token && result.user) {
                    localstore.set("token", result.token);

                    let expiresAt = new Date();
                    expiresAt.setMonth((expiresAt.getMonth() !== 11) ? expiresAt.getMonth() + 1 : 0)

                    document.cookie = `AsAuthorization=${result.token}; expires=${expiresAt.toUTCString()}; Secure`;
                    localstore.set("self", JSON.stringify(result.user));

                    router.navigate({
                        href: "/"
                    });
                }
            });
        }
    })
    return (
        <>
            <PageHeader
                icon={<IconUserPlus size={18}/>}
                title={"Register"}
            />
            <PageWrapper padding={"full"} center={true}>
                <form
                    onSubmit={async (e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        await form.handleSubmit();
                    }}
                >
                    {isLoading ? <p>Loading</p> : isError ? <p>Error: {error.message}</p> : (
                        <Container gap={"md"} align={"center"}>
                            <>
                                <form.Field
                                    name={"username"}
                                    validators={{
                                        onChange: ({value}) =>
                                            !value
                                                ? "Username required"
                                                : (value.length < 1)
                                                    ? "Username must be at least 1 character"
                                                    : undefined,
                                        onChangeAsyncDebounceMs: 500
                                    }}
                                    children={(field) => {
                                        return (
                                            <>
                                                <Input
                                                    id={field.name}
                                                    name={field.name}
													label={"Username"}
                                                    placeholder={"kodee"}
                                                    type={"username"}
                                                    value={field.state.value}
                                                    onBlur={field.handleBlur}
                                                    onChange={(e) => field.handleChange(e.target.value)}
                                                />
                                                {!field.state.meta.isValid && (
                                                    <Info text={true}
                                                          type={"danger"}>{field.state.meta.errors}</Info>
                                                )}
                                            </>
                                        )
                                    }}
                                />
                                <form.Field
                                    name={"password"}
                                    validators={{
                                        onChange: ({value}) =>
                                            !value
                                                ? "Password required"
                                                : (value.length < 6)
                                                    ? "Password must be at least 6 characters"
                                                    : undefined,
                                        onChangeAsyncDebounceMs: 500
                                    }}
                                    children={(field) => {
                                        return (
                                            <>
                                                <Input
                                                    id={field.name}
                                                    name={field.name}
													label={"Password"}
                                                    placeholder={"********"}
                                                    type={"password"}
                                                    value={field.state.value}
                                                    onBlur={field.handleBlur}
                                                    onChange={(e) => field.handleChange(e.target.value)}
                                                />
                                                {!field.state.meta.isValid && (
                                                    <Info text={true}
                                                          type={"danger"}>{field.state.meta.errors}</Info>
                                                )}
                                            </>
                                        )
                                    }}
                                />
                            </>

                            {data?.registrations === "invite" ? (
                                <>
                                    <br/>
                                    <form.Field
                                        name={"invite"}
                                        validators={{
                                            onChange: ({value}) =>
                                                !value
                                                    ? "Invite required"
                                                    : (value.length < 12)
                                                        ? "Invite must be at least 16 characters"
                                                        : undefined,
                                            onChangeAsyncDebounceMs: 500
                                        }}
                                        children={(field) => {
                                            return (
                                                <>
                                                    <Input
                                                        id={field.name}
                                                        name={field.name}
														label={"Invite"}
                                                        placeholder={"****************"}
                                                        type={"text"}
                                                        value={field.state.value}
                                                        onBlur={field.handleBlur}

                                                        onChange={(e) => field.handleChange(e.target.value)}
                                                    />
                                                    {!field.state.meta.isValid && (
                                                        <Info text={true}
                                                              type={"danger"}>{field.state.meta.errors}</Info>
                                                    )}
                                                </>
                                            )
                                        }}
                                    />
                                </>
                            ) : null}

                            <Container align={"horizontal"}>
                                <Container align={"right"}>
                                    <form.Subscribe
                                        selector={(state) => [state.canSubmit, state.isSubmitting]}
                                        children={([canSubmit, isSubmitting]) => (
                                            <Button type="submit" disabled={!canSubmit}>
                                                {isSubmitting ? '...' : 'Register'}
                                            </Button>
                                        )}
                                    />
                                </Container>
                            </Container>
                        </Container>
                    )}
                </form>
            </PageWrapper>
        </>
    )
}
