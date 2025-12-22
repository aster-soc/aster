import * as React from "react";
import {type RefObject} from "react";
import './Dropdown.scss'
import {autoUpdate, flip, offset, shift, useFloating} from "@floating-ui/react-dom";
import {useNavigate} from "@tanstack/react-router";

export interface DropdownNode {
}

export class DropdownDivider implements DropdownNode {
}

export type DropdownItemType = "danger" | undefined

// i hate this evil language
export class DropdownItem implements DropdownNode {
    type?: DropdownItemType = undefined
    icon?: React.ReactNode = undefined
    title?: string = undefined
    to?: string = undefined

    constructor(type: DropdownItemType, icon: React.ReactNode, title: string, to?: string, onClick?: () => void) {
        this.type = type;
        this.icon = icon;
        this.title = title;
        this.to = to;
        this.onClick = onClick
    }

    onClick?: () => void =
        () => {
        }
}

function Dropdown(
    {items, parent, open, setOpen}:
    {
        items: DropdownNode[],
        parent: RefObject<any>,
        open: boolean,
        setOpen: (open: boolean) => void
    }
) {
    const navigate = useNavigate();
    const {refs, floatingStyles} = useFloating({
        middleware: [
            offset(10),
            shift(),
            flip()
        ],
        whileElementsMounted: autoUpdate
    });

    React.useEffect(() => {
        render()
        refs.setReference(parent.current)
    })

    let renderedItems: React.ReactNode[] = []

    let random = Math.floor(Math.random() * (Math.ceil(1) - Math.floor(100000)));

    function clear() {
        renderedItems = []
    }

    function render() {
        clear()
        items.forEach((item: DropdownNode) => {
            random++
            if (item instanceof DropdownItem) {
                renderedItems.push(
                    <div className={`item${(item.type) ? " " + item.type : ""}`} key={"DropdownChild-Item-" + random}
                         onClick={() => {
                             if (item.to && item.to.startsWith("http")) window.open(item.to, "_blank")
                             if (item.to && !item.to.startsWith("http")) navigate({to: item.to})

                             if (item.onClick) {
                                 item.onClick()
                                 setOpen(false)
                             }
                         }} role={"button"}
                         tabIndex={0}>
                        {item.icon}
                        {item.title}
                    </div>
                )
            } else if (item instanceof DropdownDivider) {
                renderedItems.push(
                    <hr key={"DropdownChild-Divider-" + random}/>
                )
            }
        })
    }

    render()

    return (
        <>
            <div ref={refs.setFloating} style={floatingStyles} className={`dropdown${(open) ? " open" : ""}`}
                 role={"menu"}
                 aria-label={"Dropdown menu"}>
                {renderedItems}
            </div>
            <div className={`dropdownCatcher${(open) ? " open" : ""}`} onClick={() => setOpen(false)}></div>
        </>
    )
}

export default Dropdown
