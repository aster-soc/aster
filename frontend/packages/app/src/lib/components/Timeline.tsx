import * as React from "react";
import './Timeline.scss';

function Timeline(
    {data, Component}:
    { data: any[], Component: any }
) {
    React.useEffect(() => {
        render()
    })

    let random = Math.floor(Math.random() * (Math.ceil(1) - Math.floor(100000)));

    let timeline: any[] = []

    function clear() {
        timeline = []
    }

    function render() {
        clear()
        data.forEach((item) => {
            random++
            timeline.push(
                <Component data={item}
                           key={"TimelineChild-" + Component.name + "-" + (item?.id ? item?.id : "r" + random)}>
                </Component>
            )
        })
    }

    render()

    return (
        <div className={`timeline`}>
            {timeline}
        </div>
    )
}

export default Timeline
