import * as React from "react";

function PageWrapper({padding = "full", center = false, children}: {
    padding: "full" | "timeline" | "none",
    center: boolean,
    children: React.ReactNode
}) {
    return <div className={`pageWrapper ${padding} ${center ? ' center' : ''}`} role={"main"}>
        {children}
    </div>
}

export default PageWrapper
