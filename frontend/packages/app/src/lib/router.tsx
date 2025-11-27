import {createRouter} from "@tanstack/react-router";
import {routeTree} from "../routeTree.gen.ts";
import ErrorPage from "./components/page/ErrorPage.tsx";

const router = createRouter({
    routeTree,
    defaultErrorComponent: ErrorPage
})

declare module '@tanstack/react-router' {
    interface Register {
        router: typeof router
    }
}

export default router
