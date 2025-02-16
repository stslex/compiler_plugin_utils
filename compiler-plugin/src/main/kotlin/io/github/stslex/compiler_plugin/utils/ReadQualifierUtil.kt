package io.github.stslex.compiler_plugin.utils

import io.github.stslex.compiler_plugin.DistinctChangeConfig
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun.Companion.LOGGING_DEFAULT
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.FqName

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrPluginContext.readQualifier(
    function: IrSimpleFunction,
    logger: CompileLogger
): IrExpression? {
    val qualifiedName = DistinctUntilChangeFun::class.qualifiedName ?: return null
    val annotation = function.getAnnotation(FqName(qualifiedName)) ?: return null

    logger.i("readQualifier: annotation is found for ${function.name}")

    val irBuilder = createIrBuilder(function)

    val currentValue = annotation.getValueArgument(0)
    val logging = currentValue ?: irBuilder.irBoolean(LOGGING_DEFAULT)

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