module main

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
let e = c[0]
let f = b
function hello(a: str): str {
   print("Hello, stranger ! here's the value:")
   print(a)
   return a
}
