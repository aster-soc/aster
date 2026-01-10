import {type Dispatch, type ReactNode, type SetStateAction, useEffect, useState} from "react";
import {createRef} from "preact";
import "./Modal.scss";
import Container from "../Container.tsx";

function Modal({title, children, show = false, setShow, actions = undefined}: {
	title: string;
	children: ReactNode;
	show: boolean;
	setShow: Dispatch<SetStateAction<boolean>>;
	actions?: ReactNode;
}) {
	const dialog = createRef<HTMLDialogElement>()

	useEffect(() => {
		if (show && !dialog.current?.open) dialog.current?.showModal()
		if (!show && dialog.current?.open) dialog.current?.close()
	}, [show])

	return (
		<>
			<dialog
				ref={dialog}
				className={"modal"}
				onClose={() => setShow(false)}
			>
				<h1>{title}</h1>
				{children}
				{actions ? (
					<div className={"actions"}>
						{actions}
					</div>
				) : null}
			</dialog>
			{show ? (
				<div className={"modalBackdrop"} onClick={() => dialog.current?.close()}></div>
			): null}
		</>
	)
}

export default Modal;
