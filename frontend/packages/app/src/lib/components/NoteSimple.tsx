import * as Common from 'aster-common';
import Container from "./Container.tsx";
import Avatar from "./Avatar.tsx";
import Visibility from "./Visibility.tsx";
import DateTime from "./DateTime.tsx";
import Mfm from "./Mfm.tsx";
import "./NoteSimple.scss";

function NoteSimple(
    {data}: { data: Common.SmallNote | Common.Note }
) {
    return (
        <div className={"noteSimple"}>
            <Container gap={"md"}>
                <Container align={"horizontal"}>
                    <Container gap={"md"} align={"horizontal"}>
                        <Avatar size={"sm"} user={data.user}/>
                        <Container gap={"sm"} align={"horizontal"}>
                            <b>{data.user.displayName ?? data.user.username}</b>
                            <span>{Common.renderHandleSmall(data.user)}</span>
                        </Container>
                    </Container>

                    <Container gap={"md"} align={"horizontalRight"}>
                        <Visibility visibility={data.visibility}/>
                        <DateTime date={data.createdAt} short/>
                    </Container>
                </Container>

                <Mfm text={data.content} simple/>
            </Container>
        </div>
    )
}

export default NoteSimple
