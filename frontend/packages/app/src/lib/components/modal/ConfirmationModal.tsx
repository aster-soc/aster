import type {Dispatch, SetStateAction} from "react";
import Modal from "./Modal.tsx";
import Container from "../Container.tsx";
import Button from "../Button.tsx";

function ConfirmationModal({title, body = "Are you sure you want to do this?", action, show, setShow}: {
	title: string;
	body: string;
	action: () => void;
	show: boolean;
	setShow: Dispatch<SetStateAction<boolean>>;
}) {
	return (
		<Modal
			title={title}
			show={show}
			setShow={setShow}
		>
			<Container gap={"md"}>
				<p>{body}</p>
				<Container gap={"md"} align={"horizontal"}>
					<Button primary onClick={() => {
						setShow(false); action()
					}}>Continue</Button>
					<Button onClick={() => setShow(false)}>Cancel</Button>
				</Container>
			</Container>
		</Modal>
	)
}

export default ConfirmationModal;
