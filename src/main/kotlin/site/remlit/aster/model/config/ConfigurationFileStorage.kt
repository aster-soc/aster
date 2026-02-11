package site.remlit.aster.model.config

import site.remlit.aster.common.model.type.FileStorageType
import site.remlit.aster.exception.ConfigurationException
import site.remlit.aster.util.capitalize
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Suppress("MagicNumber")
class ConfigurationFileStorage : ConfigurationObject {
	val type: FileStorageType
		get() = FileStorageType.valueOf(
			config?.propertyOrNull("fileStorage.type")?.getString()?.capitalize() ?: "Local"
		)
	val localPath: Path
		get() {
			val path = Path(config?.propertyOrNull("fileStorage.localPath")?.getString() ?: "/var/lib/aster/files")

			if (!path.exists())
				throw ConfigurationException("File storage path doesn't exist")
			if (!path.isDirectory())
				throw ConfigurationException("File storage path is a file, not a directory")
			if (!path.toFile().canWrite())
				throw ConfigurationException("File storage path is not writeable")

			return path
		}

	val maxUploadSize: Int
		get() = config?.propertyOrNull("fileStorage.maxUploadSize")?.getString()?.toInt() ?: 25
}
