package site.remlit.aster.service

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.db.entity.EmojiEntity
import site.remlit.aster.db.table.EmojiTable
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.model.Service

/**
 * Service for managing emojis
 *
 * @since 2025.11.3.0-SNAPSHOT
 * */
object EmojiService : Service {
	/**
	 * Get an emoji
	 *
	 * @param where Query for finding emoji
	 *
	 * @return Emoji, if it exists
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): EmojiEntity? = transaction {
		EmojiEntity
			.find { where }
			.singleOrNull()
	}

	/**
	 * Get an emoji by ID
	 *
	 * @param id ID of instance
	 *
	 * @return Emoji, if it exists
	 * */
	@JvmStatic
	fun getById(id: String): EmojiEntity? = get(EmojiTable.id eq id)

	/**
	 * Get many emojis
	 *
	 * @param where Query for finding emojis
	 * @param take Number of emojis to take
	 * @param offset Offset for query
	 *
	 * @return Emojis, if they exist
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<EmojiEntity> = transaction {
		EmojiEntity
			.find { where }
			.offset(offset)
			.take(take)
			.sortedByDescending { it.createdAt }
			.toList()
	}

	/**
	 * Get many emojis by host
	 *
	 * @param host Host of emoji
	 *
	 * @return Emojis, if they exist
	 * */
	@JvmStatic
	fun getManyByHost(host: String): List<EmojiEntity> = getMany(EmojiTable.host eq host)

	/**
	 * Count emojis
	 *
	 * @param where Query to find emojis
	 *
	 * @return Count of emojis where query applies
	 * */
	@JvmStatic
	fun count(where: Op<Boolean>): Long = transaction {
		EmojiEntity
			.find { where }
			.count()
	}
}
