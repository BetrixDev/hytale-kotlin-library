# hytale-kotlin-library

[![](https://jitpack.io/v/BetrixDev/hytale-kotlin-library.svg)](https://jitpack.io/#BetrixDev/hytale-kotlin-library)

Kotlin extensions and DSLs for Hytale server plugin development. Reduce boilerplate, increase type safety, and write cleaner server plugins.

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.BetrixDev:hytale-kotlin-library:v0.1.0")
    compileOnly(files("path/to/HytaleServer.jar"))
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.BetrixDev:hytale-kotlin-library:v0.1.0'
    compileOnly files('path/to/HytaleServer.jar')
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.BetrixDev</groupId>
        <artifactId>hytale-kotlin-library</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

## Features

- **Type-Safe Commands** - Build player commands with DSL builders
- **ECS Extensions** - Kotlin extensions for Hytale's Entity-Component-System
- **Codec DSL** - Type-safe component serialization builders
- **Event Registration** - Register handlers with reified generics
- **UI Extensions** - Cleaner HUD and page APIs
- **Null Safety** - Explicit nullability throughout

## Quick Example

```kotlin
class MyPlugin : JavaPlugin() {
    override fun onLoad() {
        // Register commands with DSL
        registerCommand(
            playerCommand("hello", "Says hello") {
                execute { player, context ->
                    context.msg("Hello, ${player.name}!")
                }
            }
        )
        
        // Register events with reified generics
        registerEvent<PlayerConnectEvent> { event ->
            // Handle player connect
        }
    }
}
```

## Documentation

Full documentation available at the [docs site](https://betrixdev.github.io/hytale-kotlin-library/).

## License

MIT