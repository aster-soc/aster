import {createFileRoute, useNavigate} from '@tanstack/react-router'
import PageHeader from "../../lib/components/PageHeader.tsx";
import {
	IconAuth2fa,
	IconDeviceDesktop, IconKey,
	IconLockPassword,
	IconLogout, IconPassword,
	IconSettings,
	IconShield,
	IconUser
} from "@tabler/icons-react";
import Tab from "../../lib/components/Tab.tsx";
import PageWrapper from "../../lib/components/PageWrapper.tsx";
import Container from "../../lib/components/Container.tsx";
import {useState} from "react";
import {useQuery} from "@tanstack/react-query";
import localstore from "../../lib/utils/localstore.ts";
import Loading from "../../lib/components/Loading.tsx";
import Error from "../../lib/components/Error.tsx";
import {useForm} from "@tanstack/react-form";
import Input from "../../lib/components/Input.tsx";
import TextArea from "../../lib/components/TextArea.tsx";
import Button from "../../lib/components/Button.tsx";
import {Api} from 'aster-common'

export const Route = createFileRoute('/settings/account')({
    component: RouteComponent,
})

function RouteComponent() {
    const navigate = useNavigate()

    const [tab, setTab] = useState(0)

    let self = localstore.getSelf()

    if (!self) navigate({to: "/"})

    const {data, error, isPending, isFetching, refetch} = useQuery({
        queryKey: [`user_${localstore.getSelf()?.id}`],
        queryFn: () => Api.getUser(self.id),
    });

    const form = useForm({
        defaultValues: {
            displayName: data?.displayName,
            bio: data?.bio,
            location: data?.location,
            birthday: data?.birthday,

            avatar: data?.avatar,
            avatarAlt: data?.avatarAlt,
            banner: data?.banner,
            bannerAlt: data?.bannerAlt,

            locked: data?.locked,
            suspended: data?.suspended,
            activated: data?.activated,
            automated: data?.automated,
            discoverable: data?.discoverable,
            indexable: data?.indexable,
            sensitive: data?.sensitive,

            isCat: data?.isCat,
            speakAsCat: data?.speakAsCat
        },
        onSubmit: async (values) => {
            console.log(values)
            Api.editUser(self.id, values.value).then((result) => {
                if (result) {
                    self = result
                    localstore.set("self", JSON.stringify(self))
                }
            })
        }
    })

    function renderTab() {
        switch (tab) {
            case 0:
                return (
                    <>
                        {isPending || isFetching ? (
                            <Loading fill/>
                        ) : error ? (
                            <Error error={error} retry={refetch}/>
                        ) : (
                            <>
                                <form
                                    onSubmit={async (e) => {
                                        e.preventDefault();
                                        e.stopPropagation();
                                        await form.handleSubmit();
                                    }}
                                >
                                    <Container gap={"md"} align={"startHorizontal"} fill>
                                        <Container gap={"md"} align={"left"}>
                                            <form.Field
                                                name={"displayName"}
                                                children={(field) => {
                                                    return (
                                                        <Input
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Display name"}
                                                            type={"text"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                            wide
                                                        />
                                                    )
                                                }}
                                            />
                                            <form.Field
                                                name={"location"}
                                                children={(field) => {
                                                    return (
                                                        <Input
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Location"}
                                                            type={"text"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                            wide
                                                        />
                                                    )
                                                }}
                                            />
                                            <form.Field
                                                name={"birthday"}
                                                children={(field) => {
                                                    return (
                                                        <Input
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Birthday"}
                                                            type={"date"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                            wide
                                                        />
                                                    )
                                                }}
                                            />
                                        </Container>
                                        <Container gap={"md"} align={"right"}>
                                            <form.Field
                                                name={"bio"}
                                                children={(field) => {
                                                    return (
                                                        <TextArea
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Bio"}
                                                            type={"text"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                            wide
                                                        />
                                                    )
                                                }}
                                            />
                                            <form.Field
                                                name={"locked"}
                                                children={(field) => {
                                                    return (
                                                        <Input
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Locked"}
                                                            type={"checkbox"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                        />
                                                    )
                                                }}
                                            />
                                            <form.Field
                                                name={"indexable"}
                                                children={(field) => {
                                                    return (
                                                        <Input
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Indexable"}
                                                            type={"checkbox"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                        />
                                                    )
                                                }}
                                            />
                                            <form.Field
                                                name={"isCat"}
                                                children={(field) => {
                                                    return (
                                                        <Input
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Are you a cat?"}
                                                            type={"checkbox"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                        />
                                                    )
                                                }}
                                            />
                                            <form.Field
                                                name={"speakAsCat"}
                                                children={(field) => {
                                                    return (
                                                        <Input
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Speak as cat"}
                                                            type={"checkbox"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                        />
                                                    )
                                                }}
                                            />
                                            <form.Field
                                                name={"sensitive"}
                                                children={(field) => {
                                                    return (
                                                        <Input
                                                            id={field.name}
                                                            name={field.name}
                                                            label={"Sensitive"}
                                                            type={"checkbox"}
                                                            value={field.state.value}
                                                            onBlur={field.handleBlur}
                                                            onChange={(e) => field.handleChange(e.target.value)}
                                                        />
                                                    )
                                                }}
                                            />
                                        </Container>
                                    </Container>
                                    <Container align={"left"}>
                                        <form.Subscribe
                                            selector={(state) => [state.canSubmit, state.isSubmitting]}
                                            children={([canSubmit, isSubmitting]) => (
                                                <Button type="submit" disabled={!canSubmit}>
                                                    {isSubmitting ? '...' : 'Submit'}
                                                </Button>
                                            )}
                                        />
                                    </Container>
                                </form>
                            </>
                        )}
                    </>
                )
			case 1:
				return (
					<>
						<Container gap={"md"} align={"startHorizontal"} fill>
							<Container gap={"md"} align={"left"}>
								<Button>
									<IconPassword size={18} />
									Enable 2FA
								</Button>
							</Container>
							<Container gap={"md"} align={"right"}>
								<Button>
									<div>
										<IconKey size={18} />
									</div>
									Setup passkey
								</Button>
							</Container>
						</Container>
					</>
				)
        }
    }

    return (
        <>
            <PageHeader icon={<IconSettings size={18}/>} title={"Settings"}>
                <Tab
                    selected={false}
                    onClick={() => navigate({to: "/settings"})}
                >
                    <IconDeviceDesktop size={18}/>
                    Client
                </Tab>
                <Tab
                    selected={true}
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
						<IconSettings size={18}/>
						General
					</Tab>
					<Tab
						selected={tab === 1}
						onClick={() => setTab(1)}
					>
						<IconShield size={18}/>
						Security
					</Tab>

                    <Container align={"right"}>
						<Button danger to={"/logout"}>
							<IconLogout size={18}/>
							Logout
						</Button>
					</Container>
                </Container>
                <Container gap={"md"} padding={"12px"}>
                    {renderTab()}
                </Container>
            </PageWrapper>
        </>
    )
}
