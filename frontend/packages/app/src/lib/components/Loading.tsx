import './Loading.scss'
import {IconLoader2} from "@tabler/icons-react";
import * as React from "react";

function Loading(
    {size, fill, ...props}:
    {
        size?: number,
        fill?: boolean,
    }
) {
    let [ticks, setTicks] = React.useState(0)

    if (fill) setInterval(() => setTicks(ticks + 1), 1000)

    return (
        <div className={`loading ${fill ? "fill" : ""}`} {...props}>
            <span className={"spinner"}>
                <IconLoader2 size={size}/>
            </span>
            <div className={"notice" + (ticks >= 5 ? " show" : "")}>
                <i>This is taking longer than it should...</i>
            </div>
        </div>
    )
}

export default Loading
