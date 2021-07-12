module reflection

type Function = struct {
    name: str,
    attributes: str[],
    arguments: str[]
}

function make_function_box(name: str, attributes: any[], arguments: any[]): Function {
    return Function(name, attributes, arguments)
}
