module math

function powerOfTwo(n: i32): i32 {
    return n * n
}

function factorial(n: i32): i32 {
    var f: i32 = 0

    if (n >= 1) {
        f = n * factorial(n - 1)
    } else {
        f = 1
    }

    return f
}
