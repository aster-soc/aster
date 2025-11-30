import * as Common from 'aster-common'
import './Notification.scss'
import Container from "./Container.tsx";
import {
    IconAlertCircle,
    IconAlertTriangle,
    IconAt,
    IconDental,
    IconHeartBroken,
    IconPlus,
    IconStar,
    IconUser,
    IconUserCheck,
    IconUserPlus
} from "@tabler/icons-react";
import NoteSimple from "./NoteSimple.tsx";

function Notification(
    {data}:
    {
        data: Common.Notification,
    }
) {
    function renderName() {
        return <a href={"/@" + Common.renderHandle(data.from)}>
            {data.from.displayName ?? data.from.username}
        </a>
    }

    function renderIcon() {
        // @ts-ignore this isn't actually an enum
        switch (data.type as string) {
            case "like":
                return <IconStar size={18} color={"var(--like)"}/>
            case "react":
                return <IconPlus size={18} color={"var(--ac-1)"}/>
            case "follow":
                return <IconUserPlus size={18} color={"var(--ac-1)"}/>
            case "acceptedFollow":
                return <IconUserCheck size={18} color={"var(--ac-1)"}/>
            case "mention":
                return <IconAt size={18} color={"var(--ac-1)"}/>
            case "announcement":
                return <IconAlertCircle size={18} color={"var(--ac-1)"}/>
            case "brokenRelationship":
                return <IconHeartBroken size={18} color={"var(--ac-1)"}/>
            case "bite":
                return <IconDental size={18} color={"var(--ac-1)"}/>
            case "registration":
                return <IconUser size={18} color={"var(--like)"}/>
            case "report":
                return <IconAlertTriangle size={18} color={"var(--warn)"}/>
            default:
                return null
        }
    }

    function renderTitle() {
        // @ts-ignore this isn't actually an enum
        switch (data.type as string) {
            case "like":
                return <>
                    <p>{renderName()} liked your post</p>
                </>
            case "react":
                return <>
                    <p>{renderName()} reacted to your post</p>
                </>
            case "follow":
                return <>
                    <p>{renderName()} followed you</p>
                </>
            case "acceptedFollow":
                return <>
                    <p>{renderName()} accepted your follow request</p>
                </>
            case "mention":
                return <>
                    <p>{renderName()} mentioned you</p>
                </>
            case "announcement":
                return <>
                    <p>{/* announcement title */}</p>
                </>
            case "brokenRelationship":
                return <>
                    <p>One or more relationship broke.</p>
                </>
            case "bite":
                return <>
                    <p>{renderName()} bit you</p>
                </>
            case "registration":
                return <>
                    <p>{renderName()} registered</p>
                </>
            case "report":
                return <>
                    <p>{renderName()} submitted a report</p>
                </>
            default:
                return null
        }
    }

    function renderBody() {
        // @ts-ignore this isn't actually an enum
        switch (data.type as string) {
            case "like":
                return data.note ? <>
                    <NoteSimple data={data.note}/>
                </> : null
            case "react":
                return data.note ? <>
                    <NoteSimple data={data.note}/>
                </> : null
            case "mention":
                return data.note ? <>
                    <NoteSimple data={data.note}/>
                </> : null
            case "bite":
                return data.note ? <>
                    <NoteSimple data={data.note}/>
                </> : null
            default:
                return null
        }
    }

    return (
        <div className={`notification`}>
            <Container align={"horizontal"} gap={"lg"}>
                {renderIcon()}
                {renderTitle()}
            </Container>
            {renderBody()}
        </div>
    )
}

export default Notification
