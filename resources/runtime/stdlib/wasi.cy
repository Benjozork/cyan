module wasi

extern function fd_write(fd: i32, iov_ptr: any, num_iovs: i32, nw: i32): i32

extern function fd_read(fd: i32, iov_ptr: i32, num_iovs: i32, nw: i32): i32
