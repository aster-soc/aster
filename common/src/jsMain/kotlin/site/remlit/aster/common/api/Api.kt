package site.remlit.aster.common.api

import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.Notification
import site.remlit.aster.common.model.SearchResults
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.common.model.response.AuthResponse
import site.remlit.aster.common.util.Https
import site.remlit.aster.common.util.toObject

@JsExport
@Suppress("UtilityClassWithPublicConstructor", "Unused")
@OptIn(ExperimentalJsStatic::class)
class Api {
	companion object {
		@JsStatic
		fun register(username: String, password: String, invite: String?): AuthResponse? =
			Https.post(
				"/api/register",
				false,
				mapOf(
					"username" to username,
					"password" to password,
					"invite" to invite
				).toObject()
			).unsafeCast<AuthResponse?>()

		@JsStatic
		fun login(username: String, password: String): AuthResponse? =
			Https.post(
				"/api/login",
				false,
				mapOf(
					"username" to username,
					"password" to password
				).toObject()
			).unsafeCast<AuthResponse?>()

		@JsStatic
		fun passwordReset(code: String, password: String) =
			Https.post(
				"/api/password-reset",
				false,
				mapOf(
					"code" to code,
					"password" to password
				).toObject()
			)

		@JsStatic
		fun getTimeline(timeline: String): List<Note>? =
			Https.get("/api/timeline/$timeline", true)
				.unsafeCast<List<Note>?>()

		@JsStatic
		fun getBookmarks(): List<Note>? =
			Https.get("/api/bookmarks", true)
				.unsafeCast<List<Note>?>()

		@JsStatic
		fun search(query: String): SearchResults? =
			Https.get("/api/search?q=$query", true)
				.unsafeCast<SearchResults?>()

		@JsStatic
		fun getUser(id: String): User? =
			Https.get("/api/user/$id", true)
				.unsafeCast<User?>()

		@JsStatic
		fun lookupUser(handle: String): User? =
			Https.get("/api/lookup/$handle", true)
				.unsafeCast<User?>()

		@JsStatic
		fun editUser(id: String, data: Any): User? =
			Https.post("/api/user/$id", true, data)
				.unsafeCast<User?>()

		@JsStatic
		fun getNotifications(): List<Notification>? =
			Https.get("/api/notifications", true)
				.unsafeCast<List<Notification>?>()

		@JsStatic
		fun getNote(id: String): Note? =
			Https.get("/api/note/$id", true)
				.unsafeCast<Note?>()

		@JsStatic
		fun likeNote(id: String): Note? =
			Https.post("/api/note/$id/like", true)
				.unsafeCast<Note?>()

		@JsStatic
		fun bookmarkNote(id: String): Note? =
			Https.post("/api/note/$id/bookmark", true)
				.unsafeCast<Note?>()

		@JsStatic
		fun repeatNote(id: String, content: String? = null): Note? =
			Https.post(
				"/api/note/$id/repeat", true, mapOf(
					"content" to content,
					"visibility" to Visibility.Public // TODO: Visibility setting
				).toObject()
			).unsafeCast<Note?>()

		@JsStatic
		fun deleteNote(id: String): Note? =
			Https.delete("/api/note/$id/like")
				.unsafeCast<Note?>()

		@JsStatic
		fun createNote(data: JsAny): Note? =
			Https.post("/api/note", true, data)
				.unsafeCast<Note?>()
	}
}
