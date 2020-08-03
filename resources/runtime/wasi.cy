module wasi

type iov = struct {
    buf: i32,
    len: i32
}

extern function fd_write(fd: i32, iovs_ptr: i32, num_iovs: i32, bytes_written: i32)
