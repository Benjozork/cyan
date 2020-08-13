module __runtime__

extern function print(content: any): void

extern function err(content: any): void

extern function strlen(string: str): i32

function println(content: str): void {
    print(content + "\n")
}
