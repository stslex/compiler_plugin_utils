package io.github.stslex.compiler_plugin.utils

import io.github.stslex.compiler_plugin.DistinctChangeConfig
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun.Companion.LOGGING_DEFAULT
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrPluginContext.readQualifier(
    function: IrSimpleFunction,
    logger: CompileLogger
): IrExpression? {
    val qualifiedName = DistinctUntilChangeFun::class.qualifiedName ?: return null
    val annotation = function.getAnnotation(FqName(qualifiedName)) ?: return null

    logger.i("readQualifier: annotation is found for ${function.name}")

    val irBuilder = createIrBuilder(function)

    val logging = annotation.getValueArgument(0)
        ?: irBuilder.irBoolean(LOGGING_DEFAULT)

    val constructorSymbol = referenceClass(DistinctChangeConfig::class.toClassId())
        ?.constructors
        ?.firstOrNull()
        ?: error("CheckChangesConfig not found in IR")

    return irBuilder
        .irCallConstructor(
            callee = constructorSymbol,
            typeArguments = emptyList()
        )
        .also { it.patchDeclarationParents(function) }
        .apply {
            putValueArgument(0, logging)
        }
}

internal fun IrSimpleFunction.getQualifierValue(name: String): Boolean = getAnnotation(
    FqName(DistinctUntilChangeFun::class.qualifiedName!!)
)
    ?.getValueArgument(Name.identifier(name))
    ?.parseValue<Boolean>()
    ?: false

private inline fun <reified T> IrExpression.parseValue(): T = when (this) {
    is IrConst<*> -> when (kind) {
        IrConstKind.Boolean -> value
        IrConstKind.Byte -> value
        IrConstKind.Char -> value
        IrConstKind.Double -> value
        IrConstKind.Float -> value
        IrConstKind.Int -> value
        IrConstKind.Long -> value
        IrConstKind.Null -> value
        IrConstKind.Short -> value
        IrConstKind.String -> value
    }

    else -> error("Unsupported type")
} as? T ?: error("${T::class} is not as it expected: $value")