module io

import intrinsics
import primitive
import wasi

function print(content: str): void {
    fd_write(1, cy_str_to_iov(content), 1, 0)
}

function println(content: str): void {
    print(content + "\n")
}

function read_iov(iov_ptr: i32) {
    fd_read(0, iov_ptr, 1, 0)
}

function read_bytes(num_bytes: i32): i32 {
    let iov = cy_alloc_buf_iov(num_bytes)

    read_iov(iov)

    return iov
}

function readln(): str {
    var ret = ""

    while ret[ret.length() - 1] != "\n" {
        let chr = cy_iov_to_str(read_bytes(2))

        ret = cy_str_cat(ret, chr)
    }

    return ret
}
