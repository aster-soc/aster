import type {RefObject} from "react";
import * as React from "react";
import {useNavigate} from "@tanstack/react-router";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    wide?: boolean;
    thin?: boolean;
    center?: boolean;
    primary?: boolean;
    danger?: boolean;
    nav?: boolean;
    to?: string;
    ref?: RefObject<HTMLButtonElement | HTMLAnchorElement | null>;
    children: React.ReactNode;
}

function Button({wide, thin, center, primary, danger, nav, to, ref, children, ...props}: ButtonProps) {
    const navigate = useNavigate();

    if (to !== undefined) {
        props.onClick = (e) => {
            e.preventDefault() // annoying hack
            navigate({to: to})
        }

        return (
            <a
                href={to}
                className={`button${wide ? " wide" : ""}${thin ? " thin" : ""}${center ? " center" : ""}${primary ? " primary" : ""}${danger ? " danger" : ""}${nav ? " nav" : ""}`}
                ref={ref}
                {...props}
            >
                {children}
            </a>
        )
    }

    return (
        <button
            className={`button${wide ? " wide" : ""}${thin ? " thin" : ""}${center ? " center" : ""}${primary ? " primary" : ""}${danger ? " danger" : ""}${nav ? " nav" : ""}`}
            ref={ref}
            {...props}
        >
            {children}
        </button>
    )
}

export default Button;
