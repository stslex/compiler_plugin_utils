package io.github.stslex.compiler_plugin

import io.github.stslex.compiler_plugin.utils.Action
import io.github.stslex.compiler_plugin.utils.DefaultAction
import kotlin.reflect.KClass

/**
 * @param logging enable logs for Kotlin Compiler Runtime work (useful for debug - don't use in production)
 * @param singletonAllow if enable - generates distinction for function without classes (so it's singleton)
 * @param name set name for function in compiler logs and put in into action, else take function name
 * @param action any action to process when function enter - it invokes with state of processing function body info and [name].
 * @suppress [action] shouldn't have properties in it's constructor
 * */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class DistinctUntilChangeFun(
    val logging: Boolean = LOGGING_DEFAULT,
    val singletonAllow: Boolean = SINGLETON_ALLOW,
    val name: String = "",
    val action: KClass<out Action> = DefaultAction::class
) {

    public companion object {

        internal const val LOGGING_DEFAULT = false

        internal const val SINGLETON_ALLOW = false

    }
}