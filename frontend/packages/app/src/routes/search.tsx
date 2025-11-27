import {createFileRoute} from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconSearch} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import Container from "../lib/components/Container.tsx";
import Button from "../lib/components/Button.tsx";
import Input from "../lib/components/Input.tsx";
import {useForm} from "@tanstack/react-form";
import search from "../lib/api/search.ts";
import Timeline from "../lib/components/Timeline.tsx";
import {useState} from "react";
import Search from "../lib/components/Search.tsx";
import * as Common from 'aster-common'

export const Route = createFileRoute('/search')({
    component: RouteComponent,
})

function RouteComponent() {
    const [data, setData] = useState(new Common.SearchResults(false, []))

    const form = useForm({
        defaultValues: {
            query: "",
        },
        onSubmit: async (values) => {
            await search(values.value.query).then((data) => {
                setData(data);
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
                        <Timeline data={data.results} Component={Search}/>
                    </Container>
                </Container>
            </PageWrapper>
        </>
    )
}
