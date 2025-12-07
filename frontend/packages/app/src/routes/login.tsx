import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconLogin} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import {useForm} from "@tanstack/react-form";
import Input from "../lib/components/Input.tsx";
import Container from "../lib/components/Container.tsx";
import Button from "../lib/components/Button.tsx";
import Info from '../lib/components/Info.tsx';
import localstore from "../lib/utils/localstore.ts";
import router from "../lib/router.tsx";
import {Api} from 'aster-common'

export const Route = createFileRoute('/login')({
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
    const form = useForm({
        defaultValues: {
            username: '',
            password: ''
        },
        onSubmit: async (values) => {
            console.log(values);
            await Api.login(values.value.username, values.value.password).then((result) => {
                if (result.token && result.user) {
                    localstore.set("token", result.token);

                    let expiresAt = new Date();
                    expiresAt.setMonth((expiresAt.getMonth() !== 11) ? expiresAt.getMonth() + 1 : 0)

                    document.cookie = `AsAuthorization=${result.token}; expires=${expiresAt.toUTCString()}; Secure`;
                    localstore.set("self", JSON.stringify(result.user));

                    window.location.replace("/");
                }
            });
        }
    })

    return (
        <>
            <PageHeader
                icon={<IconLogin size={18}/>}
                title={"Login"}
            />
            <PageWrapper padding={"full"} center={true}>
                <form
                    onSubmit={async (e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        await form.handleSubmit();
                    }}
                >
                    <Container gap={"md"} align={"center"}>
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
                                            placeholder={"Username"}
                                            type={"text"}
                                            value={field.state.value}
                                            onBlur={field.handleBlur}
                                            onChange={(e) => field.handleChange(e.target.value)}
                                        />
                                        {!field.state.meta.isValid && (
                                            <Info text={true} type={"danger"}>{field.state.meta.errors}</Info>
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
                                            placeholder={"Password"}
                                            type={"password"}
                                            value={field.state.value}
                                            onBlur={field.handleBlur}
                                            onChange={(e) => field.handleChange(e.target.value)}
                                        />
                                        {!field.state.meta.isValid && (
                                            <Info text={true} type={"danger"}>{field.state.meta.errors}</Info>
                                        )}
                                    </>
                                )
                            }}
                        />

                        <Container align={"horizontal"}>
                            <Container align={"left"}>
                                <a href="/forgot-password">
                                    Forgot password?
                                </a>
                            </Container>

                            <Container align={"right"}>
                                <form.Subscribe
                                    selector={(state) => [state.canSubmit, state.isSubmitting]}
                                    children={([canSubmit, isSubmitting]) => (
                                        <Button type="submit" disabled={!canSubmit}>
                                            {isSubmitting ? '...' : 'Login'}
                                        </Button>
                                    )}
                                />
                            </Container>
                        </Container>
                    </Container>
                </form>
            </PageWrapper>
        </>
    )
}
