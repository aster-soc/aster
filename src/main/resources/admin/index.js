console.log("Loaded admin panel script")

window.addEventListener("load", () => {
	registerSearchListener()
})

function registerSearchListener() {
	const searchForm = document.getElementById("admin-list-nav-search");
	if (searchForm) {
		console.log("Search form on page, adding event listener.");
		searchForm.addEventListener("submit", event => {
			event.preventDefault();

			const formData = new FormData(searchForm)
			const query = formData.get("query");

			console.log(`Searching for: ${query}`)
			window.location.href = window.location.pathname + `?q=${query}`
		})
	}
}

function reloadPlugins() {
    fetch("/api/plugins/reload", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("aster_token")
        }
    }).then(() => {
        window.location.reload()
    })
}
