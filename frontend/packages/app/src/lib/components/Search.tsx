import * as Common from 'aster-common'

function Search({data}: { data: Common.SearchResults }) {
    return (
        <p>{JSON.stringify(data)}</p>
    )
}

export default Search
