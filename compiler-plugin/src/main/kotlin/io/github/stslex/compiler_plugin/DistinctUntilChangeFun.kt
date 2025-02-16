package io.github.stslex.compiler_plugin

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class DistinctUntilChangeFun(
    val logging: Boolean = LOGGING_DEFAULT
) {

    public companion object {

        internal const val LOGGING_DEFAULT = false

    }
}