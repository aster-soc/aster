import './Mfm.scss'

function Mfm(
    {text, simple = false, emoji = []}:
    { text?: string, simple?: boolean, emoji?: any[] }
) {
    return (
        <>
            {text}
        </>
    )
}

export default Mfm
