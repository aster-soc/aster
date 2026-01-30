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

## Queue Flow

### Inbox and Deliver

The inbox and deliver queues follow a very similar flow. First, preprocessors are ran on the job, then
the job is handled. The inbox is handled with the InboxHandlerRegistry, while deliver is handled by the ApDeliverService.
Jobs can be cancelled by preprocessors by setting the job to null. 

Queue preprocessors, unlike handlers, do not require a type. Preprocessors are passed the raw queue entity and would need
to do type checks after serialization. Due to this, preprocessors are less performant than overriding an existing handler.

Queue preprocessors and handlers can be registered with Java and Kotlin specific methods. For the Kotlin methods, you don't
need to pass anything besides the type argument. 

```kotlin
fun main() {
	InboxHandlerRegistry.register<ApTypeHandler>("Type")
}
```

```java
void main() {
	InboxHandlerRegistry.register("Type", new ApTypeHandler());
}
```

## JvmSynthetic methods

Sometimes, there's easier ways to write methods where only type arguments can replace arguments in a method. These are
only usable in Kotlin, so they need to be marked `@JvmSynthetic`. They should only be used as a wrapper around a main function
doing the same thing that is Java friendly. For example, the two register methods on the InboxHandlerRegistry.

# Services

Services must be Kotlin objects, and all public methods are to be marked with either `@JvmStatic` or `@JvmSynthetic`.
This allows clean APIs to be used in Java.

## Database

Jetbrains Exposed (the ORM used) doesn't persist joins outside the current transaction context. That's why NoteService
will return a Note rather than a NoteEntity. The Note object will persist all the values given to it, and they will be
usable outside the transaction context. You may want to do this for other services, use NoteService as a point of
reference for that.

