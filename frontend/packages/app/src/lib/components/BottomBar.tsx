import "./BottomBar.scss"
import Button from "./Button.tsx";
import {IconBell, IconHome, IconMenu2, IconPencil, IconSearch} from "@tabler/icons-react";
import {useRouterState} from "@tanstack/react-router";

function BottomBar() {
    const pathname = useRouterState().location.pathname;

    return (
        <div className={"bottomBar"}>
            <Button nav circle>
                <IconMenu2 size={22}/>
            </Button>
            <Button nav circle primary={pathname === "/"} to={"/"}>
                <IconHome size={22}/>
            </Button>
            <Button nav circle primary={pathname === "/notifications"} to={"/notifications"}>
                <IconBell size={22}/>
            </Button>
            <Button nav circle primary={pathname === "/search"} to={"/search"}>
                <IconSearch size={22}/>
            </Button>
            <Button nav circle primary={pathname === "/compose"} to={"/compose"}>
                <IconPencil size={22}/>
            </Button>
        </div>
    )
}

export default BottomBar
