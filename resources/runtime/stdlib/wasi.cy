module wasi

extern function fd_write(fd: i32, iov_ptr: any, num_iovs: i32, nw: i32): i32
