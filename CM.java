/*
  File Name: CM.java
  To Build: 
  After the scanner, cm.flex, and the parser, cm.cup, have been created.
    javac CM.java
  
  To Run: 
    java -classpath /usr/share/java/cup.jar:. CM [test_file] [-a | -s | -c]
*/

import java.util.*;
import java.io.*;
import absyn.*;

public class CM {
    /*
     * -a: perform syntactic analysis and output an abstract syntax tree (.abs)
     * -s: perform type checking and output symbol tables (.sym)
     * -c : compile and output TM assembly language code (.tm)
     */
    public static final String EXT_AST = ".ast";
    public static final String EXT_SYM = ".sym";
    public static final String EXT_TM = ".tm";

    public static boolean genAST = false;
    public static boolean genSYM = false;
    public static boolean genTM = false;

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 4) {
            System.err.println("Usage: java -classpath /usr/share/java/cup.jar:. CM [test_file] [-a | -s | -c]");
            System.exit(1);
        }

        String filename = "";
        for (String arg : args) {
            if (arg.endsWith(".cm")) {
                filename = arg;
            } else if (arg.equals("-a")) {
                genAST = true;
            } else if (arg.equals("-s")) {
                genSYM = true;
            } else if (arg.equals("-c")) {
                genTM = true;
            }
        }

        try {
            parser p = new parser(new Lexer(new FileReader(filename)));
            p.genAST = genAST;
            p.genSYM = genSYM;
            p.genTM = genTM;
            File file = new File(filename);
            String name = file.getName();
            p.filename = name.substring(0, name.lastIndexOf('.'));
            Object result = (Object) (p.parse().value);
        } catch (Exception e) {
            /* do cleanup here -- possibly rethrow e */
            e.printStackTrace();
        }
    }
}