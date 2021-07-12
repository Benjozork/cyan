package cyan.compiler.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.expression.CyanArrayExpression
import cyan.compiler.parser.ast.expression.CyanBinaryExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.function.*
import cyan.compiler.parser.ast.operator.CyanBinaryPlusOperator
import cyan.compiler.parser.ast.types.CyanStructDeclaration
import cyan.compiler.parser.ast.types.CyanTraitDeclaration
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*

class StatementsTest {

    val parser = CyanModuleParser()

    private fun doTest(source: String, expectedSource: List<CyanStatement>) =
        assertIterableEquals(expectedSource.map { it.toString() }, parser.parseToEnd("module test\n$source").source.statements.map { it.toString() })

    @Nested
    inner class TypeDeclarations {

        @Test fun `trait with one function`() = doTest (
            """
                type Printable = trait {
                    function print(): void
                }
            """.trimIndent(),
            listOf (
                CyanTraitDeclaration (
                    CyanIdentifierExpression("Printable"),
                    arrayOf (
                        CyanTraitDeclaration.Element.Function (
                            CyanFunctionDeclaration (
                                CyanFunctionSignature (
                                    attributes = emptyList(),
                                    receiver = null,
                                    name = CyanIdentifierExpression("print"),
                                    args = emptyList(),
                                    typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Void)),
                                    isExtern = false
                                ),
                                source = null
                            )
                        )
                    )
                )
            )
        )

        @Test fun `trait with two functions`() = doTest (
            """
                type Serialize = trait {
                    function in(): str
                    
                    function out(): str
                }
            """.trimIndent(),
            listOf (
                CyanTraitDeclaration (
                    CyanIdentifierExpression("Serialize"),
                    arrayOf (
                        CyanTraitDeclaration.Element.Function (
                            CyanFunctionDeclaration (
                                CyanFunctionSignature (
                                    attributes = emptyList(),
                                    receiver = null,
                                    name = CyanIdentifierExpression("in"),
                                    args = emptyList(),
                                    typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str)),
                                    isExtern = false
                                ),
                                source = null
                            )
                        ),
                        CyanTraitDeclaration.Element.Function (
                            CyanFunctionDeclaration (
                                CyanFunctionSignature (
                                    attributes = emptyList(),
                                    receiver = null,
                                    name = CyanIdentifierExpression("out"),
                                    args = emptyList(),
                                    typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str)),
                                    isExtern = false
                                ),
                                source = null
                            )
                        )
                    )
                )
            )
        )

        @Test fun `trait with one property`() = doTest (
            """
                type Named = trait {
                    name: str
                }
            """.trimIndent(),
            listOf (
                CyanTraitDeclaration (
                    CyanIdentifierExpression("Named"),
                    arrayOf (
                        CyanTraitDeclaration.Element.Property (
                            CyanIdentifierExpression("name"),
                            CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str))
                        )
                    )
                )
            )
        )

        @Test fun `trait with two properties`() = doTest (
            """
                type Being = trait {
                    name: str
                    age: i32
                }
            """.trimIndent(),
            listOf (
                CyanTraitDeclaration (
                    CyanIdentifierExpression("Being"),
                    arrayOf (
                        CyanTraitDeclaration.Element.Property (
                            CyanIdentifierExpression("name"),
                            CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str))
                        ),
                        CyanTraitDeclaration.Element.Property (
                            CyanIdentifierExpression("age"),
                            CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32))
                        )
                    )
                )
            )
        )

        @Test fun `trait with one function and one property`() = doTest (
            """
                type Person = trait {
                    function speak(): void
                    name: str
                }
            """.trimIndent(),
            listOf (
                CyanTraitDeclaration (
                    CyanIdentifierExpression("Person"),
                    arrayOf (
                        CyanTraitDeclaration.Element.Function (
                            CyanFunctionDeclaration (
                                CyanFunctionSignature (
                                    attributes = emptyList(),
                                    receiver = null,
                                    name = CyanIdentifierExpression("speak"),
                                    args = emptyList(),
                                    typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Void)),
                                    isExtern = false
                                ),
                                source = null
                            )
                        ),
                        CyanTraitDeclaration.Element.Property (
                            CyanIdentifierExpression("name"),
                            CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str))
                        )
                    )
                )
            )
        )

    }

    @Nested
    inner class Derives {

        @Test fun `with one function impl`() = doTest (
            """
                type Greeting = struct {
                    text: str
                
                    derive Printable {
                        print(): str {
                            return text
                        }
                    }
                }
            """.trimIndent(),
            listOf (
                CyanStructDeclaration (
                    CyanIdentifierExpression("Greeting"),
                    arrayOf (
                        CyanStructDeclaration.Property(CyanIdentifierExpression("text"), CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str)))
                    ),
                    arrayOf (
                        CyanDerive (
                            CyanTypeAnnotation.Reference(CyanIdentifierExpression("Printable")),
                            arrayOf (
                                CyanDerive.Item.Function (
                                    CyanIdentifierExpression("print"),
                                    emptyArray(),
                                    CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str)),
                                    CyanSource (
                                        listOf (
                                            CyanReturn(CyanIdentifierExpression("text"))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

    }

    @Nested
    inner class VariableDeclarations {

        @Test fun `i32 no type annotation`() = doTest (
            """
                let a = 5
            """.trimIndent(),
            listOf (
                CyanVariableDeclaration (
                    name = CyanIdentifierExpression(value = "a"),
                    mutable = false,
                    type = null,
                    value = CyanNumericLiteralExpression(5, null)
                )
            )

        )

        @Test fun `i32 with type annotation`() = doTest (
            """
                let a: i32 = 5
            """.trimIndent(),
            listOf (
                CyanVariableDeclaration (
                    name = CyanIdentifierExpression(value = "a"),
                    mutable = false,
                    type = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                    value = CyanNumericLiteralExpression(5, null)
                )
            )

        )

        @Test fun `i32 array with type annotation`() = doTest (
            """
                let a: i32[] = [5, 7]
            """.trimIndent(),
            listOf (
                CyanVariableDeclaration (
                    name = CyanIdentifierExpression(value = "a"),
                    mutable = false,
                    type = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32, array = true)),
                    value = CyanArrayExpression(arrayOf(CyanNumericLiteralExpression(5), CyanNumericLiteralExpression(7)))
                )
            )

        )

        @Test fun `mutable i32 with type annotation`() = doTest (
            """
                var a: i32 = 5
            """.trimIndent(),
            listOf (
                CyanVariableDeclaration (
                    name = CyanIdentifierExpression(value = "a"),
                    mutable = true,
                    type = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                    value = CyanNumericLiteralExpression(5, null)
                )
            )

        )

    }

    @Nested
    inner class FunctionDeclarations {

        @Test fun `implicit void with no args`() = doTest (
            """
                function doSomething() {
                    println("something")
                }
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    signature = CyanFunctionSignature (
                        name = CyanIdentifierExpression("doSomething"),
                        args = emptyList(),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Void)),
                        isExtern = false
                    ),
                    source = CyanSource(emptyList())
                )
            )
        )

        @Test fun `explicit i32 with no args`() = doTest (
            """
                function doSomethingWithInt(): i32 {
                    println("something")
                }
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    signature = CyanFunctionSignature (
                        name = CyanIdentifierExpression("doSomethingWithInt"),
                        args = emptyList(),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                        isExtern = false
                    ),
                    source = CyanSource(emptyList())
                )
            )
        )

        @Test fun `explicit i32 with one arg`() = doTest (
            """
                function doSomethingWithInt(a: str): i32 {
                    println("something")
                }
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    signature = CyanFunctionSignature (
                        name = CyanIdentifierExpression("doSomethingWithInt"),
                        args = listOf(CyanFunctionArgument("a", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str)))),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                        isExtern = false
                    ),
                    source = CyanSource(emptyList())
                )
            )
        )

        @Test fun `explicit i32 with two args`() = doTest (
            """
                function doSomethingWithInt(a: str, b: i64): i32 {
                    println("something")
                }
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    signature = CyanFunctionSignature (
                        name = CyanIdentifierExpression("doSomethingWithInt"),
                        args = listOf (
                            CyanFunctionArgument("a", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str))),
                            CyanFunctionArgument("b", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I64)))
                        ),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                        isExtern = false
                    ),
                    source = CyanSource(emptyList())
                )
            )
        )

        @Test fun `extern explicit i32 with two args`() = doTest (
            """
                extern function doSomethingWithInt(a: str, b: i64): i32
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    signature = CyanFunctionSignature (
                        name = CyanIdentifierExpression("doSomethingWithInt"),
                        args = listOf (
                            CyanFunctionArgument("a", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str))),
                            CyanFunctionArgument("b", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I64)))
                        ),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                        isExtern = true
                    ),
                    source = null
                )
            )
        )

        @Test fun `extern explicit i32 with two args and an attribute`() = doTest (
            """
                [unsafe]
                extern function doSomethingWithInt(a: str, b: i64): i32
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    signature = CyanFunctionSignature (
                        attributes = listOf (
                            CyanFunctionAttribute.Keyword(CyanIdentifierExpression("unsafe"))
                        ),
                        name = CyanIdentifierExpression("doSomethingWithInt"),
                        args = listOf (
                            CyanFunctionArgument("a", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str))),
                            CyanFunctionArgument("b", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I64)))
                        ),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                        isExtern = true
                    ),
                    source = null
                )
            )
        )

        @Test fun `simple receiver`() = doTest (
            """
                function (Person).describe() {
                    print("yes")
                }
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    CyanFunctionSignature (
                        receiver = CyanFunctionReceiver (
                            CyanTypeAnnotation.Reference(CyanIdentifierExpression("Person"))
                        ),
                        name = CyanIdentifierExpression("describe"),
                        args = emptyList(),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Void)),
                        isExtern = false
                    ),
                    source = CyanSource(emptyList())
                )
            )
        )

        @Test fun `one attribute`() = doTest (
            """
                [unsafe]
                function doSomethingWithInt(a: str, b: i64): i32
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    signature = CyanFunctionSignature (
                        attributes = listOf (
                            CyanFunctionAttribute(CyanIdentifierExpression("unsafe"))
                        ),
                        name = CyanIdentifierExpression("doSomethingWithInt"),
                        args = listOf (
                            CyanFunctionArgument("a", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str))),
                            CyanFunctionArgument("b", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I64)))
                        ),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                        isExtern = false
                    ),
                    source = null
                )
            )
        )

        @Test fun `multiple attributes`() = doTest (
            """
                [unsafe, experimental]
                function doSomethingWithInt(a: str, b: i64): i32
            """.trimIndent(),
            listOf (
                CyanFunctionDeclaration (
                    signature = CyanFunctionSignature (
                        attributes = listOf (
                            CyanFunctionAttribute(CyanIdentifierExpression("unsafe")),
                            CyanFunctionAttribute(CyanIdentifierExpression("experimental"))
                        ),
                        name = CyanIdentifierExpression("doSomethingWithInt"),
                        args = listOf (
                            CyanFunctionArgument("a", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Str))),
                            CyanFunctionArgument("b", CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I64)))
                        ),
                        typeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.I32)),
                        isExtern = false
                    ),
                    source = null
                )
            )
        )

    }

    @Nested
    inner class ForLoops {

        @Test fun `with simple expression`() = doTest (
            """
                for n of numbers {
                    nop()
                }
            """.trimIndent(),
            listOf (
                CyanForStatement (
                    CyanIdentifierExpression("n"),
                    CyanIdentifierExpression("numbers"),
                    CyanSource (
                        listOf (
                            CyanFunctionCall(CyanIdentifierExpression("nop"), emptyArray())
                        )
                    )
                )
            )
        )

        @Test fun `with complex expression`() = doTest (
            """
                for element of numbers + letters {
                    nop()
                }
            """.trimIndent(),
            listOf (
                CyanForStatement (
                    CyanIdentifierExpression("element"),
                    CyanBinaryExpression (
                        CyanIdentifierExpression("numbers"),
                        CyanBinaryPlusOperator,
                        CyanIdentifierExpression("letters")
                    ),
                    CyanSource (
                        listOf (
                            CyanFunctionCall(CyanIdentifierExpression("nop"), emptyArray())
                        )
                    )
                )
            )
        )

    }

}
