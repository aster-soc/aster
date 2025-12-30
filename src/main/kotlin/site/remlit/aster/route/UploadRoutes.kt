package site.remlit.aster.route

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.slf4j.LoggerFactory
import site.remlit.aster.model.ApiException
import site.remlit.aster.model.Configuration
import site.remlit.aster.registry.RouteRegistry
import site.remlit.aster.service.DriveService
import site.remlit.aster.service.IdentifierService
import site.remlit.aster.util.authenticatedUserKey
import site.remlit.aster.util.authentication
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

internal object UploadRoutes {
	private val logger = LoggerFactory.getLogger(UploadRoutes::class.java)

	fun register() = RouteRegistry.registerRoute {
		staticFiles("/uploads", Configuration.fileStorage.localPath.toFile())

		authentication(
			required = true,
		) {
			post("/upload") {
				val authenticatedUser = call.attributes[authenticatedUserKey]
				// mb value * 1mb in bytes = max upload value in bytes
				val maxUploadSize = Configuration.fileStorage.maxUploadSize * 1000000L

				val multipartData = call.receiveMultipart()
				multipartData.forEachPart { part ->
					if (part is PartData.FileItem) {
						val name = part.originalFileName as String
						val contentType = part.contentType as ContentType
						val contentLength = call.request.header(HttpHeaders.ContentLength)?.toLong() ?: 0

						if (contentLength > maxUploadSize)
							throw ApiException(
								HttpStatusCode.PayloadTooLarge,
								"Exceeds max file upload size of ${Configuration.fileStorage.maxUploadSize}MB"
							)

						val path =
							Path("${Configuration.fileStorage.localPath.absolutePathString()}/${authenticatedUser.id}/$name").let {
								if (it.exists())
									Path(
										"${Configuration.fileStorage.localPath.absolutePathString()}" +
												"/${authenticatedUser.id}/${IdentifierService.generate()}-$name"
									)
								else it
							}

						if (!path.exists()) Files.createDirectories(path.parent)

						logger.debug(
							"Uploading file ({}, {} v {}) to {}",
							contentType,
							contentLength,
							maxUploadSize,
							path
						)

						part.provider().copyAndClose(path.toFile().writeChannel())

						DriveService.create(
							authenticatedUser,
							contentType,
							path
						)
					}
					part.dispose()
				}
				call.respond(HttpStatusCode.OK)
			}
		}
	}
}
