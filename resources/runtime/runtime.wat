(module
    (import "wasi_unstable" "fd_write" (func $fd_write (param i32 i32 i32 i32) (result i32)))

    (memory 1)
    (export "memory" (memory 0))

    (data (i32.const 16) "hello world\n")
    (data (i32.const 32) "What ???\n")

    (func $cy_b_print (param i32)
        (call $fd_write
            (i32.const 1)
            (local.get 0)
            (i32.const 1)
            (i32.const 20)
        )

        drop
    )

    (func $main (export "_start")
        (i32.store (i32.const 0) (i32.const 16)) ;; str0 ptr @ 0x00
        (i32.store (i32.const 4) (i32.const 12)) ;; str0 len @ 0x04
        (i32.store (i32.const 8) (i32.const 32)) ;; str1 ptr @ 0x08
        (i32.store (i32.const 12) (i32.const 9)) ;; str1 len @ 0x12

        (call $cy_b_print (i32.const 0)) ;; print @ 0x00
        (call $cy_b_print (i32.const 8)) ;; print @ 0x08
    )
)
