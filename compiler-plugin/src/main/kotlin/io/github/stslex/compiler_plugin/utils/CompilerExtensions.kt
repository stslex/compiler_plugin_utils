package io.github.stslex.compiler_plugin.utils

import io.github.stslex.compiler_plugin.DistinctChangeCache
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KClass

internal fun IrPluginContext.createIrBuilder(
    declaration: IrDeclaration
): DeclarationIrBuilder = DeclarationIrBuilder(
    generatorContext = this,
    symbol = declaration.symbol
)

internal inline fun <reified T : Any> KClass<T>.toClassId(): ClassId = ClassId(
    FqName(java.`package`.name),
    Name.identifier(java.simpleName)
)

internal fun IrPluginContext.buildLambdaForBody(
    originalBody: IrBody,
    function: IrSimpleFunction,
): IrExpression {

    val lambdaFunction = irFactory.buildFun {
        this.startOffset = function.startOffset
        this.endOffset = function.endOffset
        this.origin = IrDeclarationOrigin.DEFINED
        this.name = Name.identifier(function.name.asString() + ".lambda")
        this.returnType = function.returnType
        this.visibility = function.visibility
        this.modality = Modality.FINAL
    }.apply {
        body = originalBody.deepCopyWithSymbols()
    }

    val lambdaType = irBuiltIns.functionN(0).typeWith(function.returnType)

    return IrFunctionExpressionImpl(
        startOffset = function.startOffset,
        endOffset = function.endOffset,
        type = lambdaType,
        function = lambdaFunction,
        origin = IrStatementOrigin.LAMBDA
    ).also {
        it.patchDeclarationParents(function)
    }
}

/**
 * Generate safe for overloading unique function name
 */
internal val IrFunction.fullyQualifiedName: String
    get() = when (val parent = this.parent) {
        is IrClass -> parent.name.asString()
        is IrPackageFragment -> parent.kotlinFqName.asString()
        else -> "UnknownParent"
    }.let { name ->
        // to be safe for overloading
        val argTypes = this.valueParameters.joinToString(", ") {
            it.type.toString()
        }
        "$name.${this.name.asString()}.($argTypes)"
    }

/**
 * Create call for [DistinctChangeCache.invoke]
 */
internal fun IrPluginContext.buildSaveInCacheCall(
    keyLiteral: IrExpression,
    argsListExpr: IrExpression,
    lambdaExpr: IrExpression,
    function: IrSimpleFunction,
    qualifierArgs: IrExpression,
    logger: CompileLogger
): IrExpression {
    logger.i("buildSaveInCacheCall for ${function.name}, args: ${argsListExpr.dump()} with config: ${qualifierArgs.dump()}")

    val memoizeFunction = referenceFunctions(
        CallableId(
            classId = DistinctChangeCache::class.toClassId(),
            callableName = Name.identifier("invoke")
        )
    )
        .singleOrNull()
        ?: error("Cannot find function DistinctChangeCache.memorize")

    return IrCallImpl(
        startOffset = function.startOffset,
        endOffset = function.endOffset,
        type = function.returnType,
        symbol = memoizeFunction,
        typeArgumentsCount = 1,
        valueArgumentsCount = 4
    )
        .also { call -> call.patchDeclarationParents(function) }
        .apply {
            putTypeArgument(0, function.returnType)
            putValueArgument(0, keyLiteral)
            putValueArgument(1, argsListExpr)
            putValueArgument(2, lambdaExpr)
            putValueArgument(3, qualifierArgs)
        }
}