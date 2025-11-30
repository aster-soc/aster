import * as React from "react";
import './Avatar.scss'
import * as Common from 'aster-common'

function Avatar(
    {user, size}:
    { user: any, size?: undefined | 'xl' | 'lg' | 'md' | 'sm' }
) {
    React.useEffect(() => {
    })

    let fallback = "/assets/img/avatar.png"

    return (
        <div className={`avatarCtn`}>
            <a href={`/${Common.renderHandle(user)}`}
               className={`avatar ${size ?? ""} highlightable`}>
                <img src={user?.avatar ?? fallback} alt={user?.avatarAlt ?? `${user.username}'s avatar`}
                     onError={e => e.currentTarget.src = fallback}/>
            </a>
        </div>
    )
}

export default Avatar
