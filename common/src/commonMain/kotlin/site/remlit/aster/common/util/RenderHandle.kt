package site.remlit.aster.common.util

import site.remlit.aster.common.model.SmallUser
import site.remlit.aster.common.model.User
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Renders a handle of a user (e.g. @user@example.com, or if local @user)
 *
 * @param user User to render handle of
 *
 * @return Rendered handle
 * */
@JsExport
fun renderHandle(user: User?): String = if (user == null) "" else
	"@${user.username}${if (user.host != null) "@${user.host}" else ""}"

/**
 * Renders a handle of a user (e.g. @user@example.com, or if local @user)
 *
 * @param user User to render handle of
 *
 * @return Rendered handle
 * */
@JsExport
@JsName("renderHandleSmall")
fun renderHandle(user: SmallUser?): String = if (user == null) "" else
	"@${user.username}${if (user.host != null) "@${user.host}" else ""}"
