module primitive

import intrinsics

function (str).length(): i32 {
    return cy_str_len(this)
}
