module math

type Fraction = struct {
    num: i32,
    den: i32
}

function displayFraction(f: Fraction): void {
    print(f.num / f.den)
}

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
