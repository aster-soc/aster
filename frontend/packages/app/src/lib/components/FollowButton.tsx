import Button from "./Button.tsx";
import {useQuery, type UseQueryResult} from "@tanstack/react-query";
import Loading from "./Loading.tsx";
import {Api, type Nullable, type Relationship} from 'aster-common'

function FollowButton({id, query}: { id: string, query?: UseQueryResult<Nullable<Relationship>, Error> }) {
    if (query === undefined) query = useQuery({
		queryKey: [`relationship_${id}`],
		queryFn: () => Api.getUserRelationship(id),
	});

    return (
        <Button onClick={() => {
            Api.followUser(id).then(() => query.refetch());
        }}>
            {query?.isPending || query?.isFetching || query?.error ? (
                <Loading/>
            ) : (
				query?.data?.to && !query?.data?.to?.pending ? ("Unfollow") :
					query?.data?.to && query?.data?.to?.pending ? ("Cancel follow") :
                        ("Follow")
            )}
        </Button>
    )
}

export default FollowButton;
