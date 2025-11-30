import {IconHome, IconLock, IconMail, IconQuestionMark, IconWorld} from "@tabler/icons-react";

function Visibility({visibility}: { visibility: string }) {
    switch (visibility) {
        case "public":
            return (
                <IconWorld className={"highlightable"} size={18} aria-label={"Public visibility"} title={"Public"}
                           tabIndex={0}/>
            )
        case "unlisted":
            return (
                <IconHome className={"highlightable"} size={18} aria-label={"Unlisted visibility"} title={"Unlisted"}
                          tabIndex={0}/>
            )
        case "followers":
            return (
                <IconLock className={"highlightable"} size={18} aria-label={"Followers only visibility"}
                          title={"Followers Only"} tabIndex={0}/>
            )
        case "direct":
            return (
                <IconMail className={"highlightable"} size={18} aria-label={"Direct visibility"} title={"Direct"}
                          tabIndex={0}/>
            )
        default:
            return (
                <IconQuestionMark className={"highlightable"} size={18}
                                  aria-label={`Unknown visibility (${visibility})`} title={`Unknown (${visibility})`}
                                  tabIndex={0}/>
            )
    }
}

export default Visibility;
