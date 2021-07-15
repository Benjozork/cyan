module primitive

import intrinsics
import io
import wasm

function (str).length(): i32 {
    return cy_str_len(this)
}

function (str).charAt(index: i32): i32 {
    return cy_str_char_at(this, index)
}

function (str).contains(other: str): bool {
    var match = false

    let thisLen = this.length()
    let otherLen = other.length()

    let lastIndex = thisLen - 1

    var startIndex = 0
    var numSameChars = 0
    while (startIndex + (otherLen - 1)) < lastIndex + 1 {
        var idx = 0
        while idx < otherLen {
            var charAtIdx = this.charAt(startIndex + idx)
            var shouldBeAtIdx = other.charAt(idx)

            if (charAtIdx == shouldBeAtIdx) {
                numSameChars = numSameChars + 1
            } else {
                idx = otherLen
            }

            idx = idx + 1
        }

        if (numSameChars == otherLen) {
            match = true
            startIndex = lastIndex + 1
        } else {
            startIndex = startIndex + 1
        }
    }

    return match
}

function (str).substring(from: i32, to: i32): str {
    let this_length = this.length()

    if (from < 0) {
        println("str::substring: from < 0")
        wasm_trap()
    }

    if (to > this_length - 1) {
        println("str::substring: to > length - 1")
        wasm_trap()
    }

    if (from > to) {
        println("str::substring: from > to")
        wasm_trap()
    }

    let new_string_ptr = cy_malloc(to - from + 1)

    mem_copy(to_pointer(this), new_string_ptr, to - from + 1)

    return pointer_to_str(new_string_ptr)
}

function (str).startsWith(other: str): bool {
    var ret = true
    let len = this.length()

    var idx = 0
    while idx < this.length() && idx < other.length() {
        if (this[idx] != other[idx]) {
            ret = false
        }
        idx = idx + 1
    }

    return ret
}

function (i32).toString(base: i32): str {
    let convertString = "0123456789ABCDEF"

    var ret = ""
    if (this < base) {
        ret = convertString[this]
    } else {
        ret = (this / base).toString(base) + convertString[this % base]
    }

    return ret
}
