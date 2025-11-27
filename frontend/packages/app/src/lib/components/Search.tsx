import * as Common from 'aster-common'
import Note from "./Note.tsx";
import UserCard from "./UserCard.tsx";

function Search({data}: { data: Common.SearchResults }) {
    if ("note" in data) {
        return (
            <Note data={data.note}/>
        )
    } else if ("user" in data) {
        return (
            <UserCard data={data.user}/>
        )
    }
}

export default Search
