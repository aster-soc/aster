import * as React from "react";

function PageHeader({icon, title, children}: { icon: React.ReactNode, title: string, children?: React.ReactNode }) {
    React.useEffect(() => {
        document.title = `${title} - Aster`
    })

    return (
        <div className={"pageHeader"} role={"heading"}>
            <div className={"icon"} aria-hidden={true}>
                {icon}
            </div>
            <div className={"title"}>
                {title}
            </div>
            <div className={"children"}>
                {children}
            </div>
        </div>
    )
}

export default PageHeader
