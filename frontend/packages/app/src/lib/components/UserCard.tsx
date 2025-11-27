import * as Common from 'aster-common'
import Container from "./Container.tsx";
import Avatar from "./Avatar.tsx";
import './UserCard.scss'
import {useNavigate} from "@tanstack/react-router";

function UserCard(
    {data, padded = false}: { data: Common.SmallUser | Common.User, padded?: boolean }
) {
    const navigate = useNavigate();

    return (
        <div
            className={"userCard" + (padded ? " padded" : "")}
            onClick={() => navigate({to: `/@${data.username}${data.host ? "@" + data.host : ""}`})}
        >
            <Container gap={"md"} align={"horizontal"}>
                <Avatar user={data} size="md"/>
                <Container>
                    <span>{data.displayName ?? data.username}</span>
                    <span>@{data.username}@{data.host}</span>
                </Container>
            </Container>
        </div>
    )
}

export default UserCard;
