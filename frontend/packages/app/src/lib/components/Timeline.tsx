import * as React from "react";
import './Timeline.scss';
import Container from "./Container.tsx";

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
            {timeline.length > 0 ? timeline : (
                <Container align={"center"} padding={"12px 0"}>
                    <span className={"notice"}>Nothing to show...</span>
                </Container>
            )}
        </div>
    )
}

export default Timeline
