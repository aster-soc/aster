import * as common from "aster-common";
import {Api} from "aster-common";
import './DriveFile.scss'
import Container from "./Container.tsx";
import Button from "./Button.tsx";
import {IconPencil, IconTrash} from "@tabler/icons-react";
import * as React from "react";

function DriveFile({data}: { data: common.DriveFile }) {
    const [hidden, setHidden] = React.useState(false);

    return hidden ? null : (
        <div className={"driveFile"}>
            <img src={data.src} alt={data.alt}/>
            <Container gap={"md"} align={"horizontalCenter"}>
                <Button onClick={() => alert("TODO: Edit")}>
                    <IconPencil size={18}/>
                </Button>
                <Button danger onClick={() => Api.deleteDriveFile(data?.id).then(() => setHidden(true))}>
                    <IconTrash size={18}/>
                </Button>
            </Container>
        </div>
    )
}

export default DriveFile;
