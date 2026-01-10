import { createFileRoute } from '@tanstack/react-router'
import PageHeader from "../lib/components/PageHeader.tsx";
import {IconPencil} from "@tabler/icons-react";
import PageWrapper from "../lib/components/PageWrapper.tsx";
import Compose from "../lib/components/Compose.tsx";

export const Route = createFileRoute('/compose')({
  component: RouteComponent,
})

function RouteComponent() {
	return (
		<>
			<PageHeader
				icon={<IconPencil size={18}/>}
				title={"Compose"}
			/>
			<PageWrapper padding={"full"}>
				<Compose />
			</PageWrapper>
		</>
	)
}
