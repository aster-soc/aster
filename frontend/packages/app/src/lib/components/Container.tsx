import * as React from "react";
import './Container.scss'

function Container(
    {gap, align, border, padding, clazz, fill, children, ...props}:
    {
        gap?: "sm" | "md" | "lg" | "xl" | undefined,
        align?: 'center' | 'left' | 'right' | 'horizontal' | 'horizontalCenter' | 'horizontalRight' | 'startHorizontal',
        border?: "top" | "bottom" | undefined,
        padding?: string | undefined,
        clazz?: string,
        fill?: boolean,
        children: React.ReactNode,
        props?: never
    }
) {
    return (
        <div
            className={`container${gap ? " gap-" + gap : ""}${align ? " " + align : ""}${border ? " border-" + border : ""}${clazz ? " " + clazz : ""}${fill ? " fill" : ""}`}
            style={padding ? `padding:${padding};` : ""}
            {...props}
        >
            {children}
        </div>
    )
}

export default Container
