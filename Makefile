JAVA=java
JAVAC=javac
JFLEX=jflex
CLASSPATH=-classpath /usr/share/java/cup.jar:.
#CUP=$(JAVA) $(CLASSPATH) java_cup.Main <
CUP=cup
CC = gcc
CFLAGS = 

all: CM.class tm

CM.class: absyn/*.java parser.java sym.java Lexer.java symbol/*.java SemanticAnalyzer.java TypeCheck.java CodeGeneration.java CM.java

tm: tm.c
	$(CC) $(CFLAGS) tm.c -o tm

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

# -dump
parser.java: cm.cup
	$(CUP) -expect 3 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class symbol/*.class *.ast *.sym *.tm tm *~

