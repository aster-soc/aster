<h1>
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="src/main/resources/uikit/branding/wmark-dark.svg">
        <source media="(prefers-color-scheme: light)" srcset="src/main/resources/uikit/branding/wmark-light.svg">
        <img height="50" alt="Aster" src="src/main/resources/uikit/branding/wmark-light.svg">
    </picture>
</h1>

[Issue Tracker](https://youtrack.remlit.site/projects/AS/issues)

Decentralized social software using ActivityPub, written in Kotlin.

## Goals

- Setup and forget. Aster shouldn't be a pain to run.
- Provide typical wants and needs of a social software, and more.
- Allow developing plugins for extending capabilities with ease.
- Make sure users choices are respected, especially blocks and mutes.

## Building

To build Aster, you'll need at least Java 21. You'll also need Node.js (Latest LTS version recommended) and pnpm (
`npm i -g pnpm`).

Run the build script `./gradlew build`, and then grab the JAR from `build/libs/aster-*-all.jar`.

## Running

To run Aster, you'll need at least Java 21 and a PostgreSQL database.
Copy `configuration.example.yaml` to `configuration.yaml` and fill out the database connection information.
After that, run `java -jar aster.jar migration:execute` and your database will be set up and your instance is ready to
go!

After setting up a user, you can promote them to an Admin role with the CLI. First, get the ID of the generated Admin
role by running `java -jar aster.jar role:list`, and then `java -jar aster.jar role:give {User ID} {Role ID}`.

## Configuring

The main configuration is `configuration.yaml`, you can copy `configuration.example.yaml` and modify it. There's also
some environment variables that are important.

## Contributing

Code contributions are welcome, but Aster is in early development and I may have plans for how to do things already. You
should contact me before opening a pull request or working on anything so we can get on the same page.

Contributing by reporting bugs is also welcome, too! Aster's issue tracker can be
found [here](https://youtrack.remlit.site/projects/AS/issues).

### Project Breakdown

The main part of the project is the backend, `main`, which runs the server. It uses Ktor, a custom event system, and a
custom plugin system.

There's a module called `common` which targets the JVM and JS using Kotlin Multiplatform, this allows shared models and
types between the frontend and backend and shared logic. Certain things used in the admin frontend (server side rendered
and built into `main`) are also usable in the frontend, so it's put there. The frontend's API request handling code is
also in this package.

There's another module that relates to `common` called `common-generators`. It generates partial versions of certain
models that are all nullable, and by default null. These can be used for edits, like on a user or a note.

Aster uses a markdown format used by Misskey called MFM. In the `mfmkt` module, there's a library that targets the JVM
and JS. It's used for the frontend and backend to handle MFM and scanning user submitted content.

The last module is `frontend`, the directory that contains the React frontend project.

Aster has two frontends, the server side rendered admin panel and the user facing React frontend. They use a shared UI
kit that is under the `main` module's resources as "uikit." It's very bare bones, but reduces a lot of variables and
makes essential assets not dependent on the React frontend being enabled.

### Development Environment

IntelliJ IDEA is a requirement for working with Aster. For development, the JetBrains Runtime is recommended.

When you clone Aster in IDEA, automatically it should recognize the build scripts in `.run`. Follow the building and
running instructions above to create a JAR and prepare your database.

Afterward, running `Backend Development Build` in the IDE should be all you need to develop, unless you need to use CLI
commands. This script skips generating documentation and compiling the frontend, which significantly slows down the
build time.

If you are working on the frontend, running `pnpm dev` in the `frontend` directory will start a
development server for the frontend which hot reloads.
