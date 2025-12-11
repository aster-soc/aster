import './Mfm.scss'
import * as React from "react";
import * as Common from 'aster-common'
import {MfmKt, MfmMention, type MfmNode, MfmText} from 'mfmkt';

function Mfm(
    {text, simple = false, emoji = []}:
    { text?: string, simple?: boolean, emoji?: any[] }
) {
    let result: Array<React.ReactNode> = []

    React.useEffect(() => {
        render()
    })

    render()

    function render() {
        if (text === undefined) return undefined

        let mfmTree = MfmKt.parse(text);
        console.debug('[Mfm Tree]', mfmTree)

        renderChildren(mfmTree)
    }

    function renderChildren(children: MfmNode[], depth: number = 0) {
        for (const node of children) {
            renderElement(node, depth)
            if ('children' in node) renderChildren(node.children, depth + 1)
        }
    }

    function renderElement(e: MfmNode, depth: number = 0) {
        if (e instanceof MfmText) {
            result.push(
                <span dangerouslySetInnerHTML={{__html: e.toString()}}/>
            )
            if ('children' in e) renderChildren(e.children, depth + 1)
        } else if (e instanceof MfmMention) {
            result.push(
                <a href={"/" + Common.renderHandle({username: e.username, host: e.host})}>
                    {e.toString()}
                </a>
            )
            if ('children' in e) renderChildren(e.children, depth + 1)
        } else {
            result.push(
                <span dangerouslySetInnerHTML={{__html: e.toString()}}/>
            )
            if ('children' in e) renderChildren(e.children, depth + 1)
        }
    }


    return (
        <>
            {result}
        </>
    )
}

export default Mfm
