import * as Common from 'aster-common'
import Container from "./Container.tsx";
import Avatar from "./Avatar.tsx";

function UserCard(
    {data}: { data: Common.SmallUser }
) {
    return (
        <Container gap={"md"} align={"horizontal"}>
            <Avatar user={data} size="md"/>
            <Container>
                <span>{data.displayName ?? data.username}</span>
                <span>@{data.username}@{data.host}</span>
            </Container>
        </Container>
    )
}

export default UserCard;
