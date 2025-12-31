import "./NoteAttachments.scss"
import * as Common from 'aster-common'
import {IconFile} from "@tabler/icons-react";

function NoteAttachments({ attachments }: { attachments: Common.NoteAttachment[] }) {
	function renderAttachment(attachment) {
		if (attachment.type.startsWith("image")) {
			return (
				<div className={"attachment"}>
					<img src={attachment.src} alt={attachment.alt} />
				</div>
			)
		} else if (attachment.type.startsWith("video")) {
			return (
				<div className={"attachment"}>
					<video src={attachment.src} aria-label={attachment.alt} />
				</div>
			)
		} else if (attachment.type.startsWith("audio")) {
			return (
				<div className={"attachment"}>
					<audio src={attachment.src} aria-label={attachment.alt} />
				</div>
			)
		} else {
			return (
				<div className={"attachment"}>
					<IconFile size={24} />
				</div>
			)
		}
	}

	if (attachments.length === 0) {
		return null
	}

	return (
		<div className={"attachments"}>
			{attachments.map((attachment) => renderAttachment(attachment))}
		</div>
	)
}

export default NoteAttachments
