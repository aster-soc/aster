import Button from "../Button.tsx";
import './Navigation.scss'
import {
    IconBell,
    IconBookmark,
    IconDashboard,
    IconDots,
    IconFolder,
    IconHome,
    IconSearch,
    IconSettings,
    IconUserPlus
} from "@tabler/icons-react";
import {useRouterState} from "@tanstack/react-router";
import localstore from "../../utils/localstore.ts";

function NavigationWidget() {
    const pathname = useRouterState().location.pathname;

	let self = localstore.getSelf()

    return (
        <ul className={`widget-navigation widget padded`}>
            <li>
                <Button wide nav collapse primary={pathname === "/"} to={'/'}>
                    <IconHome size={18}/>
                    Home
                </Button>
            </li>
			{self ? <>
				<li>
					<Button wide nav collapse primary={pathname === "/notifications"} to={'/notifications'}>
						<IconBell size={18}/>
						Notifications
					</Button>
				</li>
				<li>
					<Button wide nav collapse primary={pathname === "/follow-requests"} to={'/follow-requests'}>
						<IconUserPlus size={18}/>
						Follow Requests
					</Button>
				</li>
			</> : null}
            <br/>
			{self ? <>
				<li>
					<Button wide nav collapse primary={pathname === "/search"} to={'/search'}>
						<IconSearch size={18}/>
						Search
					</Button>
				</li>
				<li>
					<Button wide nav collapse primary={pathname === "/bookmarks"} to={'/bookmarks'}>
						<IconBookmark size={18}/>
						Bookmarks
					</Button>
				</li>
				<li>
					<Button wide nav collapse primary={pathname === "/drive"} to={'/drive'}>
						<IconFolder size={18}/>
						Drive
					</Button>
				</li>
				<br/>
			</> : null}
            <li>
                <Button wide nav collapse>
                    <IconDots size={18}/>
                    More
                </Button>
            </li>
			{self ? <>
				<li>
					<Button wide nav collapse primary={pathname === "/admin"} to={'/admin'} realLink>
						<IconDashboard size={18}/>
						Dashboard
					</Button>
				</li>
			</>: null}
            <li>
                <Button wide nav collapse primary={pathname.startsWith("/settings")} to={'/settings'}>
                    <IconSettings size={18}/>
                    Settings
                </Button>
            </li>
        </ul>
    )
}

export default NavigationWidget;
