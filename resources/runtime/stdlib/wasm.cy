module wasm

[wasm_instructions = [
    "local.get $addr",
    "i32.load"
]]
extern function load32(addr: i32): i32

[wasm_instructions = [
    "local.get $addr",
    "i32.load"
]]
extern function loadstr(addr: i32): str

[wasm_instructions = [
    "local.get $addr",
    "local.get $value",
    "i32.store"
]]
extern function store32(addr: i32, value: i32)

[wasm_instructions = [
    "local.get $struct_ptr",
    "local.get $index",
    "i32.const 4",
    "i32.mul",
    "i32.add"
]]
extern function struct_member_addr(struct_ptr: any, index: i32): i32

[wasm_instructions = [
    "local.get $ptr"
]]
extern function pointer_to_str(ptr: i32): str
