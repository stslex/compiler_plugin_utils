package io.github.stslex.compiler_plugin

/**
 * @param logging enable logs for Kotlin Compiler Runtime work (useful for debug - don't use in production)
 * @param singletonAllow if enable - generates distinction for function without classes (so it's singleton)
 * */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class DistinctUntilChangeFun(
    val logging: Boolean = LOGGING_DEFAULT,
    val singletonAllow: Boolean = false
) {

    public companion object {

        internal const val LOGGING_DEFAULT = false

    }
}