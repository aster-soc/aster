import {createFileRoute, useNavigate} from '@tanstack/react-router'
import PageHeader from "../../lib/components/PageHeader.tsx";
import {
	IconDeviceDesktop,
	IconKey, IconLock, IconLockOff,
	IconLogout,
	IconPassword,
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
import Modal from "../../lib/components/modal/Modal.tsx";
import ConfirmationModal from "../../lib/components/modal/ConfirmationModal.tsx";
import alert, {Alert, AlertType} from "../../lib/utils/alert.ts";

export const Route = createFileRoute('/settings/account')({
    component: RouteComponent,
})

function RouteComponent() {
    const navigate = useNavigate()

    const [tab, setTab] = useState(0)

	function navigateTab(tab: number) {
		setTab(tab)
	}

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

	const [showLogoutModel, setShowLogoutModel] = useState(false);

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
				const loginRequirement = useQuery({
					queryKey: [`user_loginreq_${localstore.getSelf()?.id}`],
					queryFn: () => Api.getLoginRequirements(self.username),
				});

				const [show2faModal, setShow2faModal] = useState(false);
				const [secret, setSecret] = useState("");

				function start2faRegistration() {
					setShow2faModal(true);
					Api.userRegisterTotp().then((e) => {
						setSecret(e?.secret);
					})
				}

				function unregister2fa() {
					Api.userUnregisterTotp()
					alert.add(new Alert("", AlertType.Success, "Unregistered 2FA"))
				}

				return (
					<>
						<Container gap={"md"} align={"startHorizontal"}>
							<Container gap={"md"} align={"left"}>
								{loginRequirement.data?.totp ? (
										<>
											{/*
											<Button onClick={}>
												<IconPassword size={18} />
												Test 2FA
											</Button>
											*/}
											<Button danger onClick={unregister2fa}>
												<IconLockOff size={18} />
												Disable 2FA
											</Button>
										</>
								) : (
									<Button onClick={start2faRegistration}>
										<IconLock size={18} />
										Enable 2FA
									</Button>
								)}
							</Container>
							{/*
							<Container gap={"md"} align={"right"}>
								<Button>
									<IconKey size={18} />
									Setup passkey
								</Button>
							</Container>
							*/}
						</Container>

						<Modal
							title={"Enable 2FA"}
							show={show2faModal}
							setShow={setShow2faModal}
							actions={<>
								<Button onClick={() => setShow2faModal(false)}>
									Continue
								</Button>
							</>}
						>
							<Container gap={"lg"} align={"left"}>
								<p>In your 2FA app, enter the following secret:</p>
								<p>
									<code>{secret}</code>
								</p>
								<p>
									Afterwards, you can confirm the codes generated work by clicking "Test 2FA," or you
									can choose to disable it by pressing "Disable 2FA."
								</p>
							</Container>
						</Modal>
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
						onClick={() => navigateTab(0)}
					>
						<IconSettings size={18}/>
						General
					</Tab>
					<Tab
						selected={tab === 1}
						onClick={() => navigateTab(1)}
					>
						<IconShield size={18}/>
						Security
					</Tab>

                    <Container align={"right"}>
						<Button danger onClick={() => setShowLogoutModel(true)}>
							<IconLogout size={18}/>
							Logout
						</Button>
					</Container>
                </Container>
                <Container gap={"md"} padding={"12px"}>
                    {renderTab()}
                </Container>

				<ConfirmationModal
					title={"Logout"}
					body={"Are you sure you'd like to logout?"}
					action={() => navigate({to:"/logout"})}
					show={showLogoutModel}
					setShow={setShowLogoutModel}
				/>
            </PageWrapper>
        </>
    )
}
