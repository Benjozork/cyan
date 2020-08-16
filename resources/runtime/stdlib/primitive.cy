module primitive

import intrinsics

function (str).length(): i32 {
    return cy_str_len(this)
}

function (str).charAt(index: i32): i32 {
    return cy_str_char_at(this, index)
}
