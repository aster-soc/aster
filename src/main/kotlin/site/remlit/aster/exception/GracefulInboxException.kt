package site.remlit.aster.exception

/**
 * Gracefully stop processing an activity, like a return but with a reason.
 *
 * @param message Reason for exception
 * */
class GracefulInboxException(message: String) : Exception(message)
