# Assembly Silos
This Java program implements a set of Assembly Silos, which are independent virtual machines that run a simple assembly language. Each silo runs on its own thread and communicates with other silos through a transfer region. The silos must remain in sync with each other, running exactly one instruction before waiting for every other silo to finish its current instruction.

**Creation of parser, interpreter, and GUI was required for this project** 

## How to run the Program
As usual with these specific Java programs. Use of JavaFX and Java version 17 is required. For ease of use, programs were created using Azul Zulu 17.

## Architecture Overview
Each silo contains a program written in the Assembly Language described below. When the last instruction of a program is executed, execution automatically continues from the first instruction of the program. When a silo writes to a port, it must wait for the silo on the other side to read the value before it can carry on. The same goes for attempting to read a value.

The silos are artificially limited to one instruction per second to facilitate grading. There can be any number of input/output streams of data. Input streams are read by a silo adjacent to it, while output streams are written to by a silo adjacent to it.

## Assembly Language Overview
The Assembly Language used in this program consists of the following instructions:

**NOOP**: An instruction that does nothing.<br>
**MOVE** [SRC] [DST]: Reads a value from [SRC] and writes the result to [DST].<br>
**SWAP**: Switches the value of the ACC register with the value of the BAK register.<br>
**SAVE**: Writes the value from the ACC register onto the BAK register.<br>
**ADD** [SRC]: Adds the value of [SRC] to the value in the ACC register.<br>
**SUB** [SRC]: Subtracts the value of [SRC] from the value in the ACC register.<br>
**NEGATE**: Negates the value of the ACC register, zero remains zero.<br>
**JUMP** [LABEL]: Jumps control of the program to the instruction following the given [LABEL].<br>
**JEZ** [LABEL]: Jumps control of the program to the instruction following the given [LABEL] if the value in the ACC register is equal to zero.<br>
**JNZ** [LABEL]: Jumps control of the program to the instruction following the given [LABEL] if the value in the ACC register is not equal to zero.<br>
**JGZ** [LABEL]: Jumps control of the program to the instruction following the given [LABEL] if the value in the ACC register is greater than zero.<br>
**JLZ** [LABEL]: Jumps control of the program to the instruction following the given [LABEL] if the value in the ACC register is less than zero.<br>
**JRO** [SRC]: Jumps control of the program to the instruction specified by the offset, which is the value contained within [SRC].<br>
## Program Inputs 
This program requires user input through the command line in order to construct the initial state of the program. The input must be in the following format:
An example input text file is provided in the main directory for use. You must copy and paste into the program console, then use an addition "." to let the program know you are done.


The first line indicates the number of rows and columns in the grid. The subsequent lines after that are the instructions which must be loaded into the first silo. Note that the silos will be given row by row. When you see the keyword "END", that means the instructions for that silo are complete. You then move on to the next silo and repeat the process.

Next, you must read in the input/output streams. An input stream will follow the keyword "INPUT" and will be followed by the coordinates of the stream. Note that these coordinates will have one out-of-bounds coordinate. This is to signify that it is not part of the normal silo grid but rather adjacent to it. In the example above, the input stream would go above the silo grid and over the second column. The values of the stream are given after these coordinates.

An output stream will follow the keyword "OUTPUT" and only coordinates will be given. Both streams will end with the keyword "END". You can assume that this input will be well-formed.

---
![](/resources/Recording_2023-07-25_162845_AdobeExpress.gif)