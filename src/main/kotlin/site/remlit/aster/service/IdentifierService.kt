package site.remlit.aster.service

import org.jetbrains.annotations.ApiStatus
import site.remlit.aster.model.Configuration
import site.remlit.aster.model.IdentifierType
import site.remlit.aster.model.Service
import site.remlit.aidx4j.AidUtil
import site.remlit.aidx4j.AidxUtil
import java.util.*

/**
 * Service for generating identifiers.
 *
 * @since 2025.5.1.0-SNAPSHOT
 * */
object IdentifierService : Service {
	/**
	 * Generate ID with default format
	 *
	 * @return Generated ID
	 * */
	@JvmStatic
	fun generate(): String = when (Configuration.identifiers) {
		IdentifierType.Aid -> generateAid()
		IdentifierType.Aidx -> generateAidx()
		IdentifierType.Uuid -> generateUuid()
	}

	/**
	 * Generate ID with Aid format
	 *
	 * @return Generated ID
	 * */
	@ApiStatus.Internal
	fun generateAid(): String = AidUtil.generateAid()

	/**
	 * Generate ID with Aidx format
	 *
	 * @return Generated ID
	 * */
	@ApiStatus.Internal
	fun generateAidx(): String = AidxUtil.generateAidx()

	/**
	 * Generate ID with Uuid format
	 *
	 * @return Generated ID
	 * */
	@ApiStatus.Internal
	fun generateUuid(): String = UUID.randomUUID().toString()
}
