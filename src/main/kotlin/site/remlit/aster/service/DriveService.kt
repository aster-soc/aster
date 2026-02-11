package site.remlit.aster.service

import io.ktor.http.*
import io.trbl.blurhash.BlurHash
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import site.remlit.aster.common.model.DriveFile
import site.remlit.aster.common.util.orNull
import site.remlit.aster.db.entity.DriveFileEntity
import site.remlit.aster.db.entity.UserEntity
import site.remlit.aster.db.table.DriveFileTable
import site.remlit.aster.db.table.NoteTable
import site.remlit.aster.db.table.UserTable
import site.remlit.aster.event.drive.DriveFileCreateEvent
import site.remlit.aster.event.drive.DriveFileDeleteEvent
import site.remlit.aster.event.drive.DriveFileEditEvent
import site.remlit.aster.model.config.Configuration
import site.remlit.aster.model.Service
import site.remlit.aster.model.ap.ApIdOrObject
import site.remlit.aster.model.ap.ApNote
import site.remlit.aster.model.ap.activity.ApUpdateActivity
import site.remlit.aster.service.ap.ApDeliverService
import site.remlit.aster.service.ap.ApIdService
import site.remlit.aster.util.model.fromEntities
import site.remlit.aster.util.model.fromEntity
import site.remlit.aster.util.sql.arrayContains
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.name

/**
 * Service for managing drive files and other drive related tasks.
 *
 * @since 2025.10.5.0-SNAPSHOT
 * */
object DriveService : Service {
	/**
	 * Get a drive file.
	 *
	 * @param where Query to find drive file
	 *
	 * @return Found drive file, if any
	 * */
	@JvmStatic
	fun get(where: Op<Boolean>): DriveFile? = transaction {
		val driveFile = DriveFileEntity
			.find { where }
			.singleOrNull()
			?.load(DriveFileEntity::user)

		if (driveFile != null)
			DriveFile.fromEntity(driveFile)
		else null
	}

	/**
	 * Get a drive file by ID.
	 *
	 * @param id ID of drive file
	 *
	 * @return Found drive file, if any
	 * */
	@JvmStatic
	fun getById(id: String): DriveFile? = get(DriveFileTable.id eq id)

	/**
	 * Get a drive file by source.
	 *
	 * @param src Source of drive file
	 *
	 * @return Found drive file, if any
	 * */
	@JvmStatic
	fun getBySrc(src: String): DriveFile? = get(DriveFileTable.src eq src)

	/**
	 * Get many drive files.
	 *
	 * @param where Query to find drive files
	 * @param take Number of drive files to take
	 * @param offset Offset for query
	 *
	 * @return Found drive files, if any
	 * */
	@JvmStatic
	fun getMany(
		where: Op<Boolean>,
		take: Int = Configuration.timeline.defaultObjects,
		offset: Long = 0
	): List<DriveFile> = transaction {
		val driveFiles = (DriveFileTable innerJoin UserTable)
			.selectAll()
			.where { where }
			.offset(offset)
			.let { DriveFileEntity.wrapRows(it) }
			.sortedByDescending { it.createdAt }
			.take(take)
			.toList()

		if (!driveFiles.isEmpty())
			DriveFile.fromEntities(driveFiles)
		else listOf()
	}

	/**
	 * Get count of drive files.
	 *
	 * @param where Query to find drive files
	 *
	 * @return Count of drive files
	 * */
	@JvmStatic
	fun count(where: Op<Boolean>): Long = transaction {
		DriveFileTable
			.leftJoin(UserTable)
			.select(where)
			.count()
	}

