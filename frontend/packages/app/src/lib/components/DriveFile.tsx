import * as common from "aster-common";
import {Api} from "aster-common";
import './DriveFile.scss'
import Container from "./Container.tsx";
import Button from "./Button.tsx";
import {
    IconCoffee,
    IconFile,
    IconFileZip,
    IconPencil,
    IconQuestionMark,
    IconTrash,
    IconVideo
} from "@tabler/icons-react";
import * as React from "react";

function DriveFile({data}: { data: common.DriveFile }) {
    const [hidden, setHidden] = React.useState(false);

    function renderPreview() {
        const type = data.type
        if (type.startsWith("image")) {
            return <img src={data.src} alt={data.alt}/>
        } else if (type.startsWith("video")) {
            return <div className={"preview"}>
                <IconVideo size={30}/>
            </div>
        } else if (type.startsWith("application/zip")) {
            return <div className={"preview"}>
                <IconFileZip size={30}/>
            </div>
        } else if (type.startsWith("application/java-archive")) {
            return <div className={"preview"}>
                <IconCoffee size={30}/>
            </div>
        } else if (type.startsWith("application") || type.startsWith("text")) {
            return <div className={"preview"}>
                <IconFile size={30}/>
            </div>
        } else {
            return <div className={"preview"}>
                <IconQuestionMark size={30}/>
            </div>
        }
    }

    function renderName() {
        let split = data?.src?.split("/")
        return <span className={"name"}>{split[split.length - 1]}</span>
    }

    return hidden ? null : (
        <div className={"driveFile"}>
            {renderPreview()}
            {renderName()}
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
