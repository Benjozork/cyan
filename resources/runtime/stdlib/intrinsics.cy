module intrinsics

extern function cy_malloc(size: i32): i32

extern function cy_free(pointer: any): void

extern function cy_str_char_at(string: str, index: i32): i32

extern function cy_str_char_at_as_str(string: str, index: i32): str

extern function cy_str_len(string: str): i32

extern function cy_str_cat(first: str, second: str): str

extern function cy_str_to_iov(string: str): i32

extern function cy_str_cmp(first: str, second: str): i32

extern function cy_iov_to_str(iov_ptr: i32): str

extern function cy_alloc_buf_iov(size: i32): i32

extern function cy_iov_get_buf(iov_ptr: i32): i32

extern function cy_iov_set_buf(iov_ptr: i32, new_buf_ptr: i32): void

extern function cy_iov_get_len(iov_ptr: i32): i32

extern function cy_iov_set_len(iov_ptr: i32, new_len: i32): void

extern function cy_init_heap(): void

extern function cy_dump_mem(start: i32, end: i32): void

extern function cy_dump_heap_block(pointer: any): void
