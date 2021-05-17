Compiler
The ultimate goal of this project is to implement a simple C- language compiler. The program accepts the source code of C- as input and generates output in a readable format based on different command line options. Three command line options should be implemented for this project:
	(1) -a: perform syntactic analysis and output an abstract syntax tree (.abs)
	(2) -s: perform type checking and output symbol tables (.sym)
	(3) -c: compile and output TM assembly language code (.tm)


/********** Building Instructions **********/
To build the program, type "make" in the current directory, which will generate an executable called "CM".
To rebuild the program, type "make clean" and "make" again.


/********** Tests **********/
All test files are provided in the directory "test", to run tests, 
type:
	"java -classpath /usr/share/java/cup.jar:. CM test/[filename.cm] [-a | -s | -c]"

	Test files: [0-9.cm]


/********** Outputs **********/
-a will generate a .ast file in the root directory,
-s will generate a .sym file in the root directory,
-c will generate a .tm file in the root directory.


/********** TM simulator **********/
To run the .tm file:
type:
	./tm [0-9].tm


@author: Xiaotian Dai
@date: 2021/03/23