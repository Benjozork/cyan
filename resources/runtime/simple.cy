module main

type Person = struct {
    name: str,
    age: i32
}

function makePerson(name: str, age: i32): Person {
    return Person(name, age)
}

function main() {
    let me = makePerson("Bruh", 19)

    cy_dump_heap_block(me)
}
