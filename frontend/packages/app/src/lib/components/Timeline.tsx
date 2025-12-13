import * as React from "react";
import {useRef} from "react";
import './Timeline.scss';
import {randomString} from "aster-common"
import type {DefinedUseInfiniteQueryResult} from "@tanstack/react-query";
import Container from "./Container";
import Loading from "./Loading.tsx";

function Timeline(
    {query, Component, grid = false}:
    { query?: DefinedUseInfiniteQueryResult<any>, Component: any, grid?: boolean }
) {
    const intersectionRef = useRef(null)
    const intersectionId = randomString()

    let noMore = false

    React.useEffect(() => {
        const observer = new IntersectionObserver(async (entries) => {
            if (!noMore)
                for (const entry of entries) {
                    console.log(entry)
                    if (entry.isIntersecting) {
                        query?.fetchNextPage()
                        break
                    }
                }
        }, {
            threshold: 0.8
        });

        if (intersectionRef.current) observer.observe(intersectionRef.current);
    }, intersectionRef)

    function renderBaseTimeline() {
        if (query?.data && query.data?.pages && query.data.pages.length > 0) {
            return (
                query?.data?.pages?.map((items) => (
                    (items && items.length > 0) ?
                        items?.map((item) => (
                            (item ? <Component data={item}
                                               key={"TimelineChild-" + Component.name + "-" + (item?.id ? item?.id : "r" + randomString())}>
                            </Component> : null)
                        )) : renderNone()
                ))
            )
        } else {
            return renderNone()
        }
    }

    function renderNone() {
        noMore = true
        return (
            <Container align={"center"} padding={"12px 0"}>
                <span className={"notice"}>Nothing more to show...</span>
            </Container>
        )
    }

    return <>
        <div className={`timeline`}>
            {grid ? <Container gap={"md"} align={"startHorizontal"} wrap>
                {renderBaseTimeline()}
            </Container> : renderBaseTimeline()}
        </div>
        <div id={"intersection-" + intersectionId} ref={intersectionRef}>
            <Container align={"center"} padding={"12px 0"}>
                {query.isFetchingNextPage ? <Loading/> : null}
            </Container>
        </div>
    </>
}

export default Timeline
