import * as Common from 'aster-common';
import Container from "./Container.tsx";

function NoteSimple(
    {data}: { data: Common.SmallNote }
) {
    return (
        <Container>
            {JSON.stringify(data)}
        </Container>
    )
}

export default NoteSimple
