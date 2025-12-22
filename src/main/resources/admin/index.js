console.log("Loaded admin panel script")

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
