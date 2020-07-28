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
