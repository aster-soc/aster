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
    circle?: boolean;
    collapse?: boolean;
    to?: string;
    realLink?: boolean;
    ref?: RefObject<HTMLButtonElement | HTMLAnchorElement | null>;
    children: React.ReactNode;
}

function Button({
                    wide,
                    thin,
                    center,
                    primary,
                    danger,
                    nav,
                    circle,
                    collapse,
                    to,
                    realLink,
                    ref,
                    children,
                    ...props
                }: ButtonProps) {
    const navigate = useNavigate();

    function renderClasses() {
        return `button${wide ? " wide" : ""}${thin ? " thin" : ""}${center ? " center" : ""}${primary ? " primary" : ""}${danger ? " danger" : ""}${nav ? " nav" : ""}${circle ? " circle" : ""}${collapse ? " collapse" : ""}`
    }

    if (to !== undefined) {
        if (!realLink) {
            props.onClick = (e) => {
                e.preventDefault() // annoying hack
                navigate({to: to})
            }
        }

        return (
            <a
                href={to}
                className={renderClasses()}
                ref={ref}
                {...props}
            >
                {children}
            </a>
        )
    }

    return (
        <button
            className={renderClasses()}
            ref={ref}
            {...props}
        >
            {children}
        </button>
    )
}

export default Button;
