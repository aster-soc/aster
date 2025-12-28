import {createFileRoute, useLocation} from '@tanstack/react-router'
import UserPage from "../lib/components/page/UserPage.tsx";
import NotFoundPage from "../lib/components/page/NotFoundPage.tsx";
import * as React from "react";

/**
* # Subrouter
* Tanstack Router doesn't allow certain types of paths, this is where they go.
* */

export const Route = createFileRoute('/$')({
    component: RouteComponent
})

function RouteComponent() {
    const location = useLocation();

    function getPath() {
        // remove / at start of path
        return location.pathname?.slice(1)
    }

    const [path, setPath] = React.useState(getPath())

    React.useEffect(() => {
        console.debug(`subrouter: route changed, ${location.pathname}`)
        setPath(getPath())
    }, [location])

    if (!path) return <NotFoundPage/>

    if (
        // @user OR @user@example.com
        /^@[a-zA-Z0-9_\-.]+(@[a-zA-Z0-9_\-.]+)?/
            .test(path)
    ) {
        return <UserPage handle={path}/>
    } else {
        return <NotFoundPage/>
    }
}
