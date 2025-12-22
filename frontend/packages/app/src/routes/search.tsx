import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconSearch} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import Container from "../lib/components/Container.tsx";
import Button from "../lib/components/Button.tsx";
import Input from "../lib/components/Input.tsx";
import {useForm} from "@tanstack/react-form";
import {Api} from 'aster-common'

export const Route = createFileRoute('/search')({
    component: RouteComponent,
})

function RouteComponent() {
    const form = useForm({
        defaultValues: {
            query: "",
        },
        onSubmit: async (values) => {
            Api.search(values.value.query).then((data) => {
            });
        }
    });

    return (
        <>
            <PageHeader icon={<IconSearch size={18}/>} title={"Search"}/>
            <PageWrapper padding={"full"} center={false}>
                <Container gap={"md"}>
                    <form
                        onSubmit={async (e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            await form.handleSubmit();
                        }}
                    >
                        <Container gap={"md"} align={"horizontal"}>
                            <form.Field
                                name={"query"}
                                children={(field) => {
                                    return (
                                        <Input
                                            id={field.name}
                                            name={field.name}
                                            placeholder={"Type a query..."}
                                            type={"text"}
                                            value={field.state.value}
                                            onBlur={field.handleBlur}
                                            onChange={(e) => field.handleChange(e.target.value)}
                                            wide
                                        />
                                    )
                                }}
                            />

                            <form.Subscribe
                                selector={(state) => [state.canSubmit, state.isSubmitting]}
                                children={([canSubmit, isSubmitting]) => (
                                    <Button type="submit" disabled={!canSubmit}>
                                        {isSubmitting ? '...' : 'Search'}
                                    </Button>
                                )}
                            />
                        </Container>
                    </form>
                    <Container>
                        {/* <Timeline query={} Component={Search}/> */}
                    </Container>
                </Container>
            </PageWrapper>
        </>
    )
}
