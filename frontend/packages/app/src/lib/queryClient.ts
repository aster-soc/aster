import {QueryClient} from '@tanstack/react-query'

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 2,
            retryDelay: 5000,
            refetchOnWindowFocus: false
        }
    }
});

export default queryClient;
