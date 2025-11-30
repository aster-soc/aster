import './Compose.scss'
import Container from "./Container.tsx";
import localstore from "../utils/localstore.ts";
import Avatar from "./Avatar.tsx";
import {IconAlertTriangle, IconMoodSmile, IconPaperclip} from "@tabler/icons-react";
import Button from "./Button.tsx";
import TextArea from "./TextArea.tsx";
import type {RefObject} from "react";
import * as React from "react";
import Visibility from "./Visibility.tsx";
import Dropdown, {DropdownItem, type DropdownNode} from "./dropdown/Dropdown.tsx";
import Input from "./Input.tsx";

function Compose() {
    let [cw, setCw] = React.useState(undefined)
    let [showCwField, setShowCwField] = React.useState(localstore.getParsed("always_show_cw_compose"))

    let [content, setContent] = React.useState(undefined)
    let [visibility, setVisibility] = React.useState("public")

    let [visibilityDropdownOpen, setVisibilityDropdownOpen] =
        React.useState(false)
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
        "Hows your day going?",
        "What's up?"
    ]
    // if this isn't a state, most interactions with the box recalculate this
    const [placeholder] =
        React.useState(placeholders[Math.floor(Math.random() * placeholders.length)]);

    let account = localstore.getSelf()

    if (account === undefined) return <></>

    function post() {
        console.log(`CW ${cw} CONTENT ${content} VIS ${visibility}`)
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
                    <Button onClick={() => post()}>Post</Button>
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
                {showCwField ? (
                    <Input
                        wide
                        placeholder={"Content warning"}
                        value={content}
                    />
                ) : null}
                <TextArea
                    wide
                    rows={5}
                    placeholder={placeholder}
                    value={content}
                />
                {renderFooter()}
            </Container>
        </div>
    )
}

export default Compose
