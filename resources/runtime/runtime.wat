(import "wasi_unstable" "fd_write" (func $fd_write (param i32 i32 i32 i32) (result i32)))
(import "wasi_unstable" "fd_read" (func $fd_read (param i32 i32 i32 i32) (result i32)))

(start $main)

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

(func $cy_array_set (param $arr_ptr i32) (param $arr_idx i32) (param $new_value i32)
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
    local.get $new_value
    i32.store
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

        memory.size
        i32.const 65536
        i32.mul

        global.get $heap_start
        i32.sub

        local.get $curr_block_ptr
        i32.gt_u
        if $continue
            local.get $next_block_ptr
            local.set $curr_block_ptr
            br $init_blocks
        end
    end
)

(func $cy_merge_heap_block_with_previous (param $block_ptr i32) (param $prev_block_ptr i32)
    (local $new_next_block_ptr i32)

    ;; find new next block ptr
    local.get $block_ptr
    i32.const 1
    i32.add
    i32.load
    local.set $new_next_block_ptr

    ;; rewrite prev block nextaddr
    local.get $prev_block_ptr
    i32.const 1
    i32.add
    local.get $new_next_block_ptr
    i32.store

    ;; remove block start
    local.get $block_ptr
    i32.const 0
    i32.store8

    local.get $block_ptr
    i32.const 1
    i32.add
    i32.const 0
    i32.store
)

(func $cy_malloc (param $size i32) (result i32)
    (local $block_ptr i32)
    (local $next_block_ptr i32)

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

            local.tee $block_ptr
            i32.eqz
            if $end_of_heap
                i32.const 1
                i32.const 16
                call $cy_str_to_iov_panic
                i32.const 1
                i32.const 0
                call $fd_write
                unreachable
            end

            br $search
        end
        ;; check if size > 60
        local.get $size
        i32.const 60
        i32.gt_u
        if $multi_block
            ;; check if next block is free
            i32.const 1
            local.get $block_ptr
            i32.add
            i32.load
            local.tee $next_block_ptr
            i32.load8_u
            i32.eqz
            if $next_block_free
                local.get $next_block_ptr
                local.get $block_ptr
                call $cy_merge_heap_block_with_previous

                br $multi_block
            end
            unreachable
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

(func $cy_free (param $ptr i32)
    local.get $ptr
    i32.const 5
    i32.sub
    i32.const 0
    i32.store8
)

(func $cy_str_char_at (param $str i32) (param $idx i32) (result i32)
    (block $B0
        local.get $idx
        local.get $str
        call $cy_str_len
        i32.lt_u
        br_if $B0
        unreachable
    )

    local.get $str
    local.get $idx
    i32.add
    i32.load8_u
)

(func $cy_str_char_at_as_str (param $str i32) (param $idx i32) (result i32)
    (local $new_str_ptr i32)

    ;; allocate new string
    i32.const 1
    call $cy_malloc
    local.tee $new_str_ptr

    ;; load char
    local.get $str
    local.get $idx
    i32.add
    i32.load8_u

    i32.store8

    local.get $new_str_ptr
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
    local.tee $second_len

    local.get $first_len
    i32.add
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

    local.get $str
    call $cy_str_len
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

(func $cy_str_to_iov_panic (param $str i32) (result i32)
    (local $addr i32)

    i32.const 0
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

(func $cy_str_cmp (param $first i32) (param $second i32) (result i32)
    (local $are_equal i32)
    (local $did_check i32)
    (local $curr_idx i32)
    (local $first_max_idx i32)

    i32.const 0
    local.set $are_equal

    i32.const 0
    local.set $curr_idx

    ;; get length of first
    local.get $first
    call $cy_str_len
    i32.const 1
    i32.sub
    local.set $first_max_idx

    ;; early bail if it's not the same length as second
    local.get $second
    call $cy_str_len
    i32.const 1
    i32.sub
    local.get $first_max_idx
    i32.eq
    if $do_check
        loop $check_char
            ;; check if done
            local.get $curr_idx
            local.get $first_max_idx
            i32.le_u
            if $check_iter
                local.get $first
                local.get $curr_idx
                i32.add
                i32.load8_u
                local.get $second
                local.get $curr_idx
                i32.add
                i32.load8_u
                i32.eq
                if $next_iter
                    local.get $curr_idx
                    i32.const 1
                    i32.add
                    local.set $curr_idx
                    br $check_char
                end

                ;; char not equal - break out of $do_check
                br $do_check
            end

            ;; done checking
            i32.const 1
            local.set $are_equal
        end
    end

    local.get $are_equal
    i32.eqz
)

(func $cy_iov_to_str (param $iov_ptr i32) (result i32)
    local.get $iov_ptr
    i32.load
)

(func $cy_alloc_buf_iov (param $size i32) (result i32)
    (local $buf_ptr i32)
    (local $iov_ptr i32)

    local.get $size
    call $cy_malloc
    local.set $buf_ptr
    i32.const 4
    call $cy_malloc
    i32.const 3
    i32.add
    local.set $iov_ptr

    local.get $iov_ptr
    local.get $buf_ptr
    i32.store

    local.get $iov_ptr
    i32.const 4
    i32.add
    local.get $size
    i32.store

    local.get $iov_ptr
)

(func $cy_iov_get_buf (param $iov_ptr i32) (result i32)
    local.get $iov_ptr
    i32.load
)

(func $cy_iov_set_buf (param $iov_ptr i32) (param $new_buf_ptr i32)
    local.get $iov_ptr
    local.get $new_buf_ptr
    i32.store
)

(func $cy_iov_get_len (param $iov_ptr i32) (result i32)
    local.get $iov_ptr
    i32.const 4
    i32.add
    i32.load
)

(func $cy_iov_set_len (param $iov_ptr i32) (param $new_len i32)
    local.get $iov_ptr
    i32.const 4
    i32.add
    local.get $new_len
    i32.store
)

(func $cy_dump_mem (param $from i32) (param $to i32)
    i32.const 0
    local.get $from
    i32.store
    i32.const 4
    local.get $to
    local.get $from
    i32.sub
    i32.store

    i32.const 1
    i32.const 0
    i32.const 1
    i32.const 0
    call $fd_write
    drop
)

(func $cy_dump_heap_block (param $ptr i32)
    ;; block start
    local.get $ptr

    ;; block end
    local.get $ptr
    i32.const 4
    i32.sub
    i32.load
    i32.const 1
    i32.sub

    call $cy_dump_mem
)

;; cyanc_insert_here

;; cyanc_insert_prealloc_here
