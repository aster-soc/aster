import "./UserPage.scss";
import PageHeader from "../PageHeader.tsx";
import PageWrapper from "../PageWrapper.tsx";
import {
	IconArrowBackUp,
	IconCake,
	IconDots,
	IconMapPin,
	IconNote, IconPhoto,
	IconUser
} from "@tabler/icons-react";
import {useQuery} from "@tanstack/react-query";
import Loading from "../Loading.tsx";
import Error from "../Error.tsx";
import Avatar from "../Avatar.tsx";
import {useEffect, useState} from "react";
import Container from "../Container.tsx";
import Button from "../Button.tsx";
import Mfm from "../Mfm.tsx";
import * as Common from 'aster-common'
import {Api} from 'aster-common'
import FollowButton from "../FollowButton.tsx";
import Tab from "../Tab.tsx";
import UserPage_Notes from "./UserPage_Notes.tsx";

function UserPage(
    {handle}: { handle: string }
) {
	if (handle === undefined) return null

	let [displayName, setDisplayName] = useState(handle);
    let [tab, setTab] = useState(0);

    const {isLoading, isError, error, data} = useQuery({
        queryKey: [`user_${handle}`],
        queryFn: () => Api.lookupUser(handle).then((e) => {
            setDisplayName(e?.displayName ?? e?.username ?? handle);
            return e
        }),
    });

	const relationship = useQuery({
		queryKey: [`relationship_${data?.id}`],
		queryFn: () => (data?.id) ? Api.getUserRelationship(data.id) : undefined,
	});

	function renderTab() {
		switch (tab) {
			case 0: return <UserPage_Notes />;
			case 1: return <UserPage_Notes withReplies />;
			case 2: return <UserPage_Notes media />;
		}
	}

	function renderRelationshipTag() {
		const data = relationship?.data
		if (data === undefined) return null

		//if (data.type === "follow")
	}

    function render() {
        return (
            <>
                <Container gap={"lg"}>
                    <div
						className={"userHeader"}
						style={{ backgroundImage: `url(${data?.banner})` }}
					/>
                    <div className={"userIdentity"}>
                        <Container gap={"xl"} align={"horizontal"}>
                            <Container align={"horizontal"} gap={"md"}>
                                <Container>
                                    <Avatar size={"xl"} user={data} thought={data?.id} />
                                </Container>
                                <Container gap={"sm"}>
                                    <span className={"displayName"}>{displayName}</span>
                                    <span className={"username"}>{Common.renderHandle(data)}</span>
                                </Container>
                            </Container>

                            <FollowButton id={data?.id} query={relationship}/>
                            <Button>
								<IconDots size={18}/>
							</Button>
                        </Container>
                    </div>
                    <div className={"underHeader"}>
                        <Container align={"left"} gap={"md"}>
                            <span
                                className={"bio" + ((data?.bio === undefined || data?.bio === "") ? " none" : "")}
                            >
                                {(data?.bio === undefined || data?.bio === "") ? "This user hasn't written a description yet." : (
                                    <Mfm text={data.bio}></Mfm>
                                )}
                            </span>

							{data?.location != null ? (
								<Container align={"horizontal"} gap={"md"}>
									<IconMapPin size={18}/>
									<span className={"birthday"}>{data.location}</span>
								</Container>
							) : null}

                            {data?.birthday != null ? (
                                <Container align={"horizontal"} gap={"md"}>
                                    <IconCake size={18}/>
                                    <span className={"birthday"}>
										{new Date(data?.birthday).toLocaleString(
											[],
											{ dateStyle: "long" }
										)}
									</span>
                                </Container>
                            ) : null}

                            {data?.createdAt != null ? (
                                <span className={"createdAt"}>Joined on {
									new Date(data.createdAt).toLocaleString(
										[],
										{ dateStyle: "long", timeStyle: "short" }
									)
								}</span>
                            ) : null}
                        </Container>
                    </div>
                </Container>
				<div className={"underHeaderTabs"}>
					<Container gap={"xl"} align={"horizontal"}>
						<Tab
							selected={tab === 0}
							onClick={() => setTab(0)}
						>
							<IconNote size={18} />
							Notes
						</Tab>
						<Tab
							selected={tab === 1}
							onClick={() => setTab(1)}
						>
							<IconArrowBackUp size={18} />
							Notes with Replies
						</Tab>
						<Tab
							selected={tab === 2}
							onClick={() => setTab(2)}
						>
							<IconPhoto size={18} />
							Media
						</Tab>
					</Container>
				</div>
				{renderTab()}
            </>
        )
    }

    return (
        <div className={"userPage"}>
            <PageHeader icon={(data === undefined) ? <IconUser size={18}/> : <Avatar size={"sm"} user={data}/>}
                        title={displayName + (displayName ? " (" + Common.renderHandle(data) + ")" : null)}/>
            <PageWrapper padding={"full"} center={false}>
                {isLoading ? (
                    <Loading fill={true}/>
                ) : isError ? (<Error error={error}/>) : render()}
            </PageWrapper>
        </div>
    )
}

export default UserPage
