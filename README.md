# Hytale.kt

[![](https://jitpack.io/v/BetrixDev/hytale-kotlin-library.svg)](https://jitpack.io/#BetrixDev/hytale-kotlin-library)

> [!CAUTION]
> This library is a proof-of-concept more than anything and hasn't been tested.

Kotlin extensions and DSLs for Hytale server plugin development. Reduce boilerplate, increase type safety, and write cleaner server plugins.

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.BetrixDev:hytaleKt:VERSION")
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
    implementation 'com.github.BetrixDev:hytaleKt:VERSION'
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
        <artifactId>hytaleKt</artifactId>
        <version>VERSION</version>
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

Full documentation available at the [docs site](https://hytale-kotlin-library.vercel.app)
## License

MIT