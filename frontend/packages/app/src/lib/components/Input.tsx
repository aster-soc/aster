import * as React from "react";
import './Input.scss';
import * as Common from 'aster-common'

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    wide?: boolean;
}

function Input(
    {label, wide, ...props}:
    InputProps
) {
    const id = Common.randomString()

    return (
        <div className={"inputCtn " + props.type}>
            {props.type === "checkbox" ? (
                <>
                    <input id={id} className={`input ${wide ? "wide" : ""}`} {...props} />
                    {label ? (
                        <label for={id} className={`inputLabel ${wide ? "wide" : ""}`}>{label}</label>
                    ) : null}
                </>
            ) : (
                <>
                    {label ? (
                        <label for={id} className={`inputLabel ${wide ? "wide" : ""}`}>{label}</label>
                    ) : null}
                    <input id={id} className={`input ${wide ? "wide" : ""}`} {...props} />
                </>
            )}
        </div>
    )
}

export default Input
