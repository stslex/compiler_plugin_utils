package io.github.stslex.compiler_plugin.utils

import io.github.stslex.compiler_plugin.DistinctChangeCache
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.fileParentOrNull
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
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
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrPluginContext.buildSaveInCacheCall(
    keyLiteral: IrExpression,
    argsListExpr: IrExpression,
    lambdaExpr: IrExpression,
    function: IrSimpleFunction,
    logger: CompileLogger,
    backingField: IrFieldSymbolImpl
): IrExpression {
    logger.i("buildSaveInCacheCall for ${function.name}, args: ${argsListExpr.dump()}")

    val distinctChangeClassSymbol = referenceClass(DistinctChangeCache::class.toClassId())
        ?: error("Cannot find DistinctChangeCache")

    val invokeFunSymbol = distinctChangeClassSymbol.owner.declarations
        .filterIsInstance<IrSimpleFunction>()
        .firstOrNull { it.name == Name.identifier("invoke") }
        ?: error("Cannot find DistinctChangeCache.invoke")

    val getDistCacheField = IrGetFieldImpl(
        startOffset = function.startOffset,
        endOffset = function.endOffset,
        symbol = backingField,
        type = distinctChangeClassSymbol.owner.defaultType,
        receiver = function.dispatchReceiverParameter?.let { thisReceiver ->
            IrGetValueImpl(
                startOffset = function.startOffset,
                endOffset = function.endOffset,
                symbol = thisReceiver.symbol,
                type = thisReceiver.type
            )
        },
        origin = null
    )

    return IrCallImpl(
        startOffset = function.startOffset,
        endOffset = function.endOffset,
        type = function.returnType,
        symbol = invokeFunSymbol.symbol,
        typeArgumentsCount = 1,
        valueArgumentsCount = 3,
        origin = null
    )
        .also { it.patchDeclarationParents(function.parent) }
        .apply {
            dispatchReceiver = getDistCacheField

            putTypeArgument(0, function.returnType)
            putValueArgument(0, keyLiteral)
            putValueArgument(1, argsListExpr)
            putValueArgument(2, lambdaExpr)
        }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrPluginContext.generateFields(
    function: IrSimpleFunction,
    qualifierArgs: IrExpression,
    logger: CompileLogger
): IrFieldSymbolImpl {
    logger.i("generateFields for ${function.name} parent: ${function.file}")

    val parentClass = function.parentClassOrNull
    val parentFile = function.fileParentOrNull

    val errorNotFound =
        "function ${function.name} in ${function.file} couldn't be used with @DistinctUntilChangeFun"

    if (parentClass == null && parentFile == null) error(errorNotFound)


    val startOffset = parentClass?.startOffset ?: parentFile?.startOffset ?: error(errorNotFound)
    val endOffset = parentClass?.endOffset ?: parentFile?.endOffset ?: error(errorNotFound)

    val fieldSymbol = IrFieldSymbolImpl()

    val distinctChangeClass = referenceClass(DistinctChangeCache::class.toClassId())
        ?: error("couldn't find DistinctChangeCache")

    val backingField = irFactory.createField(
        startOffset = startOffset,
        endOffset = endOffset,
        origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD,
        symbol = fieldSymbol,
        name = Name.identifier("_distinctCache"),
        type = distinctChangeClass.defaultType,
        visibility = DescriptorVisibilities.PRIVATE,
        isFinal = true,
        isExternal = false,
        isStatic = parentClass == null,
    )

    val constructorSymbol = distinctChangeClass.owner.declarations
        .filterIsInstance<IrConstructor>()
        .firstOrNull { it.isPrimary }
        ?: error("Cannot find primary constructor of DistinctChangeCache")

    val callDistInit = IrConstructorCallImpl.fromSymbolOwner(
        startOffset = startOffset,
        endOffset = endOffset,
        type = distinctChangeClass.defaultType,
        constructorSymbol = constructorSymbol.symbol
    )
        .apply {
            putValueArgument(0, qualifierArgs)
        }

    backingField.parent = function.parent
    backingField.initializer = irFactory.createExpressionBody(callDistInit)
    (function.parentClassOrNull ?: function.fileParentOrNull)?.declarations?.add(backingField)

    return fieldSymbol
}