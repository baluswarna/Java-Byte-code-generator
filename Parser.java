/* 		OO PARSER AND BYTE-CODE GENERATOR FOR TINY PL
 
Grammar for TinyPL (using EBNF notation) is as follows:

 program ->  decls stmts end
 decls   ->  int idlist ;
 idlist  ->  id { , id } 
 stmts   ->  stmt [ stmts ]
 cmpdstmt->  '{' stmts '}'
 stmt    ->  assign | cond | loop
 assign  ->  id = expr ;
 cond    ->  if '(' rexp ')' cmpdstmt [ else cmpdstmt ]
 loop    ->  while '(' rexp ')' cmpdstmt  
 rexp    ->  expr (< | > | =) expr
 expr    ->  term   [ (+ | -) expr ]
 term    ->  factor [ (* | /) term ]
 factor  ->  int_lit | id | '(' expr ')'
 
Lexical:   id is a single character; 
	      int_lit is an unsigned integer;
		 equality operator is =, not ==

Sample Program: Factorial
 
int n, i, f;
n = 4;
i = 1;
f = 1;
while (i < n) {
  i = i + 1;
  f= f * i;
}
end

   Sample Program:  GCD
   
int x, y;
x = 121;
y = 132;
while (x != y) {
  if (x > y) 
       { x = x - y; }
  else { y = y - x; }
}
end

 */

import java.util.ArrayList; 
import java.util.LinkedHashMap; 
import java.util.List; 
import java.util.Map; 
 
public class Parser { 
    @SuppressWarnings("unused") 
    public static void main(String[] args)  { 
        System.out.println("Enter program and terminate with 'end'!\n"); 
        //int no; while((no = Lexer.lex()) != 0) System.out.println(Token.toString(no)); 
        Lexer.lex(); 
        Program p = new Program(); 
        Code.output(); 
    } 
} 
 
class Program { 
    private Decls d; 
    private Stmts s; 
      
    public Program(){ 
        d = new Decls(); 
        d.decls(); 
        s = new Stmts(); 
        s.stmts(); 
    } 
} 
 
class Decls { 
    private Idlist i; 
     
    public void decls(){ 
        i = new Idlist(); 
        i.idlist(); 
    } 
} 
 
class Idlist { 
    public void idlist(){ 
        if(Lexer.nextToken == Token.KEY_INT){ 
            Lexer.lex(); 
            while(Lexer.nextToken == Token.ID){ 
                Code.add(Lexer.ident); 
                Lexer.lex(); 
                if(Lexer.nextToken == Token.SEMICOLON) break; 
                if(Lexer.nextToken != Token.COMMA){ 
                    Lexer.error("Comma Expected Between Identifiers.\nProgram Terminated."); 
                } 
                Lexer.lex(); 
            } 
        } 
        else{ 
            Lexer.error("Invalid ID List.\nProgram Terminated."); 
        } 
    } 
} 
 
class Stmt { 
    private Assign a; 
    private Cond c; 
    private Loop l; 
     
    public boolean stmt(){ 
        Lexer.lex(); 
        if(Lexer.nextToken == Token.ID){ //ASSIGN 
            a = new Assign(); 
            a.assign(); 
        } 
        else if(Lexer.nextToken == Token.KEY_IF){ //CONDITION 
            c = new Cond(); 
            c.cond(); 
        } 
        else if(Lexer.nextToken == Token.KEY_WHILE){ //LOOP 
            l = new Loop(); 
            l.loop(); 
        } 
        else if(Lexer.nextToken == Token.KEY_END){ //END 
            Code.generate("return"); 
            return false; 
        } 
        return true; 
    } 
}  
 
class Stmts { 
    private Stmt s; 
     
    public void stmts(){ 
        s = new Stmt(); 
        while(s.stmt()){ 
            s = new Stmt(); 
        } 
    } 
} 
 
class Assign { 
    private Expr e; 
     
    public void assign(){ 
        int index = Code.getPointer(Lexer.ident); 
        Lexer.lex(); //EQUALS OPERATOR 
        Lexer.lex(); 
        e = new Expr(); 
        e.expr(); 
        String code = "istore_" + index; 
        Code.generate(code); 
    } 
} 
 
