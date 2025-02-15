[![Maven Central](https://img.shields.io/maven-central/v/io.github.stslex/compiler-plugin)](https://central.sonatype.com/search?namespace=io.github.stslex)

> Check annotated functions arguments. If they don't change - return last result.
> Functions could be logged.

import compiler plugin: 
```kotlin
dependencies {
    implementation("io.github.stslex:compiler-plugin:0.0.1")
    kotlinCompilerPluginClasspath("io.github.stslex:compiler-plugin:0.0.1")
}
```

in code: 
```kotlin
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun

@DistinctUntilChangeFun
fun setUserName(username: String){
  // function logic
}
```
