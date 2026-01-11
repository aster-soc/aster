function UserPage_Notes({withReplies, media}: {
	withReplies?: boolean, media?: boolean
}) {
	if (media) {
		return (
			<p>Media</p>
		)
	} else {
		return (
			<p>Notes{withReplies ? " with Replies" : ""}</p>
		)
	}
}

export default UserPage_Notes
