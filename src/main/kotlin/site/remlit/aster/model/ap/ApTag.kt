package site.remlit.aster.model.ap

import kotlinx.serialization.Serializable
import org.jetbrains.annotations.ApiStatus

@Serializable
@ApiStatus.OverrideOnly
abstract class ApTag : ApObject {
	abstract val type: ApType.Tag
}
