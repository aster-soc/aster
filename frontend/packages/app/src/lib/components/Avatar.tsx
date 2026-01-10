import * as Common from 'aster-common'
import './Avatar.scss'
import {useNavigate} from "@tanstack/react-router";
import localstore from "../utils/localstore.ts";
import {decodeBlurHash, getBlurHashAverageColor} from "fast-blurhash";
import {createRef} from "preact";
import {useEffect, useRef, useState} from "react";

function Avatar(
    {user, size}:
    { user: any, size?: undefined | 'xl' | 'lg' | 'md' | 'sm' }
) {
    const navigate = useNavigate();
	const [useFallback, setUseFallback] = useState(false);

    let fallback = "/assets/img/avatar.png"

	let sizePx = 45;
	switch (size) {
		case 'xl': sizePx = 55; break;
		case 'lg': sizePx = 50; break;
		case 'md': sizePx = 35; break;
		case 'sm': sizePx = 25; break;
	}

	const canvasRef = createRef<HTMLCanvasElement>()

	useEffect(() => {
		if (!user.avatarBlurHash) return

		try {
			const decoded = decodeBlurHash(user.avatarBlurHash, sizePx, sizePx)
			const ctx = canvasRef.current?.getContext('2d');
			const imageData = ctx?.createImageData(sizePx, sizePx);
			imageData.data.set(decoded);
			ctx.putImageData(imageData, 0, 0);
		} catch (_) {}
	})

	function render() {
		if (useFallback) {
			return (
				<canvas ref={canvasRef} width={sizePx} height={sizePx} />
			)
		} else {
			return (
				<img
					src={user?.avatar ?? fallback}
					alt={user?.avatarAlt ?? `${user.username}'s avatar`}
					onError={() => setUseFallback(true)}
				/>
			)
		}
	}

    return (
        <div className={`avatarCtn`}>
            <div
                className={`avatar ${size ?? ""} highlightable${localstore.getParsed("rounded_avatars") ? " rounded" : ""}`}
                onClick={() => navigate({to: `/${Common.renderHandle(user)}`})}
            >
				{render()}
            </div>
        </div>
    )
}

export default Avatar
