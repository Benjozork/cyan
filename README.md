# cyan
An experiment in programming languages

## Current features

* variables and arrays (immutable by default)
* structs (immutable)
* complex expressions (parantheses, precedence, PEMDAS)
* basic type inference for variable declarations
* type checking for if statements and variables with type annotations
* arithmetic (+, -, \*, /, %)
* if, else if, else
* functions (local only)
* stack-based interpreter
* JS codegen

## Goals

* modern, flexible language
* flexible but strong typing
* multiple compile targets

## Non-Goals

* interop with existing languages / ecosystems

## Example

```rust
let a = 1847899 + (301111 * 5)    // Complex expressions
let b = "hello"                   // Type inference
let c = ["hi", "hello", b]        // Homogeneous arrays
let d: bool = true                // Type annotations
var e = "what"                    // Mutable variables
e = "so ..."
print(c[2])                       // Array indexing
if (d) {                          // "if - else if - else" chains
    print(c)
} else if (false || d) {
    print("hi !")
} else {
    print("ho !")
}
function hi(a) {                  // Functions
    let array: i32[] = [1, 3, 42, 127, (10 % 3)]
    print("Hello world !")
    print(array)
    print(array.length)
    function hello(b) {           // Local functions
       print("heck")
       print(b)
    }
}
hi(9)
print(hi)

type Person = struct {          // Structs
    name: str,
    age: i32
}
let p: Person = { "James", 18 }
print(p.name)
```

## Architecture

The cyan compiler (`cyanc`) currently works using an internal representation called FIR (Frontend Intermediate Representation).

FIR is a transformed code format on which type-checking and symbol resolution is performed. Therefore, any outputted FIR is valid code.

Here are the compilation steps :

1. The runtime source code (`resources/runtime/runtime.cy`) is compiled using the same steps as normal code;
2. A `FirDocument` (root node of any FIR tree) is created for the program source code, and the symbols from the compiled runtime are inserted into it;
3. The source code is lexed and parsed (`cỳan.compiler.parser`) into an AST (abstract syntax tree);
4. The AST is lowered into FIR (`cyan.lower.ast2fir`), using the previously mentionned `FirDocument`. This is where type-checking and symbol resolutioon happens;
5. (Temporary) The FIR is turned into generated JS.