	/**
	 * Create a drive file.
	 *
	 * @param user User creating file
	 * @param type Content type of file
	 * @param path Path to file
	 * @param alt Alt text for file
	 *
	 * @return Created drive file
	 * */
	@JvmStatic
	fun create(
		user: UserEntity,
		type: ContentType,
		path: Path,
		alt: String? = null,
	): DriveFile =
		transaction {
			val id = IdentifierService.generate()

			DriveFileEntity.new(id) {
				this.type = type.toString()
				this.src = "${Configuration.url.protocol.name}://${Configuration.url.host}/uploads/${user.id}/${path.name}"
				this.user = user
				this.alt = alt
				this.blurHash = if (type.toString().startsWith("image"))
					orNull { generateBlurHash(path) }
				else null
			}

			val driveFile = getById(id)!!
			DriveFileCreateEvent(driveFile).call()

			return@transaction driveFile
		}

	/**
	 * Create a drive file.
	 *
	 * @param user User creating file
	 * @param type Content type of file
	 * @param url Url for file
	 * @param alt Alt text for file
	 *
	 * @return Created drive file
	 * */
	@JvmStatic
	fun create(
		user: UserEntity,
		type: ContentType,
		url: Url,
		alt: String? = null,
	): DriveFile =
		transaction {
			val id = IdentifierService.generate()

			DriveFileEntity.new(id) {
				this.type = type.toString()
				this.src = url.toString()
				this.user = user
				this.alt = alt
			}

			val driveFile = getById(id)!!
			DriveFileCreateEvent(driveFile).call()

			return@transaction driveFile
		}

	/**
	 * Delete a drive file.
	 *
	 * @param where Query to find drive file
	 * */
	@JvmStatic
	fun delete(where: Op<Boolean>) = transaction {
		val entity = DriveFileEntity
			.find { where }
			.singleOrNull()
		if (entity == null) return@transaction

		// TODO: Delete file, if local

		DriveFileDeleteEvent(DriveFile.fromEntity(entity)).call()

		entity.delete()
	}

	/**
	 * Delete a drive file by ID.
	 *
	 * @param id ID of drive file
	 * */
	@JvmStatic
	fun deleteById(id: String) = delete(DriveFileTable.id eq id)

	/**
	 * Delete a drive file by source.
	 *
	 * @param src Source of drive file
	 * */
	@JvmStatic
	fun deleteBySrc(src: String) = delete(DriveFileTable.src eq src)

	/**
	 * Update a drive file
	 *
	 * @param driveFile Drive file to update
	 * @param alt Updated alt text
	 * @param sensitive Updated sensitive boolean
	 *
	 * @return Updated drive file
	 * */
	@JvmStatic
	fun update(
		driveFile: DriveFile,
		alt: String? = driveFile.alt,
		sensitive: Boolean = driveFile.sensitive
	): DriveFile {
		if (alt != null && alt.length > Configuration.note.maxLength.content)
			throw IllegalArgumentException("Alt text cannot be longer than ${Configuration.note.maxLength.content}")

		transaction {
			DriveFileEntity.findByIdAndUpdate(driveFile.id) {
				it.alt = alt?.ifEmpty { null }
				it.sensitive = sensitive
			}
		}

		val newFile = getById(driveFile.id)!!

		DriveFileEditEvent(newFile).call()

		if (newFile.user.isLocal()) {
			val notes = NoteService.getMany(
				NoteTable.attachments arrayContains listOf(driveFile.id) and
					(NoteTable.user eq newFile.user.id)
			)

			val user = transaction { UserEntity[newFile.user.id] }

			if (!notes.isEmpty()) {
				notes.forEach {
					val apNote = ApNote.fromEntity(it)

					ApDeliverService.deliverToFollowers<ApUpdateActivity>(
						ApUpdateActivity(
							ApIdService.renderActivityApId(IdentifierService.generate()),
							actor = user.apId,
							`object` = ApIdOrObject.createObject { apNote },
							to = apNote.to,
							cc = apNote.cc
						),
						user
					)
				}
			}
		}

		return newFile
	}

	/**
	 * Generate a blur hash for a file
	 *
	 * @param path Local path of file
	 *
	 * @return Generated blur hash
	 * */
	@JvmStatic
	fun generateBlurHash(path: Path): String {
		if (!path.exists())
			throw IllegalArgumentException("Path does not exist")

		return BlurHash.encode(ImageIO.read(path.toFile()))
	}
}
