/*
Cyan JS runtime

Version 0.1.0
 */

const __VERSION = '0.1.0';

const builtins = {
    print: console.log,
    err: console.error,
    strlen: (string) => string.length
};

// CYANC_INSERT_STDLIB_HERE
