// init
@256
D=A
@SP
M=D
// C_PUSH constant 7
@7
D=A
@SP
A=M // A=256
M=D // M=7 // RAM(256)=7
@SP
M=M+1 // SP=257
// C_PUSH constant 8
@8
D=A // D=8
@SP
A=M // A=257
M=D // M=8 // RAM(257) = 8
@SP
M=M+1 // SP=0, RAM(SP) = 257+1=258
// add
@SP
AM=M-1 // A=257, M=257
D=M // D=RAM[257] = 8
A=A-1 // A=257-1=256
M=D+M // M=8+7
