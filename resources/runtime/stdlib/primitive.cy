module primitive

import intrinsics

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
