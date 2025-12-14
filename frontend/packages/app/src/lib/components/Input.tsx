import * as React from "react";
import * as Common from 'aster-common'
import localstore from "../utils/localstore.ts";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    wide?: boolean;
    setting?: string;
}

function Input(
    {label, wide, setting, ...props}:
    InputProps
) {
    const id = Common.randomString()
    const [value, setValue] = React.useState(false);

    function refreshValue() {
        setValue(localstore.getParsed(setting));
    }

    refreshValue()

    function renderLabel() {
        return label ? (
            <label for={id} className={`inputLabel ${wide ? "wide" : ""}`}>{label}</label>
        ) : null
    }

    function renderInput() {
        if (setting && props.type === "checkbox") {
            return <input
                id={id}
                className={`input${wide ? " wide" : ""}`}
                checked={value}
                onChange={() => {
                    localstore.set(setting, !value)
                    refreshValue()
                }}
                {...props}
            />
        } else {
            return <input
                id={id}
                className={`input${wide ? " wide" : ""}`}
                {...props}
            />
        }
    }

    return (
        <div className={"inputCtn " + props.type}>
            {props.type === "checkbox" ? (
                <>
                    {renderInput()}
                    {renderLabel()}
                </>
            ) : (
                <>
                    {renderLabel()}
                    {renderInput()}
                </>
            )}
        </div>
    )
}

export default Input
