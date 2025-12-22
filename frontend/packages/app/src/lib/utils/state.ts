import {Store} from "@tanstack/react-store";

export const store = new Store({
    activeRequests: 0,
    replyingTo: undefined,
    quoting: undefined
})
