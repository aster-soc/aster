import "preact/debug"
import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {RouterProvider} from "@tanstack/react-router";
import router from "./lib/router.tsx";
import './main.scss'

const rootElement = document.getElementById('root')!
if (!rootElement.innerHTML) {
    const root = createRoot(rootElement)

    root.render(
        <StrictMode>
            <RouterProvider router={router}/>
        </StrictMode>
    )
}
