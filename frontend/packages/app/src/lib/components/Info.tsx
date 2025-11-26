import * as React from "react";
import {IconX} from "@tabler/icons-react";
import './Info.scss'

function Info(
    {text, type, icon, dismissable, small, children}:
    {
        text?: boolean,
        type?: undefined | "primary" | "success" | "warn" | "danger",
        icon?: React.ReactNode,
        dismissable?: boolean,
        small?: boolean,
        children?: React.ReactNode
    }
) {
    let [shown, setShown] = React.useState(true)

    // TODO: voiceover doesnt read alert content

    if (shown)
        return (
            <div className={`info ${type}${text ? " text" : ""}` + (small ? " small" : "")}
                 aria-label={`Alert (${type})`} tabIndex={0}>
                {icon ?
                    <div className={"icon"} aria-hidden="true">
                        {icon}
                    </div>
                    : null}

                <div className={"content"}>
                    {children}
                </div>

                {dismissable ?
                    <button className={"dismiss"} onClick={() => setShown(false)}>
                        <IconX size={18}/>
                    </button>
                    : null}
            </div>
        )
}

export default Info
