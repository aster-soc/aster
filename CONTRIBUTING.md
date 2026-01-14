# Contributing

## Code Style

Make sure your editor is following the `.editorconfig` so you don't have to worry about spacing and tabs.

Avoid unneeded brackets when possible, for example:

```kotlin
if (RelationshipService.eitherBlocking(user.id.toString(), author.id.toString()))
	return false

// rather than

if (RelationshipService.eitherBlocking(user.id.toString(), author.id.toString())) {
	return false
}
```

```kotlin
suspend fun get(where: Op<Boolean>): UserEntity? = transaction {
	UserEntity
		.find { where }
		.singleOrNull()
}

// rather than

suspend fun get(where: Op<Boolean>): UserEntity? {
	return transaction {
		UserEntity
			.find { where }
			.singleOrNull()
	}
}
```

If there's enough potential reuse for some code or potential for expansion (example: TimeService at the time of writing
has one method, but is likely to be extended for further utilities), create a service object for it. Otherwise, create a
util.

## Routing

For routing, only the ApiException is graceful. It will be caught and returned with the provided status code. The rest of
the exceptions will return 500 and the message provided along with a stack trace. Be careful with placement of exceptions
and messages returned since they'll be given to users when debug mode is on. Debug mode is not exclusive to development,
do not show anything sensitive in it.

`call.respond` isn't enough to stop the execution of a route and will cause a server error after the request
resolves.

```kotlin
get("/example/route") {
	// Stops
	throw ApiException(HttpStatusCode.Forbidden, "You can't do that")
	
	// Stops, returns 500
	throw Exception("Something went wrong")
	
	// Does not stop
	call.respond(HttpStatusCode.Forbidden)
	
	// Stops
	return@get call.respond(HttpStatusCode.Forbidden)
}
```

## Database

Jetbrains Exposed (the ORM used) doesn't persist joins outside the current transaction context. That's why NoteService
will return a Note rather than a NoteEntity. The Note object will persist all the values given to it, and they will be
usable outside the transaction context. You may want to do this for other services, use NoteService as a point of
reference for that.

