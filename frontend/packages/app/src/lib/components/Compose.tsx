import './Compose.scss'
import Container from "./Container.tsx";
import localstore from "../utils/localstore.ts";
import Avatar from "./Avatar.tsx";
import {IconAlertTriangle, IconMoodSmile, IconPaperclip} from "@tabler/icons-react";
import Button from "./Button.tsx";
import TextArea from "./TextArea.tsx";
import * as React from "react";
import {type RefObject, useEffect} from "react";
import Visibility from "./Visibility.tsx";
import Dropdown, {DropdownItem, type DropdownNode} from "./dropdown/Dropdown.tsx";
import Input from "./Input.tsx";
import {useStore} from "@tanstack/react-store";
import {store} from "../utils/state.ts";
import NoteSimple from "./NoteSimple.tsx";
import {useQuery} from "@tanstack/react-query";
import {Api} from 'aster-common'
import * as Common from 'aster-common'
import alert, {Alert, AlertType} from "../utils/alert.ts";

function Compose() {
    const replyingTo = useStore(store, (state) => state["replyingTo"]);
    const quoting = useStore(store, (state) => state["quoting"]);

	const editing = useStore(store, (state) => state["editing"]);

	const [cw, setCw] = React.useState<string | undefined>(undefined)
	const [showCwField, setShowCwField] = React.useState(localstore.getParsed("always_show_cw_compose"))

    const [content, setContent] = React.useState<string | undefined>(undefined)
	const [visibility, setVisibility] = React.useState("public")

	React.useEffect(() => {
		if (editing) Api.getNote(editing).then((note) => {
			if (!note) return

			setCw(note.cw)
			if (cw) showCwField(true)

			setContent(note.content)
			setVisibility(note.visibility)
		})
	}, [editing])

    let [visibilityDropdownOpen, setVisibilityDropdownOpen] = React.useState(false)
    let visibilityDropdownRef: RefObject<HTMLButtonElement | null> = React.createRef()
    let visibilityDropdownItems: DropdownNode[] = [
        new DropdownItem(
            undefined,
            <Visibility visibility={"public"}/>,
            "Public",
            undefined,
            () => setVisibility("public")
        ),
        new DropdownItem(
            undefined,
            <Visibility visibility={"unlisted"}/>,
            "Unlisted",
            undefined,
            () => setVisibility("unlisted")
        ),
        new DropdownItem(
            undefined,
            <Visibility visibility={"followers"}/>,
            "Followers",
            undefined,
            () => setVisibility("followers")
        ),
        new DropdownItem(
            undefined,
            <Visibility visibility={"direct"}/>,
            "Direct",
            undefined,
            () => setVisibility("direct")
        )
    ]

    const placeholders = [
        "What's on your mind?",
        "What are you thinking about?",
        "How's your day going?",
        "What's up?"
    ]
    // if this isn't a state, most interactions with the box recalculate this
    const [placeholder] =
        React.useState(placeholders[Math.floor(Math.random() * placeholders.length)]);

    let account = localstore.getSelf()

    if (account === undefined) return <></>

    function post() {
        if (!content && !quoting) return

        Api.createNote({
            cw: cw,
            content: content,
            visibility: visibility,
            replyingTo: replyingTo,
        }).then((e) => {
            if (e) {
                setCw(undefined)
                setContent(undefined)
                store.setState((state) => {
                    return {...state, ["replyingTo"]: undefined, ["quoting"]: undefined}
                })

                alert.add(new Alert("", AlertType.Success, "Posted note"))
            }
        })
    }

	function update() {
		if (!content && !quoting) return

		Api.editNote(editing, {
			cw: cw,
			content: content,
			visibility: visibility,
			replyingTo: replyingTo,
		}).then((e) => {
			if (e) {
				setCw(undefined)
				setContent(undefined)
				store.setState((state) => {
					return {...state, ["replyingTo"]: undefined, ["quoting"]: undefined, ["editing"]: undefined}
				})

				alert.add(new Alert("", AlertType.Success, "Updated note"))
			}
		})
	}

    function renderHeader() {
        return (
            <>
                <Container align={"horizontal"}>
                    <Container align={"left"}>
                        <Avatar size={"md"} user={account}/>
                    </Container>
                    <Container align={"right"}>
                        <Button
                            nav
                            ref={visibilityDropdownRef}
                            onClick={() => setVisibilityDropdownOpen(!visibilityDropdownOpen)}
                        >
                            <Visibility visibility={visibility}/>
                        </Button>
                    </Container>
                </Container>

                <Dropdown
                    items={visibilityDropdownItems}
                    parent={visibilityDropdownRef}
                    open={visibilityDropdownOpen}
                    setOpen={setVisibilityDropdownOpen}
                />
            </>
        )
    }

    const reply = useQuery({
        queryKey: ['compose_reply'],
        queryFn: async () => (replyingTo) ? await Api.getNote(replyingTo) : null,
    })

    const quote = useQuery({
        queryKey: ['compose_quote'],
        queryFn: async () => (quoting) ? await Api.getNote(quoting) : null,
    })

    useEffect(() => {
        reply.refetch()
        quote.refetch()
    }, [replyingTo, quoting])

    function renderReply() {
        if (replyingTo && reply.data)
            return (
                <NoteSimple data={reply.data}/>
            )
    }

    function renderQuote() {
        if (quoting && quote.data)
            return (
                <NoteSimple data={quote.data}/>
            )
    }

    function renderFooter() {
        return (
            <Container align={"horizontal"}>
                <Container align={"left"}>
                    <Container align={"horizontal"}>
                        <Button nav>
                            <IconPaperclip size={18}/>
                        </Button>
                        <Button nav onClick={() => setShowCwField(!showCwField)}>
                            <IconAlertTriangle size={18}/>
                        </Button>
                        <Button nav>
                            <IconMoodSmile size={18}/>
                        </Button>
                    </Container>
                </Container>
                <Container align={"right"}>
					{editing ? (
						<Button primary onClick={() => update()}>Update</Button>
					) : (
						<Button onClick={() => post()}>Post</Button>
					)}
                </Container>
            </Container>
        )
    }

    /*
    * Dropdown for IconPaperclip
    * Upload media
    * Use media from drive
    * Attach poll
    * */

    return (
        <div className={`compose`}>
            <Container gap={"md"}>
                {renderHeader()}
                {renderReply()}
                {showCwField ? (
                    <Input
                        wide
                        placeholder={"Content warning"}
                        value={cw}
                        onChange={(e) => setCw(e.target.value)}
                    />
                ) : null}
                <TextArea
                    wide
                    rows={5}
                    placeholder={placeholder}
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                />
                {renderQuote()}
                {renderFooter()}
            </Container>
        </div>
    )
}

export default Compose
