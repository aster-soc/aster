import {createFileRoute} from "@tanstack/react-router";
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconUserPlus} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import {useInfiniteQuery} from "@tanstack/react-query";
import localstore from "../lib/utils/localstore.ts";
import Loading from "../lib/components/Loading.tsx";
import Error from "../lib/components/Error.tsx";
import {Api} from 'aster-common'
import Timeline from "../lib/components/Timeline.tsx";
import FollowRequest from "../lib/components/FollowRequest.tsx";

export const Route = createFileRoute('/follow-requests')({
	component: RouteComponent,
})

function RouteComponent() {
	const query = useInfiniteQuery({
		queryKey: [`follow-requests_${localstore.getSelf()?.id}`],
		queryFn: ({pageParam}) => Api.getFollowRequests(pageParam),
		initialPageParam: undefined,
		getNextPageParam: (lastPage) => {
			return lastPage ? lastPage?.at(-1)?.createdAt : undefined
		}
	});

	return (
		<>
			<PageHeader icon={<IconUserPlus size={18}/>} title={"Follow Requests"}/>
			<PageWrapper padding={"full"} center={false}>
				{query.isPending ? (
					<Loading fill/>
				) : query.error ? (
					<Error error={query.error} retry={query.refetch}/>
				) : (
					<Timeline query={query} Component={FollowRequest}/>
				)}
			</PageWrapper>
		</>
	)
}
