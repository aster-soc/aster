import * as React from "react";
import './Container.scss'

function Container(
    {gap, align, border, padding, clazz, children, ...props}:
    {
        gap?: "sm" | "md" | "lg" | "xl" | undefined,
        align?: 'center' | 'left' | 'right' | 'horizontal' | 'horizontalCenter',
        border?: "top" | "bottom" | undefined,
        padding?: string | undefined,
        clazz?: string,
        children: React.ReactNode,
        props?: never
    }
) {
    return (
        <div
            className={`container${gap ? " gap-" + gap : ""}${align ? " " + align : ""}${border ? " border-" + border : ""}${clazz ? " " + clazz : ""}`}
            style={padding ? `padding:${padding};` : ""}
            {...props}
        >
            {children}
        </div>
    )
}

export default Container
