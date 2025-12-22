package site.remlit.aster.common.api

import org.w3c.xhr.FormData
import site.remlit.aster.common.model.DriveFile
import site.remlit.aster.common.model.Meta
import site.remlit.aster.common.model.Note
import site.remlit.aster.common.model.Notification
import site.remlit.aster.common.model.SearchResults
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.Visibility
import site.remlit.aster.common.model.response.AuthResponse
import site.remlit.aster.common.util.Https
import site.remlit.aster.common.util.toObject
import kotlin.js.Promise

@JsExport
@Suppress("UtilityClassWithPublicConstructor", "TooManyFunctions", "Unused")
@OptIn(ExperimentalJsStatic::class)
class Api {
	companion object {
		@JsStatic
		fun register(username: String, password: String, invite: String?) =
			Https.post(
				"/api/register",
				false,
				mapOf(
					"username" to username,
					"password" to password,
					"invite" to invite
				).toObject()
			).unsafeCast<Promise<AuthResponse?>>()

		@JsStatic
		fun login(username: String, password: String) =
			Https.post(
				"/api/login",
				false,
				mapOf(
					"username" to username,
					"password" to password
				).toObject()
			).unsafeCast<Promise<AuthResponse?>>()

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
		fun getTimeline(timeline: String, since: String? = null) =
			Https.get("/api/timeline/$timeline${if (since != null) "?since=$since" else ""}", true)
				.unsafeCast<Promise<List<Note?>>>()

		@JsStatic
		fun getBookmarks(since: String? = null) =
			Https.get("/api/bookmarks${if (since != null) "?since=$since" else ""}", true)
				.unsafeCast<Promise<List<Note?>>>()

		@JsStatic
		fun search(query: String) =
			Https.get("/api/search?q=$query", true)
				.unsafeCast<Promise<SearchResults?>>()

		@JsStatic
		fun getUser(id: String) =
			Https.get("/api/user/$id", true)
				.unsafeCast<Promise<User?>>()

		@JsStatic
		fun getUserRelationship(id: String) =
			Https.get("/api/user/$id/relationship", true)
				.unsafeCast<Promise<User?>>()

		@JsStatic
		fun lookupUser(handle: String) =
			Https.get("/api/lookup/$handle", true)
				.unsafeCast<Promise<User?>>()

		@JsStatic
		fun editUser(id: String, data: Any) =
			Https.post("/api/user/$id", true, data)
				.unsafeCast<Promise<User?>>()

		@JsStatic
		fun followUser(id: String) =
			Https.post("/api/user/$id/follow", true)
				.unsafeCast<Promise<User?>>()

		@JsStatic
		fun getNotifications(since: String? = null) =
			Https.get("/api/notifications${if (since != null) "?since=$since" else ""}", true)
				.unsafeCast<Promise<List<Notification>?>>()

		@JsStatic
		fun getNote(id: String) =
			Https.get("/api/note/$id", true)
				.unsafeCast<Promise<Note?>>()

		@JsStatic
		fun likeNote(id: String) =
			Https.post("/api/note/$id/like", true)
				.unsafeCast<Promise<Note?>>()

		@JsStatic
		fun bookmarkNote(id: String) =
			Https.post("/api/note/$id/bookmark", true)
				.unsafeCast<Promise<Note?>>()

		@JsStatic
		fun repeatNote(id: String, content: String? = null) =
			Https.post(
				"/api/note/$id/repeat", true, mapOf(
					"content" to content,
					"visibility" to Visibility.Public // TODO: Visibility setting
				).toObject()
			).unsafeCast<Promise<Note?>>()

		@JsStatic
		fun deleteNote(id: String) =
			Https.delete("/api/note/$id")
				.unsafeCast<Promise<Note?>>()

		@JsStatic
		fun createNote(data: JsAny) =
			Https.post("/api/note", true, data)
				.unsafeCast<Promise<Note?>>()

		@JsStatic
		fun getMeta() =
			Https.get("/api/meta", true)
				.unsafeCast<Promise<Meta?>>()

		@JsStatic
		fun getDrive(since: String? = null) =
			Https.get("/api/drive${if (since != null) "?since=$since" else ""}", true)
				.unsafeCast<Promise<List<DriveFile>?>>()

		@JsStatic
		fun getDriveFile(id: String) =
			Https.get("/api/drive/file/$id")
				.unsafeCast<Promise<DriveFile?>>()

		@JsStatic
		fun deleteDriveFile(id: String) =
			Https.delete("/api/drive/file/$id")

		@JsStatic
		fun upload(data: FormData) =
			Https.postRaw("/upload", true, data)
	}
}
