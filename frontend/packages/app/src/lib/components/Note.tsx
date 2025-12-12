import * as React from "react";
import {type RefObject, useState} from "react";
import * as Common from "aster-common";
import {Api} from "aster-common";
import './Note.scss'
import Container from "./Container.tsx";
import Avatar from "./Avatar.tsx";
import {
    IconAlertTriangle,
    IconArrowBackUp,
    IconArrowBackUpDouble,
    IconBookmark,
    IconDots,
    IconExternalLink,
    IconFlag,
    IconLink,
    IconMaximize,
    IconPencil,
    IconPlus,
    IconRepeat,
    IconStar,
    IconTrash
} from "@tabler/icons-react";
import DateTime from "./DateTime.tsx";
import Visibility from "./Visibility.tsx";
import Button from "./Button.tsx";
import {Link, useNavigate} from "@tanstack/react-router";
import localstore from "../utils/localstore.ts";
import Dropdown, {DropdownDivider, DropdownItem, type DropdownNode} from "./dropdown/Dropdown.tsx";
import Mfm from "./Mfm.tsx";
import {useStore} from "@tanstack/react-store";
import {store} from "../utils/state.ts";
import alert, {Alert, AlertType} from '../utils/alert.ts'

function Note(
    {data, detailed = false}:
    { data: Common.Note, detailed?: boolean }
) {
    const navigate = useNavigate();

    const replyingTo = useStore(store, (state) => state["replyingTo"]);

    let [show, setShow] = React.useState(true)
    let [note, setNote] = React.useState(data)
    let [repeater, setRepeater] = React.useState()

    let self = localstore.getSelf()
    let isOwnPost = note?.user?.id === self?.id

    let [cwOpen, setCwOpen] = React.useState(false)

    if (note?.repeat !== undefined && note?.content === undefined) {
        setRepeater(note?.user)
        setNote(note?.repeat)
    }

    /* Interactions */

    function like() {
        Api.likeNote(note?.id).then((e) => {
            if (e?.id === note?.id) setNote(e)
        })
    }

    function repeat() {
        let existingRepeat: Common.SmallNote | undefined =
            (note?.repeats?.find((e) => e?.user?.id === self?.id))

        console.log("existing repeat", existingRepeat)

        if (existingRepeat) {
            Api.deleteNote(existingRepeat?.id).then(() => {
                setShow(false)
            })
        } else {
            Api.repeatNote(note?.id)
        }
    }

    /* Rendering */

    function renderHandle() {
        return (
            <span className={"username"}>{Common.renderHandle(note.user)}</span>
        )
    }

    function renderContentWarning() {
        // todo: screenreader check
        return (
            <div className={"cw"}>
                <Container gap={"md"} align={"horizontal"}>
                    <Container>
                        <IconAlertTriangle size={18}/>
                    </Container>
                    <Container align={"left"}>
                        <span>{note?.cw}</span>
                    </Container>
                    <Container>
                        {!cwOpen ? (
                            <Button thin onClick={() => setCwOpen(true)}>Show</Button>
                        ) : (
                            <Button thin onClick={() => setCwOpen(false)}>Hide</Button>
                        )}
                    </Container>
                </Container>
            </div>
        )
    }

    function renderContent() {
        // todo: screenreader check
        if (!note?.cw) {
            return (
                <Container>
                    <Mfm text={note?.content}/>
                </Container>
            )
        } else {
            return (
                <Container gap={"md"}>
                    {!cwOpen ? renderContentWarning() : (
                        <>
                            {renderContentWarning()}
                            <Container>
                                <Mfm text={note?.content}/>
                            </Container>
                        </>
                    )}
                </Container>
            )
        }
    }

    function renderTags() {
        if (!note.tags || note.tags.length === 0) return null

        let tags: React.JSX.Element[] = []
        for (const tag of note.tags) {
            tags.push(
                <div className={"tag " + tag}>
                    <Link to={"/tag/" + tag}>
                        #{tag}
                    </Link>
                </div>
            )
        }

        return (
            <Container align={"horizontal"} gap={"sm"}>
                {tags}
            </Container>
        )
    }

    const [dropdownOpen, setDropdownOpen] = useState(false)
    let dropdownRef: RefObject<HTMLButtonElement | null> = React.createRef()

    let dropdownItems: DropdownNode[] = []

    if (!detailed) dropdownItems.push(
        new DropdownItem(
            undefined,
            <IconMaximize size={18}/>,
            'Expand note',
            `/note/${data.id}`
        ),
        new DropdownDivider()
    )

    dropdownItems.push(
        new DropdownItem(
            undefined,
            <IconLink size={18}/>,
            'Copy link',
            undefined,
            () => window.navigator.clipboard
                .writeText(window.location.href + "note/" + note.id)
                .then(() => {
                    alert.add(new Alert(
                        "",
                        AlertType.Success,
                        "Copied link"
                    ))
                })
        )
    )

    if (data.user.host)
        dropdownItems.push(
            new DropdownItem(
                undefined,
                <IconLink size={18}/>,
                'Copy remote link',
                undefined,
                () => window.navigator.clipboard
                    .writeText(note.apId)
                    .then(() => {
                        alert.add(new Alert(
                            "",
                            AlertType.Success,
                            "Copied remote link"
                        ))
                    })
            ),
            new DropdownItem(
                undefined,
                <IconExternalLink size={18}/>,
                'View on remote',
                note.apId,
                () => {
                }
            )
        )

    if (self)
        dropdownItems.push(
            new DropdownDivider(),
            new DropdownItem(
                undefined,
                <IconBookmark size={18}/>,
                'Bookmark note',
                undefined,
                () => Api.bookmarkNote(note.id).then(() => {
                    alert.add(new Alert(
                        "",
                        AlertType.Success,
                        "Bookmarked note"
                    ))
                })
            ),
            new DropdownItem(
                "danger",
                <IconFlag size={18}/>,
                'Report note',
                undefined,
                () => {
                }
            ),
        )

    // or is admin
    if (isOwnPost)
        dropdownItems.push(
            new DropdownDivider(),
            new DropdownItem(
                undefined,
                <IconPencil size={18}/>,
                'Edit note',
                undefined,
                () => {
                }
            ),
            new DropdownItem(
                "danger",
                <IconTrash size={18}/>,
                'Delete note',
                undefined,
                () => Api.deleteNote(note?.id).then(() => {
                    alert.add(new Alert(
                        "",
                        AlertType.Success,
                        "Deleted note"
                    ))
                })
            )
        )

    return show ? (
        <article className={"note highlightable"} tabIndex={0}
                 aria-label={`Note by ${Common.renderHandle(note.user)}${note?.content ? ", " + note?.content : ""}`}>
            {repeater != null ? (
                <Container align={"horizontal"} gap={"md"} clazz={"repeatHeader"}>
                    <IconRepeat size={18} color={"var(--tx-3)"}/>
                    <span>
                        {repeater?.displayName ?? repeater?.username} repeated
                    </span>
                </Container>
            ) : null}
            <Container align={"horizontal"} gap={"lg"}>
                <Container>
                    <Avatar user={note?.user}/>
                </Container>
                <Container align={"left"}>
                    <span
                        className={"names"}
                        onClick={() => navigate({to: `/${Common.renderHandle(note.user)}`})}
                    >
                        <p className={"displayName"}>{note?.user?.displayName ?? note?.user?.username}</p>
                        <p className={"handle"}>{renderHandle()}</p>
                    </span>
                </Container>
                <Container align={"right"}>
                    <Container gap={"sm"} onClick={() => navigate({to: `/note/${note.id}`})}>
                        <DateTime date={note?.createdAt} short={true}/>
                        <Visibility visibility={note?.visibility}/>
                    </Container>
                </Container>
            </Container>

            {note?.replyingTo ? (
                <a href={"/note/" + note?.replyingTo?.id}>
                    <Container align={"horizontal"} gap={"sm"}>
                        <IconArrowBackUp aria-hidden size={18}/>
                        <span>Replying to {note?.replyingTo?.user?.displayName ?? note?.replyingTo?.user?.username}</span>
                    </Container>
                </a>
            ) : null}

            {renderContent()}

            {renderTags()}

            <footer>
                <button
                    className={"highlightable"}
                    title={"Reply"}
                    onClick={() => store.setState((state) => {
                        return {...state, ["replyingTo"]: note?.id}
                    })}
                >
                    {note?.replyingTo ? (
                        <IconArrowBackUpDouble aria-hidden size={20}/>
                    ) : (
                        <IconArrowBackUp aria-hidden size={20}/>
                    )}
                </button>
                <button
                    className={"highlightable" + ((note?.repeats?.some((e) => e?.user?.id === self?.id)) ? " repeated" : "")}
                    title={"Repeat"}
                    onClick={() => repeat()}
                >
                    <IconRepeat aria-hidden size={20}/>
                    {localstore.getParsed("hide_repeat_count") ? null : (
                        (note?.repeats?.length >= 1) ? <span>{note?.repeats?.length}</span> : null
                    )}
                </button>
                <button
                    className={"highlightable" + ((note?.likes?.some((e) => e?.id === self?.id)) ? " liked" : "")}
                    title={"Like"}
                    onClick={() => like()}
                >
                    <IconStar aria-hidden size={20}/>
                    {localstore.getParsed("hide_like_count") ? null : (
                        (note?.likes?.length >= 1) ? <span>{note?.likes?.length}</span> : null
                    )}
                </button>
                <button className={"highlightable"} title={"React"}>
                    <IconPlus aria-hidden size={20}/>
                </button>
                <button className={`highlightable${dropdownOpen ? " selected" : ""}`} title={"More"}
                        ref={dropdownRef}
                        onClick={() => setDropdownOpen(!dropdownOpen)}>
                    <IconDots aria-hidden size={20}/>
                </button>
            </footer>

            <Dropdown items={dropdownItems} parent={dropdownRef} open={dropdownOpen} setOpen={setDropdownOpen}/>
        </article>
    ) : null
}

export default Note
