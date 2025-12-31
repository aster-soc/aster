import * as Common from 'aster-common';
import {Api} from 'aster-common';
import Avatar from "./Avatar.tsx";
import Container from "./Container.tsx";
import Button from "./Button.tsx";
import {IconCheck, IconX} from "@tabler/icons-react";
import "./FollowRequest.scss"
import alert, {Alert, AlertType} from "../utils/alert.ts";

function FollowRequest({ data }: { data: Common.Relationship}) {
	return (
		<div className={"followRequest"}>
			<Avatar user={data.from} />
			<Container align={"left"}>
				<b>{data.from.displayName ?? data.from.username}</b>
				<span>{Common.renderHandle(data.from)}</span>
			</Container>
			<Container align={"horizontalRight"} gap={"md"}>
				<Button primary onClick={() => Api.acceptFollowRequest(data.id).then(() => {
					alert.add(new Alert("", AlertType.Success, "Accepted follow request"));
				})}>
					<IconCheck size={18} />
					Accept
				</Button>
				<Button danger onClick={() => Api.rejectFollowRequest(data.id).then(() => {
					alert.add(new Alert("", AlertType.Success, "Rejected follow request"));
				})}>
					<IconX size={18} />
					Reject
				</Button>
			</Container>
		</div>
	)
}

export default FollowRequest;
