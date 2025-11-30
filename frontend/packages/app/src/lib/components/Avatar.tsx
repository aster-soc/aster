import * as Common from 'aster-common'
import './Avatar.scss'
import {useNavigate} from "@tanstack/react-router";
import localstore from "../utils/localstore.ts";

function Avatar(
    {user, size}:
    { user: any, size?: undefined | 'xl' | 'lg' | 'md' | 'sm' }
) {
    const navigate = useNavigate();

    let fallback = "/assets/img/avatar.png"

    return (
        <div className={`avatarCtn`}>
            <div
                className={`avatar ${size ?? ""} highlightable${localstore.getParsed("rounded_avatars") ? " rounded" : ""}`}
                onClick={() => navigate({to: `/${Common.renderHandle(user)}`})}
            >
                <img src={user?.avatar ?? fallback} alt={user?.avatarAlt ?? `${user.username}'s avatar`}
                     onError={e => e.currentTarget.src = fallback}/>
            </div>
        </div>
    )
}

export default Avatar