class Cond { 
    public boolean cond(){ 
    	private Rexpr rex;
    	private Cmpdstmt cmst;
    	if(Lexer.nextToken == Token.KEY_IF){
    		  Lexer.lex();
    		if(Lexer.nextToken == Token.LEFT_PAREN){
    			Lexer.lex();
    			rex = new Rexpr();
    			rex.rexp();
    			Lexer.lex(); // for ')'
    			Lexer.lex();
    			cmst = new Cmpdstmt();
    			cmst.cmpdstmt();
    			Lexer.lex();
    			if(Lexer.nextToken == Token.KEY_ELSE){
    				Lexer.lex();
    				cmst = new Cmpdstmt();
        			cmst.cmpdstmt();
    			}
    		}
    	}
        return true; 
    } 
} 
 
class Loop { 
    public boolean loop(){ 
        return true; 
    } 
} 
 
class Cmpdstmt { 
	private Stmts s;
    public boolean cmpdstmt(){ 
    	if(Lexer.nextToken== Token.LEFT_BRACE){
    		Lexer.lex();
    		s= new Stmts();
    		s.stmts();
    		Lexer.lex();
    		}
    	return true;
     }
 } 
 
 
class Rexpr { 
	private Expr e;
    public boolean rexp(){ 
    	e = new Expr();
    	if(Lexer.nextToken == Token.LESSER_OP || Lexer.nextToken == Token.GREATER_OP || Lexer.nextToken == Token.ASSIGN_OP || Lexer.nextToken == Token.NOT_EQ){
    		int next= Lexer.nextToken;
    		Lexer.lex();
        	e = new Expr();
        	if(next== Token.LESSER_OP) Code.generate("if_icmpge");
        	else if(next== Token.GREATER_OP) Code.generate("if_icmple")
        	else if(next== Token.ASSIGN_OP) Code.generate("if_icmpne");
        	else(next==Token.NOT_EQ)Code.generate("if_icmpeq");
    	}
    	
        return true; 
    } 
} 
 
class Expr { 
    private Expr e; 
    private Term t; 
     
    public boolean expr(){ 
        t = new Term(); 
        if(t.term()) return true; 
        if(Lexer.nextToken == Token.SEMICOLON) return true; 
        if(Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP){ 
            int next = Lexer.nextToken; 
            Lexer.lex(); 
            e = new Expr(); 
            e.expr(); 
            if(next == Token.ADD_OP) Code.generate("iadd"); 
            else Code.generate("isub"); 
        } 
        return false; 
    } 
} 
 
class Term {   
    private Term t; 
    private Factor f; 
     
    public boolean term(){ 
        f = new Factor(); 
        f.factor(); 
        Lexer.lex(); 
        if(Lexer.nextToken == Token.SEMICOLON) return true; 
        if(Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP){ 
            int next = Lexer.nextToken; 
            Lexer.lex(); 
            t = new Term(); 
            t.term(); 
            if(next == Token.MULT_OP) Code.generate("imul"); 
            else Code.generate("idiv"); 
        } 
        return false; 
    } 
} 
 
class Factor { 
    private Expr e; 
     
    public void factor(){ 
        if(Lexer.nextToken == Token.INT_LIT){ 
            String code = "iconst_" + Lexer.intValue; 
            Code.generate(code); 
        } 
        else if(Lexer.nextToken == Token.ID){ 
        	            String code = "iload_" + Code.getPointer(Lexer.ident); 
            Code.generate(code); 
        } 
        else if(Lexer.nextToken == Token.LEFT_PAREN){ 
            Lexer.lex(); 
            e = new Expr(); 
            e.expr(); 
            Lexer.lex(); //RIGHT PARENTHESIS 
            System.out.println(Lexer.nextToken); 
            Lexer.lex(); 
        } 
    } 
} 
 
class Code { 
    private static Map<Character, Integer> stack = new LinkedHashMap<Character, Integer>(); 
    private static int stackTop = -1; 
    private static List<String> instructions = new ArrayList<String>(); 
    private static int instructionPointer = -1; 
     
    public static void add(char identifier){ 
        stackTop = stackTop + 1; 
        stack.put(identifier, stackTop); 
    } 
     
    public static int getPointer(char identifier){ 
        return stack.get(identifier); 
    } 
     
    public static void generate(String code){ 
        instructions.add(code); 
        instructionPointer = instructionPointer + 1; 
    } 
     
    public static void output(){ 
        System.out.println(); 
        System.out.println("Java Byte Codes are:"); 
        for(String code : instructions){ 
            System.out.println(code); 
        } 
    } 
}