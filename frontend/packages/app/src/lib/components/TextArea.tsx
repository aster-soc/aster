import * as React from "react";

interface TextAreaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
    label?: string;
    wide?: boolean;
}

function TextArea(
    {label, wide, ...props}:
    TextAreaProps
) {
    return (
        <div>
            {label ? (
                <label className={`inputLabel ${wide ? "wide" : ""}`}>{label}</label>
            ) : null}
            <textarea className={`input ${wide ? "wide" : ""}`} {...props} />
        </div>
    )
}

export default TextArea
