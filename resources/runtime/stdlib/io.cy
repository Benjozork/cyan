module io

import intrinsics
import wasi

function print(content: str): void {
    fd_write(1, cy_str_to_iov(content), 1, 24)
}

function println(content: str): void {
    print(content + "\n")
}
