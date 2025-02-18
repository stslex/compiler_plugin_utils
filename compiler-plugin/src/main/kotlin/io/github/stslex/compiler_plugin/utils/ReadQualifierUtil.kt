package io.github.stslex.compiler_plugin.utils

import io.github.stslex.compiler_plugin.DistinctUntilChangeFun
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun.Companion.LOGGING_DEFAULT
import io.github.stslex.compiler_plugin.model.DistinctChangeConfig
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KClass

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrPluginContext.readQualifier(
    function: IrSimpleFunction,
    logger: CompileLogger
): IrExpression? {
    val qualifiedName = DistinctUntilChangeFun::class.qualifiedName ?: return null
    val annotation = function.getAnnotation(FqName(qualifiedName)) ?: return null

    logger.i("readQualifier: annotation is found for ${function.name}")

    val irBuilder = createIrBuilder(function)

    val loggingExpr = annotation.getValueArgument(0)
        ?: irBuilder.irBoolean(LOGGING_DEFAULT)
    val actionName = annotation.getValueArgument(2)
        ?: irBuilder.irString(function.name.identifier)
    val actionInstanceExpr = getQualifierAction(annotation, irBuilder)

    val constructorSymbol = referenceClass(DistinctChangeConfig::class.classId)
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
            putValueArgument(0, loggingExpr)
            putValueArgument(1, actionInstanceExpr)
            putValueArgument(2, actionName)
        }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
private fun IrPluginContext.getQualifierAction(
    annotation: IrConstructorCall,
    irBuilder: DeclarationIrBuilder
): IrExpression {
    val defaultActionClass = irBuiltIns
        .findClass(DefaultAction::class.name, DefaultAction::class.fqName)
        ?: error("readQualifier ${DefaultAction::class.java.simpleName} not found")

    val actionReference = annotation.getValueArgument(3) as? IrClassReference

    val actionClassSymbol = actionReference?.symbol as? IrClassSymbol ?: defaultActionClass

    val actionConstructorSymbol = actionClassSymbol.constructors.firstOrNull {
        it.owner.valueParameters.isEmpty()
    } ?: error("No no-arg constructor for action class: ${actionReference?.symbol?.defaultType}")

    return irBuilder.irCallConstructor(actionConstructorSymbol, emptyList())
}

public fun getJavaClassNonInline(kClass: KClass<*>): Class<*> = kClass.java

internal inline fun <reified T> IrSimpleFunction.getQualifierValue(
    name: String,
    defaultValue: T
): T = getAnnotation(
    FqName(DistinctUntilChangeFun::class.qualifiedName!!)
)
    ?.getValueArgument(Name.identifier(name))
    ?.parseValue<T>()
    ?: defaultValue

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