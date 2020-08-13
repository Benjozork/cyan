(import "wasi_unstable" "fd_write" (func $fd_write (param i32 i32 i32 i32) (result i32)))

(memory 1)

(export "memory" (memory 0))

;; cyanc_insert_heap_start_here

(func $cy_array_get (param $arr_ptr i32) (param $arr_idx i32) (result i32)
    (block $B0
        get_local $arr_idx
        get_local $arr_ptr
        i32.load
        i32.lt_u
        br_if $B0
        unreachable
    )
    local.get $arr_idx
    i32.const 1
    i32.add
    i32.const 4
    i32.mul
    local.get $arr_ptr
    i32.add
    i32.load
)

(func $cy_init_heap
    (local $curr_block_ptr i32)
    (local $next_block_ptr i32)

    global.get $heap_start
    local.set $curr_block_ptr

    loop $init_blocks
         ;; set block free
         local.get $curr_block_ptr
         i32.const 0
         i32.store8

         ;; calculate next block addr
         local.get $curr_block_ptr
         i32.const 64
         i32.add
         local.set $next_block_ptr

         ;; set next block addr in curr block
         local.get $curr_block_ptr
         i32.const 1
         i32.add
         local.get $next_block_ptr
         i32.store

         ;; if we are not at end, do again
         i32.const 1024
         local.get $curr_block_ptr
         i32.gt_u
         if $continue
            local.get $next_block_ptr
            local.set $curr_block_ptr
            br $init_blocks
        end
    end
)

(func $cy_malloc (result i32)
    (local $block_ptr i32)

    global.get $heap_start
    local.set $block_ptr

    loop $search (result i32)
        ;; find if block is free
        local.get $block_ptr

        ;; break if block is not free
        i32.load8_s
        i32.const 0
        i32.ne
        if $not_free
            ;; read ptr to next block
            i32.const 1
            local.get $block_ptr
            i32.add
            i32.load

            local.set $block_ptr

            br $search
        end

        ;; return block if it is free

        local.get $block_ptr
        i32.const 1
        i32.store8

        local.get $block_ptr
        i32.const 5
        i32.add
    end
)

(func $cy_str_len (param $str i32) (result i32)
    (local $curr_idx i32)

    local.get $str
    local.set $curr_idx

    (loop $check_length
        local.get $curr_idx
        i32.load8_u
        if $continue
            local.get $curr_idx
            i32.const 1
            i32.add
            local.set $curr_idx
            br $check_length
        end
    )

    local.get $curr_idx
    local.get $str
    i32.sub
)

(func $cy_str_copy (param $src i32) (param $dest i32)
    (local $src_len i32)
    (local $curr_src_char i32)
    (local $curr_dest_char i32)

    local.get $src
    local.set $curr_src_char

    local.get $dest
    local.set $curr_dest_char

    ;; find src len
    local.get $src
    call $cy_str_len
    local.set $src_len

    loop $copy
        local.get $curr_dest_char
        local.get $curr_src_char
        i32.load
        i32.store8

        ;; increment src char
        local.get $curr_src_char
        i32.const 1
        i32.add
        local.set $curr_src_char

        ;; break if lower than len
        local.get $src_len
        local.get $curr_src_char
        local.get $src
        i32.sub
        i32.gt_u
        if $continue
            ;; increment dest char
            local.get $curr_dest_char
            i32.const 1
            i32.add
            local.set $curr_dest_char

            br $copy
        end
    end
)

(func $cy_str_cat (param $first i32) (param $second i32) (result i32)
    (local $new_first_addr i32)
    (local $new_second_addr i32)
    (local $first_len i32)
    (local $second_len i32)

    local.get $first
    call $cy_str_len
    local.set $first_len

    local.get $second
    call $cy_str_len
    local.set $second_len

    call $cy_malloc
    local.set $new_first_addr

    local.get $first
    local.get $new_first_addr
    call $cy_str_copy

    local.get $first_len
    local.get $new_first_addr
    i32.add
    local.set $new_second_addr ;; dest addr = (first_len + new_first_addr)

    local.get $second
    local.get $new_second_addr
    call $cy_str_copy

    local.get $new_second_addr
    local.get $second_len
    i32.add
    i32.const 1
    i32.add
    i32.const 0
    i32.store8 ;; add null terminator

    local.get $new_first_addr
)

(func $cy_str_to_iov (param $str i32) (result i32)
    (local $addr i32)

    call $cy_malloc
    i32.const 3
    i32.add
    local.set $addr

    local.get $addr
    local.get $str
    i32.store

    local.get $addr
    i32.const 4
    i32.add
    local.get $str
    call $cy_str_len
    i32.store

    local.get $addr
)

(func $cy_dump_mem
    i32.const 0
    i32.const 0
    i32.store
    i32.const 4
    i32.const 512
    i32.store
    (call $print (i32.const 0))
)

(func $print (param $str i32)
    (local $iov i32)

    local.get $str
    call $cy_str_to_iov
    local.set $iov

    (call $fd_write
        (i32.const 1)
        (local.get $iov)
        (i32.const 1)
        (i32.const 20)
    )

    drop
)

;; cyanc_insert_here

;; cyanc_insert_prealloc_here
