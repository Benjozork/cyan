module main

import math

type Person = struct {
   name: str,
   age: i32
}

let a = 1847899 + (301111 * 5)
var b = "hello"
print(b)
b = "hi !"
print(b)
let c = ["hi", "hello", b]
let d: bool = true
let e = c[0]
let f = b
let g: Person = { "James", 18 }
let h: i32 = strlen(c[1])
print(g.name)
print(g)
let i: i32 = g.age
print(h)
if (d) {
    print(c[2])
} else if (false || d) {
    print("hi !")
} else {
    print("ho !")
}
function hello(a: str): str {
   print("Hello, stranger ! here's the value:")
   print(a)
   return a
}
print(hello("<dumb value>>"))
print(strlen("Hamza"))
print(powerOfTwo(5))
print(factorial(5))

let w: Fraction = { 2, 3 }
displayFraction(w)

let z = [56, 24, 0]
let y = (25 * 25 * 25 * 25 + 5 - 1)

let vv = false || z[0] < 60
print(vv)
if (false) {
    print("wow")
} else {
    print("why ?")
}
if (true) {
    print("hi !")
} else {
    print("fuck")
}
