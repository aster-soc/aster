package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import site.remlit.aster.common.model.NoteSearchResult
import site.remlit.aster.common.model.SearchResult
import site.remlit.aster.common.model.SearchResults
import site.remlit.aster.common.model.User
import site.remlit.aster.common.model.UserSearchResult
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.model.Service
import site.remlit.aster.util.model.fromEntity

/**
 * Service for searching.
 *
 * @since 2025.11.4.0-SNAPSHOT
 * */
object SearchService : Service {
	/**
	 * Query to search for notes and users
	 *
	 * @param query String query
	 *
	 * @return Search results
	 * */
	@JvmStatic
	fun search(query: String): SearchResults {
		val results = mutableListOf<SearchResult>()

		// User by ApId

		val userByApId = UserService.get(
			UserTable.apId eq query,
		)

		if (userByApId != null) {
			results.add(UserSearchResult(User.fromEntity(userByApId)))
			return SearchResults(true, results.toList())
		}

		// Note by ApId

		val noteByApId = NoteService.get(
			NoteTable.apId eq query,
		)

		if (noteByApId != null) {
			results.add(NoteSearchResult(noteByApId))
			return SearchResults(true, results.toList())
		}

		// User by handle

		val split = query.split("@")
		val splitUsername = split.getOrNull(1)
		val splitHost = split.getOrNull(2)

		if (splitUsername != null) {
			val handleResult = UserService.get(
				UserTable.username eq splitUsername and (UserTable.host eq splitHost),
			)

			if (handleResult != null) {
				results.add(UserSearchResult(User.fromEntity(handleResult)))
				return SearchResults(true, results.toList())
			}
		}

		// Generic string matching

		val userResults = UserService.getMany(
			UserTable.displayName like "%${query}%" or
					(UserTable.username like "%${query}%") or
					(UserTable.bio like "%${query}%")
		).distinct()

		userResults.forEach {
			results.add(UserSearchResult(User.fromEntity(it)))
		}

		val noteResults = NoteService.getMany(
			NoteTable.content like "%${query}%" or
					(NoteTable.cw like "%${query}%")
		).distinct()

		noteResults.forEach {
			results.add(NoteSearchResult(it))
		}

		return SearchResults(false, results.toList())
	}
}
