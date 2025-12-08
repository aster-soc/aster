import Button from "./Button.tsx";
import {useQuery} from "@tanstack/react-query";
import Loading from "./Loading.tsx";
import {Api} from 'aster-common'

function FollowButton({id}: { id: string }) {
    const {data, error, isPending, isFetching, refetch} = useQuery({
        queryKey: [`relationship_${id}`],
        queryFn: () => Api.getUserRelationship(id),
    });

    return (
        <Button onClick={() => {
            Api.followUser(id).then(() => refetch());
        }}>
            {isPending || isFetching || error ? (
                <Loading/>
            ) : (
                data?.to && !data?.to?.pending ? ("Unfollow") :
                    data?.to && data?.to?.pending ? ("Cancel follow") :
                        ("Follow")
            )}
        </Button>
    )
}

export default FollowButton;
