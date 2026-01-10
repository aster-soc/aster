import {type Dispatch, type ReactNode, type SetStateAction, useEffect, useState} from "react";
import {createRef} from "preact";
import "./Modal.scss";

function Modal({title, children, show, setShow}: {
	title: string;
	children: ReactNode;
	show: boolean;
	setShow: Dispatch<SetStateAction<boolean>>;
}) {
	const dialog = createRef<HTMLDialogElement>()

	useEffect(() => {
		if (show) dialog.current?.showModal()
		if (!show) dialog.current?.close()
	}, [show])

	return (
		<dialog
			ref={dialog}
			className={"modal"}
		>

		</dialog>
	)
}

export default Modal;
