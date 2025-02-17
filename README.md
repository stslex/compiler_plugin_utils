[![Maven Central](https://img.shields.io/maven-central/v/io.github.stslex/compiler-plugin)](https://central.sonatype.com/search?namespace=io.github.stslex)

> Check annotated functions arguments. If they don't change - return last result.
> Functions could be logged.
> Support extra custom action on function processing

import compiler plugin:

```kotlin
dependencies {
    implementation("io.github.stslex:compiler-plugin:$version")
    kotlinCompilerPluginClasspath("io.github.stslex:compiler-plugin:$version")
}
```

in code (all annotation properties are optional):

```kotlin

import io.github.stslex.compiler_plugin.DistinctUntilChangeFun

@DistinctUntilChangeFun(
    logging = true,
    singletonAllow = false,
    name = "set_user_second_name",
    action = TestLogger::class
)
fun setUserName(username: String) {
    // function logic
}
```

for custom actions:

```kotlin
class TestLogger : Action {

    override fun invoke(
        name: String,
        isProcess: Boolean
    ) {
        println("test action $name procession: $isProcess")
    }
}
```