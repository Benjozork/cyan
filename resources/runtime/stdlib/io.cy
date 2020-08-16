module io

import intrinsics
import wasi

function print(content: str): void {
    fd_write(1, cy_str_to_iov(content), 1, 24)
}

function println(content: str): void {
    print(content + "\n")
}

function read(len: i32): i32 {
    let iov_ptr = cy_alloc_buf_iov(len)

    fd_read(0, iov_ptr, 1, 24)

    return iov_ptr
}
