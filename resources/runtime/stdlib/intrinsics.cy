module intrinsics

extern function cy_str_char_at(string: str, index: i32): i32

extern function cy_str_len(string: str): i32

extern function cy_str_to_iov(string: str): i32

extern function cy_alloc_buf_iov(size: i32): i32
