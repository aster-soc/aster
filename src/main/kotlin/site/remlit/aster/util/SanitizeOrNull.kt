package site.remlit.aster.util

import site.remlit.aster.service.SanitizerService

/**
 * Sanitizes a string with the sanitizer service, or returns null.
 *
 * @param string String to sanitize
 *
 * @return Sanitized string
 * */
inline fun sanitizeOrNull(string: () -> String?): String? =
	SanitizerService.sanitize(string() ?: return null)
