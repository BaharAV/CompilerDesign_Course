import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;


public class Compiler {

    private static String keywords[] = new String[]{"void", "int", "char", "float", "string", "bool",
            "return", "break", "if", "do", "while", "for", "switch", "case", "default", "continue",
            "define", "#include", "enum", "struct", "union", "false", "true", "typdef", "unsigned",
            "repeat", "until", "override", "else", "in", "sizeof", "null"};

    class Token {
        TokenType type;
        String value;

        private Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            if (type == TokenType.error) {
                return ("<") + type + (",\'") + value + "',<" + lines + ">,<" + chars + (">>");
            } else {
                return ("<") + type + (",\'") + value + ("\'>");
            }
        }
    }

    class Symbol {
        String name;
        String type;
        String value;
        String addrs;

        Symbol(String name, String type, String value, String addrs) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.addrs = addrs;
        }

        @Override
        public String toString() {
            return "{ " + name + " , " + type + " , " + value + " , " + addrs + " }";
        }
    }

    private String switchon;
    private boolean infunc = false;
    private int numfunc = -1;
    private char ch;
    private int lines = 1;
    private int chars = 1;
    private int pc = 1;
    private int adrs = 0;
    private int linesofgrammer = 50;
    private int statess = 37, varr = 20, linee = 17;
    private boolean[] hasprototype = new boolean[100];
    private String[][] grammers = new String[100][100];
    private String[][] begrammers = new String[100][100];
    private String[][] firsts = new String[100][100];
    private String[][] follows = new String[100][100];
    private ArrayList<String> temp = new ArrayList<>();
    private ArrayList<Symbol> symbols = new ArrayList<>();
    private String[][] code = new String[1000][4];
    private Stack<Token> parsstack = new Stack();
    private Stack<Integer> beparsstack = new Stack();
    private Stack ss = new Stack();
    private Stack ssforpc = new Stack();
    private Stack ssfornum = new Stack();
    private Stack ssforline = new Stack();
    private int[][] parstable;
    private ArrayList<ArrayList<String>> rhst = new ArrayList<>();
    private ArrayList<ArrayList<String>> berhst = new ArrayList<>();
    private String[][] besides = new String[linee][2];
    private ArrayList<ArrayList<String>> predicts = new ArrayList<>();
    private ArrayList<String> terminals = new ArrayList<>();
    private ArrayList<String> beterminals = new ArrayList<>();
    private ArrayList<String> vars = new ArrayList<>();
    private String[][] bestates = new String[statess][varr];
    private ArrayList<ArrayList> funcymbols = new ArrayList<>();


    enum TokenType {keyword, integernum, realnum, character, stringliteral, error, identifier, booleanex, specialtoken, include, end, variable, semantic}

    private int getTemp() {
        temp.add(null);
        return temp.size();
    }

    private boolean iskeyword(String word) {
        for (String keyword : keywords)
            if (word.equals(keyword))
                return true;
        return false;
    }

    private Token scanner(FileReader inf) throws IOException {
        Token token = new Token(null, null);
        if ((ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122)) {
            token.value = "";
            do {
                token.value += ch;
                ch = (char) inf.read();
                chars++;
            } while ((ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122) || (ch >= 48 && ch <= 57));
            if (iskeyword(token.value)) {
                token.type = TokenType.keyword;
            } else {
                token.type = TokenType.identifier;
            }
            return token;
        }
        switch (ch) {
            case '`':
                token.value = "`";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case '#':
                for (int i = 0; i < 7; i++) {
                    ch = (char) inf.read();
                    chars++;
                }
                token.type = TokenType.include;
                while (ch != '<') {
                    ch = (char) inf.read();
                    chars++;
                }
                ch = (char) inf.read();
                chars++;
                token.value = "";
                while (ch != '>') {
                    token.value += ch;
                    ch = (char) inf.read();
                    chars++;
                }
                ch = (char) inf.read();
                chars++;
                return token;
            case ';':
                token.value = ";";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case '+':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                if (ch == '+') {
                    token.value = "++";
                    ch = (char) inf.read();
                    chars++;
                } else if (ch == '=') {
                    token.value = "+=";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "+";
                }
                return token;
            case '-':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                if (ch == '-') {
                    token.value = "--";
                    ch = (char) inf.read();
                    chars++;
                } else if (ch == '=') {
                    token.value = "-=";
                    ch = (char) inf.read();
                    chars++;
                } else if (ch == '>') {
                    token.value = "->";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "-";
                }
                return token;
            case '*':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                if (ch == '*') {
                    token.value = "**";
                    ch = (char) inf.read();
                    chars++;
                } else if (ch == '=') {
                    token.value = "*=";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "*";
                }
                //*:derefrence
                return token;
            case '%':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                if (ch == '=') {
                    token.value = "%=";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "%";
                }
                return token;
            case '<':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                if (ch == '<') {
                    ch = (char) inf.read();
                    chars++;
                    if (ch == '=') {
                        token.value = "<<=";
                    } else {
                        token.value = "<<";
                    }
                } else if (ch == '=') {
                    token.value = "<=";
                } else {
                    token.value = "<";
                }
                return token;
            case '>':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                if (ch == '>') {
                    ch = (char) inf.read();
                    chars++;
                    if (ch == '=') {
                        token.value = ">>=";
                        ch = (char) inf.read();
                        chars++;
                    } else {
                        token.value = ">>";
                    }
                } else if (ch == '=') {
                    token.value = ">=";
                } else {
                    token.value = ">";
                }
                return token;
            case '^':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                if (ch == '=') {
                    token.value = "^=";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "^";
                }
                return token;
            case '!':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                if (ch == '=') {
                    token.value = "!=";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "!";
                }
                return token;
            case '?':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                token.value = "?:";
                ch = (char) inf.read();
                chars++;
                return token;
            case '~':
                token.value = "~";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case '.':
                token.value = ".";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case ',':
                token.value = ",";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case '=':
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                if (ch == '<') {
                    token.value = "=<";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "=";
                }
                return token;
            case '&':
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                if (ch == '&') {
                    token.value = "&&";
                    ch = (char) inf.read();
                    chars++;
                } else if (ch == '=') {
                    token.value = "&=";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "&";
                }
                return token;
            case '|':
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                if (ch == '|') {
                    token.value = "||";
                    ch = (char) inf.read();
                    chars++;
                } else if (ch == '=') {
                    token.value = "|=";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.value = "|";
                }
                return token;
            case ':':
                ch = (char) inf.read();
                chars++;
                if (ch == '=') {
                    token.type = TokenType.specialtoken;
                    token.value = ":=";
                    ch = (char) inf.read();
                    chars++;
                } else {
                    token.type = TokenType.specialtoken;
                    token.value = ":";
                }
                return token;
            case '_':
                ch = (char) inf.read();
                chars++;
                token.type = TokenType.specialtoken;
                token.value = "_>";
                ch = (char) inf.read();
                chars++;
                return token;
            case '[':
                token.value = "[";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case ']':
                token.value = "]";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case '{':
                token.value = "{";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case '}':
                token.value = "}";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case '(':
                token.value = "(";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case ')':
                token.value = ")";
                token.type = TokenType.specialtoken;
                ch = (char) inf.read();
                chars++;
                return token;
            case '/':
                ch = (char) inf.read();
                chars++;
                if (ch != '*') {
                    token.type = TokenType.specialtoken;
                    if (ch == '/') {
                        token.value = "//";
                        ch = (char) inf.read();
                        chars++;
                    } else if (ch == '=') {
                        token.value = "/=";
                        ch = (char) inf.read();
                        chars++;
                    } else {
                        token.value = "/";
                    }
                    return token;
                } else {
                    do {
                        do {
                            ch = (char) inf.read();
                            chars++;
                            if (ch == '$') {
                                token.type = TokenType.error;
                                token.value = "unfinished comment";
                                System.out.print("unfinished comment ");
                                return token;
                            }
                            if (ch == '\n') {
                                lines++;
                                chars = 1;
                            }
                        } while (ch != '*');
                        while (ch == '*') {
                            ch = (char) inf.read();
                            chars++;
                        }
                    } while (ch != '/');
                    ch = (char) inf.read();
                    chars++;
                    return scanner(inf);
                }
            case '"':
                ch = (char) inf.read();
                chars++;
                if (ch == '\"') {
                    ch = (char) inf.read();
                    chars++;
                    ch = (char) inf.read();
                    chars++;
                    while (ch != '\"') {
                        ch = (char) inf.read();
                        chars++;
                    }
                    ch = (char) inf.read();
                    chars++;
                    ch = (char) inf.read();
                    chars++;
                    ch = (char) inf.read();
                    chars++;
                    return scanner(inf);
                } else {
                    token.value = "\"";
                    token.value += ch;
                    while (ch != '\"') {
                        ch = (char) inf.read();
                        chars++;
                        if (ch == '\\') {
                            ch = (char) inf.read();
                            chars++;
                        } else {
                            token.value += ch;
                        }
                    }
                    token.type = TokenType.stringliteral;
                    ch = (char) inf.read();
                    chars++;
                    return token;
                }
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '0':
                token.value = "";
                do {
                    token.value += ch;
                    ch = (char) inf.read();
                    chars++;
                } while (ch >= 48 && ch <= 57);
                if (ch == '.') {
                    token.type = TokenType.realnum;
                    do {
                        token.value += ch;
                        ch = (char) inf.read();
                        chars++;
                    } while (ch >= 48 && ch <= 57);
                } else {
                    token.type = TokenType.integernum;
                }
                return token;
            case '\'':
                token.type = TokenType.character;
                ch = (char) inf.read();
                chars++;
                token.value = "\'" + ch + "\'";
                ch = (char) inf.read();
                chars++;
                ch = (char) inf.read();
                chars++;
                return token;
            case '\n':
                ch = (char) inf.read();
                chars = 1;
                lines++;
                return scanner(inf);
            case '$':
                token.value = "$";
                token.type = TokenType.end;
                return token;
            case '\t':
            case ' ':
            default:
                ch = (char) inf.read();
                chars++;
                return scanner(inf);
        }
    }

    private Token reader(FileReader inf) throws IOException {
        return scanner(inf);
    }

    private void fillParseTable() {
        parstable = new int[vars.size()][terminals.size()];
        for (int i = 0; i < vars.size(); i++) {
            for (int j = 0; j < terminals.size(); j++) {
                parstable[i][j] = -1;
            }
        }
        for (int i = 0; i < vars.size(); i++) {
            for (int j = 0; j < grammers.length; j++) {
                if (grammers[j][0].equals(vars.get(i))) {
                    for (int k = 0; k < predicts.get(j).size(); k++) {
                        parstable[i][terminals.indexOf(predicts.get(j).get(k))] = j;
                    }
                }
            }
        }
    }

    private void symbolsfill() {
        symbols.add(new Symbol("inttemp[100]", "int", "var[]", "0"));
        symbols.add(new Symbol("floattemp[100]", "float", "var[]", "400"));
        symbols.add(new Symbol("chartemp[100]", "char", "var[]", "800"));
        adrs += 1000;
    }

    private int getProduction(Token pop, Token token) {
        return parstable[vars.indexOf(pop.value)][terminals.indexOf(token.value)];
    }

    private void grammersIntoArray(String name, String[][] grammer, ArrayList<String> terminal) throws IOException {
        FileReader inf = new FileReader(name);
        char save = (char) inf.read();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                grammer[i][j] = "";
            }
        }
        int i = 0, j = 0;
        while (save != '~') {
            if (save == '\n') {
                j = 0;
                i++;
            }
            if (save != ' ' && save != '.' && save != '\n' && save != '\r') {
                while (save != ' ') {
                    grammer[i][j] += save;
                    save = (char) inf.read();
                }
                if (!grammer[i][j].equals("#") && !grammer[i][j].equals("="))
                    if (!terminal.contains(grammer[i][j])) {
                        if (isNotUpper(grammer[i][j]))
                            terminal.add(grammer[i][j]);
                    }
                j++;
            }
            save = (char) inf.read();
        }
        terminal.add("$");
    }

    private boolean isNotUpper(String check) {
        for (int i = 0; i < check.length(); i++) {
            if (check.charAt(i) >= 65 && check.charAt(i) <= 90) {
                return false;
            }
        }
        return true;
    }

    private void printFirstFollow() {
        boolean print = true;
        for (int i = 0; i < linesofgrammer; i++) {
            for (int j = 0; j < 100; j++) {
                if (!(firsts[i][j].equals("!"))) {
                    if (j == 0) {
                        System.out.print("First " + firsts[i][j] + " : ");
                        vars.add(firsts[i][j]);
                    } else {
                        System.out.print(firsts[i][j] + " ");
                    }
                } else if (j == 0) {
                    print = false;
                    break;
                } else {
                    break;
                }
            }
            if (print)
                System.out.println();
            print = true;

        }
        System.out.println();
        System.out.println();
        System.out.println();
        print = true;
        for (int i = 0; i < linesofgrammer; i++) {
            for (int j = 0; j < 100; j++) {
                if (!(follows[i][j].equals("!"))) {
                    if (j == 0) {
                        System.out.print("Follow " + follows[i][j] + " : ");
                    } else {
                        System.out.print(follows[i][j] + " ");
                    }
                } else if (j == 0) {
                    print = false;
                    break;
                } else {
                    break;
                }
            }
            if (print)
                System.out.println();
            print = true;
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    private void printParseTable() {
        for (int i = 0; i < vars.size(); i++) {
            for (int j = 0; j < terminals.size(); j++) {
                System.out.print(parstable[i][j]);
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    private void printPredicts() {
        for (int i = 0; i < predicts.size(); i++) {
            System.out.print("predict " + (i + 1) + ": ");
            for (int j = 0; j < predicts.get(i).size(); j++) {
                System.out.print(predicts.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    private ArrayList<String> getFirst(String var) {
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < vars.size(); i++) {
            if (firsts[i][0].equals(var)) {
                for (int j = 1; j < 100; j++) {
                    if (!firsts[i][j].equals("!")) {
                        res.add(firsts[i][j]);
                    }
                }
            }
        }
        return res;
    }

    private ArrayList<String> getFollow(String var) {
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < vars.size(); i++) {
            if (follows[i][0].equals(var)) {
                for (int j = 1; j < 100; j++) {
                    if (!follows[i][j].equals("!")) {
                        res.add(follows[i][j]);
                    }
                }
            }
        }
        return res;
    }

    private boolean symbolIn(String id) {
        for (int i = 0; i < symbols.size(); i++) {
            if (symbols.get(i).name.equals(id) || symbols.get(i).name.contains(id + "[")) {
                return true;
            }
        }
        return false;
    }

    private boolean sybmolInFunc(String id, int in) {
        for (int i = 1; i < funcymbols.get(in).size(); i++) {
            Symbol temp = (Symbol) funcymbols.get(in).get(i);
            if (temp.name.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private int symbolWhere(String id) {
        if (symbolIn(id)) {
            for (int i = 0; i < symbols.size(); i++) {
                if (symbols.get(i).name.equals(id) || symbols.get(i).name.contains(id + "[")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int symbolWhereFunc(String id) {
        if (sybmolInFunc(id, numfunc)) {
            for (int i = 1; i < funcymbols.get(numfunc).size(); i++) {
                Symbol temp = (Symbol) funcymbols.get(numfunc).get(i);
                if (temp.name.equals(id)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void predicts() {
        for (int i = 0; i < linesofgrammer; i++) {
            ArrayList<String> temp = new ArrayList<>();
            String var = grammers[i][0];
            ArrayList<String> right = new ArrayList<>();
            for (int j = 2; j < 100; j++) {
                if (!grammers[i][j].equals("") && !grammers[i][j].startsWith("@")) {
                    right.add(grammers[i][j]);
                }
            }
            boolean isis = false;
            boolean is = false;
            if (isNotUpper(right.get(0))) {
                if (right.get(0).equals("#")) {
                    isis = true;
                } else {
                    temp.add(right.get(0));
                }
            } else {
                for (int j = 0; j < right.size(); j++) {
                    for (int k = 0; k < getFirst(right.get(j)).size(); k++) {
                        if (isNotUpper(right.get(j))) {
                            temp.add(right.get(j));
                            break;
                        } else {
                            if (getFirst(right.get(j)).get(k).equals("#")) {
                                is = true;
                            } else {
                                temp.add(getFirst(right.get(j)).get(k));
                            }
                        }
                    }
                    if (!is) {
                        break;
                    }
                }
            }
            if (is || isis) {
                for (int j = 0; j < getFollow(var).size(); j++) {
                    temp.add(getFollow(var).get(j));
                }
            }
            predicts.add(temp);
        }
    }

    private void fillRhst(String[][] grammer, ArrayList<ArrayList<String>> rhsts) {
        for (int i = 0; i < grammer.length; i++) {
            ArrayList<String> temp = new ArrayList();
            for (int j = 99; j >= 2; j--) {
                if (!grammer[i][j].equals("")) {
                    temp.add(grammer[i][j]);
                }
            }
            if (temp.size() > 0)
                rhsts.add(temp);
        }
    }

    private void printRhst(ArrayList<ArrayList<String>> rhsts) {
        for (int i = 0; i < rhsts.size(); i++) {
            for (int j = 0; j < rhsts.get(i).size(); j++) {
                System.out.print(rhsts.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    private void bottomup(Token token, FileReader inf) throws Exception {
        beparsstack.add(1);
        boolean goes = true;
        while (goes) {
            int x = -1;
            if (token.value.equals("`")) {
                for (int i = 0; i < varr; i++) {
                    if (bestates[0][i].equals(token.value)) {
                        x = i;
                        break;
                    }
                }
            } else if (token.type == TokenType.identifier) {
                for (int i = 0; i < varr; i++) {
                    if (bestates[0][i].equals("id")) {
                        x = i;
                        break;
                    }
                }
            } else if (token.type == TokenType.specialtoken) {
                for (int i = 0; i < varr; i++) {
                    if (bestates[0][i].equals(token.value)) {
                        x = i;
                        break;
                    }
                }
            } else if (token.type == TokenType.integernum || token.type == TokenType.realnum) {
                for (int i = 0; i < varr; i++) {
                    if (bestates[0][i].equals("num")) {
                        x = i;
                        break;
                    }
                }
            }
            if (x == -1) {
                throw new Exception();
            }
            String what = bestates[beparsstack.peek()][x];
            if (what == null) {
                throw new Exception();
            } else {
                if (what.equals("accept")) {
                    goes = false;
                } else if (what.charAt(0) == 'S') {
                    int num = Integer.parseInt(what.substring(1, what.length()));
                    beparsstack.push(num);
                    token = reader(inf);
                } else if (what.charAt(0) == 'R') {
                    int num = Integer.parseInt(what.substring(1, what.length()));
                    for (int i = 0; i < Integer.parseInt(besides[num - 1][1]); i++) {
                        beparsstack.pop();
                    }
                    String find = besides[num - 1][0];
                    int y = -1;
                    for (int i = 0; i < varr; i++) {
                        if (bestates[0][i].equals(find)) {
                            y = i;
                            break;
                        }
                    }
                    if (y == -1) {
                        throw new Exception();
                    }
                    beparsstack.push(Integer.valueOf(bestates[beparsstack.peek()][y].substring(1)));
                }
            }
        }
    }

    private int findfunc(String name) {
        for (int i = 0; i < funcymbols.size(); i++) {
            if (funcymbols.get(i).contains(name))
                return i;
        }
        return -1;
    }

    private void generator(String peek, Token token, FileReader inf) throws Exception {
        if (peek.equals("@savepc")) {
            ss.push(pc);
        } else if (peek.equals("@testid")) {
            if (infunc) {
                if (sybmolInFunc(String.valueOf(ss.peek()), numfunc)) {
                } else throw new Exception();
            } else {
                if (symbolIn(String.valueOf(ss.peek()))) {
                } else throw new Exception();
            }
        } else if (peek.equals("@jmpout")) {
            code[pc][0] = "jmp";
            ssforline.add(pc);
            pc++;
        } else if (peek.equals("@startswitch")) {
            code[pc][0] = "jmp";
            ssfornum.push(pc);
            switchon = String.valueOf(ss.pop());
            pc++;
        } else if (peek.equals("@def")) {
            ssfornum.add(pc);
        } else if (peek.equals("@endswitch")) {
            code[pc][0] = "jmp";
            ss.push(pc);
        } else if (peek.equals("@last")) {
            String save = ssfornum.pop().toString();
            int size = ssforpc.size();
            int lastline = (int) ss.pop();
            pc++;
            int keep = pc;
            String id = switchon;
            if (infunc) {
                if (sybmolInFunc(id, numfunc)) {
                } else {
                    throw new Exception();
                }

            } else {
                if (symbolIn(id)) {

                } else {
                    throw new Exception();
                }
            }
            for (int i = 0; i < size; i++) {
                code[pc][0] = "je";
                code[pc][1] = id;
                code[pc][2] = ssfornum.pop().toString();
                code[pc][3] = ssforpc.pop().toString();
                pc++;
            }
            size = ssforline.size();
            code[lastline][1] = String.valueOf(pc + 1);
            for (int i = 0; i < size; i++) {
                code[(int) ssforline.pop()][1] = String.valueOf(pc + 1);
            }
            code[pc][0] = "jmp";
            code[pc][1] = save;
            pc++;
            code[(int) ssfornum.pop()][1] = String.valueOf(keep);
        } else if (peek.equals("@saved")) {
            ssforpc.push(pc);
        } else if (peek.equals("@savenum")) {
            ssfornum.add(ss.pop());
        } else if (peek.equals("@outfunc")) {
            ss.pop();
            infunc = false;
            numfunc = -1;
        } else if (peek.equals("@out")) {
            ss.pop();
        } else if (peek.equals("@has")) {
            hasprototype[funcymbols.size() - 1] = true;
        } else if (peek.equals("@outt")) {
            infunc = true;
            boolean go = true;
            for (int i = 0; i < funcymbols.size(); i++) {
                if (funcymbols.get(i).contains(ss.peek())) {
                    Object keep = ss.pop();
                    ss.pop();
                    ss.push(keep);
                    numfunc = findfunc(keep.toString());
                    go = false;
                    break;
                }
            }
            if (go) {
                Object keep = ss.pop();
                if (!keep.equals("main")) {
                    ArrayList temp = new ArrayList();
                    temp.add(keep);
                    funcymbols.add(temp);
                    numfunc = findfunc(keep.toString());
                }
                ss.pop();
                ss.push(keep);
            }
        } else if (peek.equals("@poptype")) {
            ss.pop();
        } else if (peek.equals("@jz")) {
            code[pc][0] = "jz";
            ss.pop();
            code[pc][1] = "tb";
            ss.push(pc);
            pc++;
        } else if (peek.equals("@jnz")) {
            code[pc][0] = "jnz";
            ss.pop();
            code[pc][1] = "tb";
            code[pc][2] = ss.pop().toString();
            pc++;
        } else if (peek.equals("@jmpcompjmp")) {
            code[(int) ss.pop()][2] = String.valueOf(pc + 1);
            code[pc][0] = "jmp";
            code[pc][1] = ss.pop().toString();
            pc++;
        } else if (peek.equals("@movs")) {
            if (infunc) {
                boolean take = true;
                code[pc][0] = "mov";
                code[pc][1] = ss.pop().toString();
                if (!sybmolInFunc(ss.peek().toString(), numfunc)) {
                    String num = String.valueOf(ss.pop());
                    if (sybmolInFunc(ss.peek().toString(), numfunc)) {
                        take = false;
                        Symbol temp = (Symbol) funcymbols.get(numfunc).get(symbolWhereFunc(ss.peek().toString()));
                        temp.value = code[pc][1];
                        code[pc][2] = ss.pop().toString() + "[" + num + "]";
                        pc++;
                    } else
                        throw new Exception();
                }
                if (take) {
                    Symbol temp = (Symbol) funcymbols.get(numfunc).get(symbolWhereFunc(ss.peek().toString()));
                    temp.value = code[pc][1];
                    code[pc][2] = ss.pop().toString();
                    pc++;
                }
            } else {
                boolean take = true;
                code[pc][0] = "mov";
                code[pc][1] = ss.pop().toString();
                if (!symbolIn(ss.peek().toString())) {
                    String num = String.valueOf(ss.pop());
                    if (symbolIn(ss.peek().toString())) {
                        take = false;
                        symbols.get(symbolWhere(ss.peek().toString())).value = code[pc][1];
                        code[pc][2] = ss.pop().toString() + "[" + num + "]";
                        pc++;
                    } else {
                        throw new Exception();
                    }
                }
                if (take) {
                    symbols.get(symbolWhere(ss.peek().toString())).value = code[pc][1];
                    code[pc][2] = ss.pop().toString();
                    pc++;
                }
            }
        } else if (peek.equals("@move")) {
            if (infunc) {
                code[pc][0] = "mov";
                code[pc][1] = ss.pop().toString();
                Symbol temp = (Symbol) funcymbols.get(numfunc).get(funcymbols.get(numfunc).size() - 1);
                code[pc][2] = temp.name;
                temp.value = code[pc][1];
                pc++;
            } else {
                code[pc][0] = "mov";
                code[pc][1] = ss.pop().toString();
                code[pc][2] = symbols.get(symbols.size() - 1).name;
                symbols.get(symbols.size() - 1).value = code[pc][1];
                pc++;
            }
        } else if (peek.equals("@mov")) {
            code[pc][0] = "mov";
            code[pc][1] = ss.pop().toString();
            code[pc][2] = ss.peek().toString();
            pc++;
        } else if (peek.equals("@getin")) {
            String type = String.valueOf(ss.pop());
            String name = String.valueOf(token.value);
            if (hasprototype[numfunc]) {
            } else if (sybmolInFunc(name, numfunc)) {
                throw new Exception();
            } else {
                int meghdar = 0;
                if (type.equals("int") || type.equals("float"))
                    meghdar += 4;
                else if (type.equals("char"))
                    meghdar += 2;
                funcymbols.get(numfunc).add(new Symbol(name, type, "var", String.valueOf(adrs)));
                adrs += meghdar;
            }
        } else if (peek.equals("@ads")) {
            if (infunc) {
                if (sybmolInFunc(ss.peek().toString(), numfunc)) {
                    throw new Exception();
                } else {
                    String name = String.valueOf(ss.pop());
                    String type = String.valueOf(ss.peek());
                    int meghdar = 0;
                    if (type.equals("int") || type.equals("float"))
                        meghdar += 4;
                    else if (type.equals("char"))
                        meghdar += 2;
                    funcymbols.get(numfunc).add(new Symbol(name, type, "var", String.valueOf(adrs)));
                    adrs += meghdar;
                }
            } else {
                if (symbolIn(ss.peek().toString())) {
                    throw new Exception();
                } else {
                    String name = String.valueOf(ss.pop());
                    String type = String.valueOf(ss.peek());
                    int meghdar = 0;
                    if (type.equals("int") || type.equals("float"))
                        meghdar += 4;
                    else if (type.equals("char"))
                        meghdar += 2;
                    symbols.add(new Symbol(name, type, "var", String.valueOf(adrs)));
                    adrs += meghdar;
                }
            }
        } else if (peek.equals("@ad")) {
            String number = String.valueOf(ss.pop());
            int num = Integer.parseInt(number);
            String type = String.valueOf(ss.peek());
            int meghdar = 0;
            if (type.equals("int") || type.equals("float")) {
                meghdar += 4 * num;
                adrs -= 4;
            } else if (type.equals("char")) {
                meghdar += 2 * num;
                adrs -= 2;
            }
            symbols.add(new Symbol(symbols.get(symbols.size() - 1).name + "[" + number + "]", type, "var[]", String.valueOf(adrs)));
            adrs += meghdar;
            symbols.remove(symbols.size() - 2);
        } else if (peek.equals("@for")) {
            code[pc][0] = "sub";
            code[pc][1] = ss.pop().toString();
            code[pc][2] = ss.peek().toString();
            String temp = "t" + getTemp();
            code[pc][3] = temp;
            ss.push(pc);
            pc++;
            code[pc][0] = "jl";
            code[pc][1] = temp;
            code[pc][2] = "0";
            pc++;
        } else if (peek.equals("@compfor")) {
            String line = ss.pop().toString();
            code[pc][0] = "inc";
            code[pc][1] = ss.pop().toString();
            pc++;
            code[pc][0] = "jmp";
            code[pc][1] = line;
            pc++;
            code[Integer.parseInt(line) + 1][3] = String.valueOf(pc);
        } else if (peek.equals("@finaljmp")) {
            code[pc][0] = "jmp";
            ss.push(pc);
            pc++;
        } else if (peek.equals("@finalize")) {
            String line = String.valueOf(ss.pop());
            int lineint = Integer.parseInt(line);
            code[lineint][1] = String.valueOf(pc);
        } else if (peek.equals("@compjmp")) {
            code[(int) ss.pop()][2] = String.valueOf(pc);
        } else if (peek.equals("@compjmplast")) {
            String save = String.valueOf(ss.pop());
            code[(int) ss.pop()][2] = String.valueOf(pc);
            ss.push(save);
        } else if (peek.equals("@bottom")) {
            bottomup(token, inf);
        }
    }

    private void topdown() throws Exception {
        parsstack.push(new Token(TokenType.end, "$"));
        parsstack.push(new Token(TokenType.variable, "STL"));
        FileReader inf = new FileReader("code.txt");
        ch = (char) inf.read();
        chars++;
        Token token = reader(inf);
        boolean go = true;
        do {
            switch (parsstack.peek().type) {
                case variable:
                    int product;
                    if (token.type == TokenType.identifier) {
                        product = getProduction(parsstack.peek(), new Token(TokenType.identifier, "id"));
                    } else if (token.type == TokenType.identifier) {
                        product = getProduction(parsstack.peek(), new Token(TokenType.booleanex, "be"));
                    } else if (token.type == TokenType.integernum) {
                        product = getProduction(parsstack.peek(), new Token(TokenType.integernum, "num"));
                    } else if (token.type == TokenType.include) {
                        product = getProduction(parsstack.peek(), new Token(TokenType.include, "include"));
                    } else if (token.type == TokenType.realnum) {
                        product = getProduction(parsstack.peek(), new Token(TokenType.realnum, "real"));
                    } else {
                        product = getProduction(parsstack.peek(), token);
                    }
                    if (product == -1) {
                        System.out.println(token);
                        System.out.println(parsstack.peek());
                        throw new Exception();
                    }
                    parsstack.pop();
                    for (int i = 0; i < rhst.get(product).size(); i++) {
                        String got = rhst.get(product).get(i);
                        if (got.startsWith("@")) {
                            parsstack.push(new Token(TokenType.semantic, got));
                        } else if (got.equals("be")) {
                            parsstack.push(new Token(TokenType.booleanex, got));
                        } else if (got.equals("id")) {
                            parsstack.push(new Token(TokenType.identifier, got));
                        } else if (got.equals("num")) {
                            parsstack.push(new Token(TokenType.integernum, got));
                        } else if (got.equals("real")) {
                            parsstack.push(new Token(TokenType.realnum, got));
                        } else if (got.equals("include")) {
                            parsstack.push(new Token(TokenType.include, got));
                        } else if (got.equals("#")) {
                        } else if (isNotUpper(got)) {
                            parsstack.push(new Token(TokenType.specialtoken, got));
                        } else {
                            parsstack.push(new Token(TokenType.variable, got));
                        }
                    }
                    break;
                case semantic:
                    String peek = parsstack.peek().value;
                    generator(peek, token, inf);
                    parsstack.pop();
                    break;
                case booleanex:
                case identifier:
                case integernum:
                case realnum:
                case include:
                    if (token.type == TokenType.identifier || token.type == TokenType.integernum || token.type == TokenType.include || token.type == TokenType.realnum) {
                        parsstack.pop();
                        ss.push(token.value);
                        token = reader(inf);
                    } else {
                        System.out.println(token);
                        throw new Exception();
                    }
                    break;
                case keyword:
                case specialtoken:
                    if (token.value.equals(parsstack.peek().value)) {
                        parsstack.pop();
                        if (token.value.equals("int") || token.value.equals("float") || token.value.equals("char"))
                            ss.push(token.value);
                        token = reader(inf);
                    } else {
                        System.out.println(token);
                        System.out.println(parsstack.peek().value);
                        throw new Exception();
                    }
                    break;
                case end:
                    if (token.value.equals("$")) {
                        go = false;
                        break;
                    } else {
                        throw new Exception();
                    }
            }
        } while (go);
    }

    private void calculate() {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                firsts[i][j] = "!";
                follows[i][j] = "!";
            }
        }

        firsts[0][0] = "STL";
        firsts[0][1] = "for";
        firsts[0][2] = "while";
        firsts[0][3] = "repeat";
        firsts[0][4] = "if";
        firsts[0][5] = "include";
        firsts[0][6] = "int";
        firsts[0][7] = "float";
        firsts[0][8] = "char";
        firsts[0][9] = "id";
        firsts[0][10] = "#";
        firsts[0][11] = "switch";
        firsts[1][0] = "ST";
        firsts[1][1] = "for";
        firsts[1][2] = "while";
        firsts[1][3] = "repeat";
        firsts[1][4] = "if";
        firsts[1][5] = "include";
        firsts[1][6] = "int";
        firsts[1][7] = "float";
        firsts[1][8] = "char";
        firsts[1][9] = "id";
        firsts[1][10] = "switch";
        firsts[2][0] = "FST";
        firsts[2][1] = "for";
        firsts[3][0] = "WST";
        firsts[3][1] = "while";
        firsts[4][0] = "FORR";
        firsts[4][1] = "{";
        firsts[4][2] = "for";
        firsts[4][3] = "while";
        firsts[4][4] = "repeat";
        firsts[4][5] = "if";
        firsts[4][6] = "include";
        firsts[4][7] = "int";
        firsts[4][8] = "float";
        firsts[4][9] = "char";
        firsts[4][10] = "id";
        firsts[4][11] = "switch";
        firsts[5][0] = "RUST";
        firsts[5][1] = "repeat";
        firsts[6][0] = "IFST";
        firsts[6][1] = "if";
        firsts[7][0] = "IFF";
        firsts[7][1] = "else";
        firsts[7][2] = "#";
        firsts[8][0] = "INCLUDEST";
        firsts[8][1] = "include";
        firsts[9][0] = "IFFF";
        firsts[9][1] = "do";
        firsts[9][2] = "if";
        firsts[10][0] = "WSTT";
        firsts[10][1] = "{";
        firsts[10][2] = "for";
        firsts[10][3] = "while";
        firsts[10][4] = "repeat";
        firsts[10][5] = "if";
        firsts[10][6] = "include";
        firsts[10][7] = "int";
        firsts[10][8] = "float";
        firsts[10][9] = "char";
        firsts[10][10] = "id";
        firsts[10][11] = "switch";
        firsts[11][0] = "RUSTT";
        firsts[11][1] = "{";
        firsts[11][2] = "for";
        firsts[11][3] = "while";
        firsts[11][4] = "repeat";
        firsts[11][5] = "if";
        firsts[11][6] = "include";
        firsts[11][7] = "int";
        firsts[11][8] = "float";
        firsts[11][9] = "char";
        firsts[11][10] = "id";
        firsts[11][11] = "switch";
        firsts[12][0] = "DCLST";
        firsts[12][1] = "int";
        firsts[12][2] = "float";
        firsts[12][3] = "char";
        firsts[12][9] = "id";
        firsts[13][0] = "DCLSTT";
        firsts[13][1] = ",";
        firsts[13][2] = ";";
        firsts[14][0] = "CONT";
        firsts[14][1] = ":=";
        firsts[14][2] = "[";
        firsts[14][3] = "#";
        firsts[15][0] = "T";
        firsts[15][1] = "int";
        firsts[15][2] = "float";
        firsts[15][3] = "char";
        firsts[16][0] = "CHOOSE";
        firsts[16][1] = "#";
        firsts[16][2] = "(";
        firsts[17][0] = "XX";
        firsts[17][1] = "#";
        firsts[17][2] = "int";
        firsts[17][3] = "float";
        firsts[17][4] = "char";
        firsts[18][0] = "XXX";
        firsts[18][1] = "#";
        firsts[18][2] = ",";
        firsts[19][0] = "IS";
        firsts[19][1] = "#";
        firsts[19][2] = "*";
        firsts[20][0] = "CHOSE";
        firsts[20][1] = "{";
        firsts[20][2] = "#";
        firsts[20][3] = ";";
        firsts[21][0] = "ASSGN";
        firsts[21][1] = "id";
        firsts[22][0] = "ASSGNN";
        firsts[22][1] = "#";
        firsts[22][2] = "[";
        firsts[23][0] = "SWITCH";
        firsts[23][1] = "switch";
        firsts[24][0] = "CASE";
        firsts[24][1] = "#";
        firsts[24][2] = "case";

        follows[0][0] = "STL";
        follows[0][1] = "$";
        follows[0][2] = "}";
        follows[0][3] = "return";
        follows[0][4] = "break";
        follows[1][0] = "ST";
        follows[1][1] = "for";
        follows[1][2] = "while";
        follows[1][3] = "repeat";
        follows[1][4] = "if";
        follows[1][5] = "include";
        follows[1][6] = "int";
        follows[1][7] = "float";
        follows[1][8] = "char";
        follows[1][9] = "id";
        follows[1][10] = "until";
        follows[1][11] = "return";
        follows[1][12] = "$";
        follows[1][13] = "}";
        follows[1][14] = "switch";
        follows[2][0] = "FST";
        follows[2][1] = "for";
        follows[2][2] = "while";
        follows[2][3] = "repeat";
        follows[2][4] = "if";
        follows[2][5] = "include";
        follows[2][6] = "int";
        follows[2][7] = "float";
        follows[2][8] = "char";
        follows[2][9] = "id";
        follows[2][10] = "until";
        follows[2][11] = "return";
        follows[2][12] = "$";
        follows[2][13] = "}";
        follows[2][14] = "switch";
        follows[3][0] = "WST";
        follows[3][1] = "for";
        follows[3][2] = "while";
        follows[3][3] = "repeat";
        follows[3][4] = "if";
        follows[3][5] = "include";
        follows[3][6] = "int";
        follows[3][7] = "float";
        follows[3][8] = "char";
        follows[3][9] = "id";
        follows[3][10] = "until";
        follows[3][11] = "return";
        follows[3][12] = "$";
        follows[3][13] = "}";
        follows[3][14] = "switch";
        follows[4][0] = "FORR";
        follows[4][1] = "for";
        follows[4][2] = "while";
        follows[4][3] = "repeat";
        follows[4][4] = "if";
        follows[4][5] = "include";
        follows[4][6] = "int";
        follows[4][7] = "float";
        follows[4][8] = "char";
        follows[4][9] = "id";
        follows[4][10] = "until";
        follows[4][11] = "return";
        follows[4][12] = "$";
        follows[4][13] = "}";
        follows[4][14] = "switch";
        follows[5][0] = "RUST";
        follows[5][1] = "for";
        follows[5][2] = "while";
        follows[5][3] = "repeat";
        follows[5][4] = "if";
        follows[5][5] = "include";
        follows[5][6] = "int";
        follows[5][7] = "float";
        follows[5][8] = "char";
        follows[5][9] = "id";
        follows[5][10] = "until";
        follows[5][11] = "return";
        follows[5][12] = "$";
        follows[5][13] = "}";
        follows[5][14] = "switch";
        follows[6][0] = "IFST";
        follows[6][1] = "for";
        follows[6][2] = "while";
        follows[6][3] = "repeat";
        follows[6][4] = "if";
        follows[6][5] = "include";
        follows[6][6] = "int";
        follows[6][7] = "float";
        follows[6][8] = "char";
        follows[6][9] = "id";
        follows[6][10] = "until";
        follows[6][11] = "return";
        follows[6][12] = "$";
        follows[6][13] = "}";
        follows[6][14] = "switch";
        follows[7][0] = "IFF";
        follows[7][1] = "for";
        follows[7][2] = "while";
        follows[7][3] = "repeat";
        follows[7][4] = "if";
        follows[7][5] = "include";
        follows[7][6] = "int";
        follows[7][7] = "float";
        follows[7][8] = "char";
        follows[7][9] = "id";
        follows[7][10] = "until";
        follows[7][11] = "return";
        follows[7][12] = "}";
        follows[7][13] = "$";
        follows[7][14] = "switch";
        follows[8][0] = "INCLUDEST";
        follows[8][1] = "for";
        follows[8][2] = "while";
        follows[8][3] = "repeat";
        follows[8][4] = "if";
        follows[8][5] = "include";
        follows[8][6] = "int";
        follows[8][7] = "float";
        follows[8][8] = "char";
        follows[8][9] = "id";
        follows[8][10] = "until";
        follows[8][11] = "return";
        follows[8][12] = "$";
        follows[8][13] = "}";
        follows[8][14] = "switch";
        follows[9][0] = "IFFF";
        follows[9][1] = "for";
        follows[9][2] = "while";
        follows[9][3] = "repeat";
        follows[9][4] = "if";
        follows[9][5] = "include";
        follows[9][6] = "int";
        follows[9][7] = "float";
        follows[9][8] = "char";
        follows[9][9] = "id";
        follows[9][10] = "until";
        follows[9][11] = "return";
        follows[9][12] = "$";
        follows[9][13] = "}";
        follows[9][14] = "switch";
        follows[10][0] = "WSTT";
        follows[10][1] = "for";
        follows[10][2] = "while";
        follows[10][3] = "repeat";
        follows[10][4] = "if";
        follows[10][5] = "include";
        follows[10][6] = "int";
        follows[10][7] = "float";
        follows[10][8] = "char";
        follows[10][9] = "id";
        follows[10][10] = "until";
        follows[10][11] = "return";
        follows[10][12] = "$";
        follows[10][13] = "}";
        follows[10][14] = "switch";
        follows[11][0] = "RUSTT";
        follows[11][1] = "until";
        follows[12][0] = "DCLST";
        follows[12][1] = "for";
        follows[12][2] = "while";
        follows[12][3] = "repeat";
        follows[12][4] = "if";
        follows[12][5] = "include";
        follows[12][6] = "int";
        follows[12][7] = "float";
        follows[12][8] = "char";
        follows[12][9] = "id";
        follows[12][10] = "until";
        follows[12][11] = "return";
        follows[12][12] = "$";
        follows[12][13] = "}";
        follows[12][14] = "switch";
        follows[13][0] = "DCLSTT";
        follows[13][1] = "for";
        follows[13][2] = "while";
        follows[13][3] = "repeat";
        follows[13][4] = "if";
        follows[13][5] = "include";
        follows[13][6] = "int";
        follows[13][7] = "float";
        follows[13][8] = "char";
        follows[13][9] = "id";
        follows[13][10] = "until";
        follows[13][11] = "return";
        follows[13][12] = "$";
        follows[13][13] = "}";
        follows[13][14] = "switch";
        follows[14][0] = "CONT";
        follows[14][1] = ",";
        follows[14][2] = ";";
        follows[15][0] = "T";
        follows[15][1] = "id";
        follows[15][2] = "*";
        follows[16][0] = "CHOOSE";
        follows[16][1] = ",";
        follows[16][2] = ";";
        follows[17][0] = "XX";
        follows[17][1] = ")";
        follows[18][0] = "XXX";
        follows[18][1] = ")";
        follows[19][0] = "IS";
        follows[19][1] = "id";
        follows[20][0] = "CHOSE";
        follows[20][1] = ",";
        follows[20][2] = ";";
        follows[21][0] = "ASSGN";
        follows[21][1] = "for";
        follows[21][2] = "while";
        follows[21][3] = "repeat";
        follows[21][4] = "if";
        follows[21][5] = "include";
        follows[21][6] = "int";
        follows[21][7] = "float";
        follows[21][8] = "char";
        follows[21][9] = "id";
        follows[21][10] = "until";
        follows[21][11] = "return";
        follows[21][12] = "$";
        follows[21][13] = "}";
        follows[21][14] = "switch";
        follows[22][0] = "ASSGNN";
        follows[22][1] = ":=";
        follows[23][0] = "SWITCH";
        follows[23][1] = "for";
        follows[23][2] = "while";
        follows[23][3] = "repeat";
        follows[23][4] = "if";
        follows[23][5] = "include";
        follows[23][6] = "int";
        follows[23][7] = "float";
        follows[23][8] = "char";
        follows[23][9] = "id";
        follows[23][10] = "until";
        follows[23][11] = "return";
        follows[23][12] = "$";
        follows[23][13] = "}";
        follows[23][14] = "switch";
        follows[24][0] = "CASE";
        follows[24][1] = "default";
    }

    private void fillEmpty() {
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 4; j++) {
                code[i][j] = "!";
            }
        }
    }

    private void printCodes() {
        boolean yes = true;
        int k = 1;
        for (int i = 0; i < 1000; i++) {
            if (yes) {
                System.out.print(k + ":  ");
            }
            yes = false;
            for (int j = 0; j < 4; j++) {
                if (!code[i][j].equals("!")) {
                    System.out.print(code[i][j] + " ");
                    yes = true;
                }
            }
            if (yes) {
                System.out.println();
                k++;
            }
        }
    }

    private void becalculate() {
        bestates[0][0] = "||";
        bestates[0][1] = "&&";
        bestates[0][2] = "id";
        bestates[0][3] = "num";
        bestates[0][4] = "(";
        bestates[0][5] = ")";
        bestates[0][6] = "[";
        bestates[0][7] = "]";
        bestates[0][8] = "<";
        bestates[0][9] = ">";
        bestates[0][10] = "=";
        bestates[0][11] = "!";
        bestates[0][12] = ":=";
        bestates[0][13] = "`";
        bestates[0][14] = "BE";
        bestates[0][15] = "BEE";
        bestates[0][16] = "BT";
        bestates[0][17] = "BTT";
        bestates[0][18] = "BF";
        bestates[0][19] = "AST";

        bestates[1][0] = null;
        bestates[1][1] = null;
        bestates[1][2] = "S5";
        bestates[1][3] = "S6";
        bestates[1][4] = "S8";
        bestates[1][5] = null;
        bestates[1][6] = null;
        bestates[1][7] = null;
        bestates[1][8] = null;
        bestates[1][9] = null;
        bestates[1][10] = null;
        bestates[1][11] = null;
        bestates[1][12] = null;
        bestates[1][13] = null;
        bestates[1][14] = "G2";
        bestates[1][15] = null;
        bestates[1][16] = "G3";
        bestates[1][17] = null;
        bestates[1][18] = "G4";
        bestates[1][19] = "G7";

        bestates[2][0] = null;
        bestates[2][1] = null;
        bestates[2][2] = null;
        bestates[2][3] = null;
        bestates[2][4] = null;
        bestates[2][5] = null;
        bestates[2][6] = null;
        bestates[2][7] = null;
        bestates[2][8] = null;
        bestates[2][9] = null;
        bestates[2][10] = null;
        bestates[2][11] = null;
        bestates[2][12] = null;
        bestates[2][13] = "accept";
        bestates[2][14] = null;
        bestates[2][15] = null;
        bestates[2][16] = null;
        bestates[2][17] = null;
        bestates[2][18] = null;
        bestates[2][19] = null;

        bestates[3][0] = "S10";
        bestates[3][1] = null;
        bestates[3][2] = null;
        bestates[3][3] = null;
        bestates[3][4] = null;
        bestates[3][5] = "R3";
        bestates[3][6] = null;
        bestates[3][7] = "R3";
        bestates[3][8] = "R3";
        bestates[3][9] = "R3";
        bestates[3][10] = "R3";
        bestates[3][11] = "R3";
        bestates[3][12] = null;
        bestates[3][13] = "R3";
        bestates[3][14] = null;
        bestates[3][15] = "G9";
        bestates[3][16] = null;
        bestates[3][17] = null;
        bestates[3][18] = null;
        bestates[3][19] = null;

        bestates[4][0] = "R6";
        bestates[4][1] = "S12";
        bestates[4][2] = null;
        bestates[4][3] = null;
        bestates[4][4] = null;
        bestates[4][5] = "R6";
        bestates[4][6] = null;
        bestates[4][7] = "R6";
        bestates[4][8] = "R6";
        bestates[4][9] = "R6";
        bestates[4][10] = "R6";
        bestates[4][11] = "R6";
        bestates[4][12] = null;
        bestates[4][13] = "R6";
        bestates[4][14] = null;
        bestates[4][15] = null;
        bestates[4][16] = null;
        bestates[4][17] = "G11";
        bestates[4][18] = null;
        bestates[4][19] = null;

        bestates[5][0] = "R7";
        bestates[5][1] = "R7";
        bestates[5][2] = null;
        bestates[5][3] = null;
        bestates[5][4] = null;
        bestates[5][5] = "R7";
        bestates[5][6] = "S13";
        bestates[5][7] = "R7";
        bestates[5][8] = "R7";
        bestates[5][9] = "R7";
        bestates[5][10] = "R7";
        bestates[5][11] = "R7";
        bestates[5][12] = "S14";
        bestates[5][13] = "R7";
        bestates[5][14] = null;
        bestates[5][15] = null;
        bestates[5][16] = null;
        bestates[5][17] = null;
        bestates[5][18] = null;
        bestates[5][19] = null;


        bestates[6][0] = "R8";
        bestates[6][1] = "R8";
        bestates[6][2] = null;
        bestates[6][3] = null;
        bestates[6][4] = null;
        bestates[6][5] = "R8";
        bestates[6][6] = null;
        bestates[6][7] = "R8";
        bestates[6][8] = "R8";
        bestates[6][9] = "R8";
        bestates[6][10] = "R8";
        bestates[6][11] = "R8";
        bestates[6][12] = null;
        bestates[6][13] = "R8";
        bestates[6][14] = null;
        bestates[6][15] = null;
        bestates[6][16] = null;
        bestates[6][17] = null;
        bestates[6][18] = null;
        bestates[6][19] = null;

        bestates[7][0] = "R11";
        bestates[7][1] = "R11";
        bestates[7][2] = null;
        bestates[7][3] = null;
        bestates[7][4] = null;
        bestates[7][5] = "R11";
        bestates[7][6] = null;
        bestates[7][7] = "R11";
        bestates[7][8] = "R11";
        bestates[7][9] = "R11";
        bestates[7][10] = "R11";
        bestates[7][11] = "R11";
        bestates[7][12] = null;
        bestates[7][13] = "R11";
        bestates[7][14] = null;
        bestates[7][15] = null;
        bestates[7][16] = null;
        bestates[7][17] = null;
        bestates[7][18] = null;
        bestates[7][19] = null;

        bestates[8][0] = null;
        bestates[8][1] = null;
        bestates[8][2] = "S5";
        bestates[8][3] = "S6";
        bestates[8][4] = "S8";
        bestates[8][5] = null;
        bestates[8][6] = null;
        bestates[8][7] = null;
        bestates[8][8] = null;
        bestates[8][9] = null;
        bestates[8][10] = null;
        bestates[8][11] = null;
        bestates[8][12] = null;
        bestates[8][13] = null;
        bestates[8][14] = "G15";
        bestates[8][15] = null;
        bestates[8][16] = "G3";
        bestates[8][17] = null;
        bestates[8][18] = "G4";
        bestates[8][19] = "G7";

        bestates[9][0] = "R1";
        bestates[9][1] = "R1";
        bestates[9][2] = null;
        bestates[9][3] = null;
        bestates[9][4] = null;
        bestates[9][5] = "R1";
        bestates[9][6] = null;
        bestates[9][7] = "R1";
        bestates[9][8] = "R1";
        bestates[9][9] = "R1";
        bestates[9][10] = "R1";
        bestates[9][11] = "R1";
        bestates[9][12] = null;
        bestates[9][13] = "R1";
        bestates[9][14] = null;
        bestates[9][15] = null;
        bestates[9][16] = null;
        bestates[9][17] = null;
        bestates[9][18] = null;
        bestates[9][19] = null;

        bestates[10][0] = null;
        bestates[10][1] = null;
        bestates[10][2] = "S5";
        bestates[10][3] = "S6";
        bestates[10][4] = "S8";
        bestates[10][5] = null;
        bestates[10][6] = null;
        bestates[10][7] = null;
        bestates[10][8] = null;
        bestates[10][9] = null;
        bestates[10][10] = null;
        bestates[10][11] = null;
        bestates[10][12] = null;
        bestates[10][13] = null;
        bestates[10][14] = null;
        bestates[10][15] = null;
        bestates[10][16] = "G16";
        bestates[10][17] = null;
        bestates[10][18] = "G4";
        bestates[10][19] = "G7";

        bestates[11][0] = "R4";
        bestates[11][1] = "R4";
        bestates[11][2] = null;
        bestates[11][3] = null;
        bestates[11][4] = null;
        bestates[11][5] = "R4";
        bestates[11][6] = null;
        bestates[11][7] = "R4";
        bestates[11][8] = "R4";
        bestates[11][9] = "R4";
        bestates[11][10] = "R4";
        bestates[11][11] = "R4";
        bestates[11][12] = null;
        bestates[11][13] = "R4";
        bestates[11][14] = null;
        bestates[11][15] = null;
        bestates[11][16] = null;
        bestates[11][17] = null;
        bestates[11][18] = null;
        bestates[11][19] = null;

        bestates[12][0] = null;
        bestates[12][1] = null;
        bestates[12][2] = "S5";
        bestates[12][3] = "S6";
        bestates[12][4] = "S8";
        bestates[12][5] = null;
        bestates[12][6] = null;
        bestates[12][7] = null;
        bestates[12][8] = null;
        bestates[12][9] = null;
        bestates[12][10] = null;
        bestates[12][11] = null;
        bestates[12][12] = null;
        bestates[12][13] = null;
        bestates[12][14] = null;
        bestates[12][15] = null;
        bestates[12][16] = null;
        bestates[12][17] = null;
        bestates[12][18] = "G17";
        bestates[12][19] = "G7";

        bestates[13][0] = null;
        bestates[13][1] = null;
        bestates[13][2] = "S5";
        bestates[13][3] = "S6";
        bestates[13][4] = "S8";
        bestates[13][5] = null;
        bestates[13][6] = null;
        bestates[13][7] = null;
        bestates[13][8] = null;
        bestates[13][9] = null;
        bestates[13][10] = null;
        bestates[13][11] = null;
        bestates[13][12] = null;
        bestates[13][13] = null;
        bestates[13][14] = "G18";
        bestates[13][15] = null;
        bestates[13][16] = "G3";
        bestates[13][17] = null;
        bestates[13][18] = "G4";
        bestates[13][19] = "G7";

        bestates[14][0] = null;
        bestates[14][1] = null;
        bestates[14][2] = "S5";
        bestates[14][3] = "S6";
        bestates[14][4] = "S8";
        bestates[14][5] = null;
        bestates[14][6] = null;
        bestates[14][7] = null;
        bestates[14][8] = null;
        bestates[14][9] = null;
        bestates[14][10] = null;
        bestates[14][11] = null;
        bestates[14][12] = null;
        bestates[14][13] = null;
        bestates[14][14] = "G19";
        bestates[14][15] = null;
        bestates[14][16] = "G3";
        bestates[14][17] = null;
        bestates[14][18] = "G4";
        bestates[14][19] = "G7";

        bestates[15][0] = null;
        bestates[15][1] = null;
        bestates[15][2] = null;
        bestates[15][3] = null;
        bestates[15][4] = null;
        bestates[15][5] = "S36";
        bestates[15][6] = null;
        bestates[15][7] = null;
        bestates[15][8] = null;
        bestates[15][9] = null;
        bestates[15][10] = null;
        bestates[15][11] = null;
        bestates[15][12] = null;
        bestates[15][13] = null;
        bestates[15][14] = null;
        bestates[15][15] = null;
        bestates[15][16] = null;
        bestates[15][17] = null;
        bestates[15][18] = null;
        bestates[15][19] = null;

        bestates[16][0] = "S10";
        bestates[16][1] = null;
        bestates[16][2] = null;
        bestates[16][3] = null;
        bestates[16][4] = null;
        bestates[16][5] = "R3";
        bestates[16][6] = null;
        bestates[16][7] = null;
        bestates[16][8] = "R3";
        bestates[16][9] = "R3";
        bestates[16][10] = "R3";
        bestates[16][11] = "R3";
        bestates[16][12] = null;
        bestates[16][13] = "R3";
        bestates[16][14] = null;
        bestates[16][15] = "G20";
        bestates[16][16] = null;
        bestates[16][17] = null;
        bestates[16][18] = null;
        bestates[16][19] = null;

        bestates[17][0] = "R6";
        bestates[17][1] = "S12";
        bestates[17][2] = null;
        bestates[17][3] = null;
        bestates[17][4] = null;
        bestates[17][5] = "R6";
        bestates[17][6] = null;
        bestates[17][7] = null;
        bestates[17][8] = "R6";
        bestates[17][9] = "R6";
        bestates[17][10] = "R6";
        bestates[17][11] = "R6";
        bestates[17][12] = null;
        bestates[17][13] = "R6";
        bestates[17][14] = null;
        bestates[17][15] = null;
        bestates[17][16] = null;
        bestates[17][17] = "G21";
        bestates[17][18] = null;
        bestates[17][19] = null;

        bestates[18][0] = null;
        bestates[18][1] = null;
        bestates[18][2] = null;
        bestates[18][3] = null;
        bestates[18][4] = null;
        bestates[18][5] = null;
        bestates[18][6] = null;
        bestates[18][7] = "S22";
        bestates[18][8] = null;
        bestates[18][9] = null;
        bestates[18][10] = null;
        bestates[18][11] = null;
        bestates[18][12] = null;
        bestates[18][13] = null;
        bestates[18][14] = null;
        bestates[18][15] = null;
        bestates[18][16] = null;
        bestates[18][17] = null;
        bestates[18][18] = null;
        bestates[18][19] = null;

        bestates[19][0] = null;
        bestates[19][1] = null;
        bestates[19][2] = null;
        bestates[19][3] = null;
        bestates[19][4] = null;
        bestates[19][5] = null;
        bestates[19][6] = null;
        bestates[19][7] = null;
        bestates[19][8] = "S23";
        bestates[19][9] = "S24";
        bestates[19][10] = "S25";
        bestates[19][11] = "S26";
        bestates[19][12] = null;
        bestates[19][13] = null;
        bestates[19][14] = null;
        bestates[19][15] = null;
        bestates[19][16] = null;
        bestates[19][17] = null;
        bestates[19][18] = null;
        bestates[19][19] = null;

        bestates[20][0] = "R2";
        bestates[20][1] = "R2";
        bestates[20][2] = null;
        bestates[20][3] = null;
        bestates[20][4] = null;
        bestates[20][5] = "R2";
        bestates[20][6] = null;
        bestates[20][7] = "R2";
        bestates[20][8] = "R2";
        bestates[20][9] = "R2";
        bestates[20][10] = "R2";
        bestates[20][11] = "R2";
        bestates[20][12] = null;
        bestates[20][13] = "R2";
        bestates[20][14] = null;
        bestates[20][15] = null;
        bestates[20][16] = null;
        bestates[20][17] = null;
        bestates[20][18] = null;
        bestates[20][19] = null;

        bestates[21][0] = "R5";
        bestates[21][1] = "R5";
        bestates[21][2] = null;
        bestates[21][3] = null;
        bestates[21][4] = null;
        bestates[21][5] = "R5";
        bestates[21][6] = null;
        bestates[21][7] = "R5";
        bestates[21][8] = "R5";
        bestates[21][9] = "R5";
        bestates[21][10] = "R5";
        bestates[21][11] = "R5";
        bestates[21][12] = null;
        bestates[21][13] = "R5";
        bestates[21][14] = null;
        bestates[21][15] = null;
        bestates[21][16] = null;
        bestates[21][17] = null;
        bestates[21][18] = null;
        bestates[21][19] = null;

        bestates[22][0] = "R10";
        bestates[22][1] = "R10";
        bestates[22][2] = null;
        bestates[22][3] = null;
        bestates[22][4] = null;
        bestates[22][5] = "R10";
        bestates[22][6] = null;
        bestates[22][7] = "R10";
        bestates[22][8] = "R10";
        bestates[22][9] = "R10";
        bestates[22][10] = "R10";
        bestates[22][11] = "R10";
        bestates[22][12] = null;
        bestates[22][13] = "R10";
        bestates[22][14] = null;
        bestates[22][15] = null;
        bestates[22][16] = null;
        bestates[22][17] = null;
        bestates[22][18] = null;
        bestates[22][19] = null;

        bestates[23][0] = null;
        bestates[23][1] = null;
        bestates[23][2] = "S5";
        bestates[23][3] = "S6";
        bestates[23][4] = "S8";
        bestates[23][5] = null;
        bestates[23][6] = null;
        bestates[23][7] = null;
        bestates[23][8] = null;
        bestates[23][9] = null;
        bestates[23][10] = "S27";
        bestates[23][11] = null;
        bestates[23][12] = null;
        bestates[23][13] = null;
        bestates[23][14] = "G28";
        bestates[23][15] = null;
        bestates[23][16] = "G3";
        bestates[23][17] = null;
        bestates[23][18] = "G4";
        bestates[23][19] = "G7";

        bestates[24][0] = null;
        bestates[24][1] = null;
        bestates[24][2] = "S5";
        bestates[24][3] = "S6";
        bestates[24][4] = "S8";
        bestates[24][5] = null;
        bestates[24][6] = null;
        bestates[24][7] = null;
        bestates[24][8] = null;
        bestates[24][9] = null;
        bestates[24][10] = "S29";
        bestates[24][11] = null;
        bestates[24][12] = null;
        bestates[24][13] = null;
        bestates[24][14] = "G30";
        bestates[24][15] = null;
        bestates[24][16] = "G3";
        bestates[24][17] = null;
        bestates[24][18] = "G4";
        bestates[24][19] = "G7";

        bestates[25][0] = null;
        bestates[25][1] = null;
        bestates[25][2] = "S5";
        bestates[25][3] = "S6";
        bestates[25][4] = "S8";
        bestates[25][5] = null;
        bestates[25][6] = null;
        bestates[25][7] = null;
        bestates[25][8] = null;
        bestates[25][9] = null;
        bestates[25][10] = null;
        bestates[25][11] = null;
        bestates[25][12] = null;
        bestates[25][13] = null;
        bestates[25][14] = "G31";
        bestates[25][15] = null;
        bestates[25][16] = "G3";
        bestates[25][17] = null;
        bestates[25][18] = "G4";
        bestates[25][19] = "G7";

        bestates[26][0] = null;
        bestates[26][1] = null;
        bestates[26][2] = null;
        bestates[26][3] = null;
        bestates[26][4] = null;
        bestates[26][5] = null;
        bestates[26][6] = null;
        bestates[26][7] = null;
        bestates[26][8] = null;
        bestates[26][9] = null;
        bestates[26][10] = "S32";
        bestates[26][11] = null;
        bestates[26][12] = null;
        bestates[26][13] = null;
        bestates[26][14] = null;
        bestates[26][15] = null;
        bestates[26][16] = null;
        bestates[26][17] = null;
        bestates[26][18] = null;
        bestates[26][19] = null;

        bestates[27][0] = null;
        bestates[27][1] = null;
        bestates[27][2] = "S5";
        bestates[27][3] = "S6";
        bestates[27][4] = "S8";
        bestates[27][5] = null;
        bestates[27][6] = null;
        bestates[27][7] = null;
        bestates[27][8] = null;
        bestates[27][9] = null;
        bestates[27][10] = null;
        bestates[27][11] = null;
        bestates[27][12] = null;
        bestates[27][13] = null;
        bestates[27][14] = "G33";
        bestates[27][15] = null;
        bestates[27][16] = "G3";
        bestates[27][17] = null;
        bestates[27][18] = "G4";
        bestates[27][19] = "G7";

        bestates[28][0] = "R13";
        bestates[28][1] = "R13";
        bestates[28][2] = null;
        bestates[28][3] = null;
        bestates[28][4] = null;
        bestates[28][5] = "R13";
        bestates[28][6] = null;
        bestates[28][7] = "R13";
        bestates[28][8] = "R13";
        bestates[28][9] = "R13";
        bestates[28][10] = "R13";
        bestates[28][11] = "R13";
        bestates[28][12] = null;
        bestates[28][13] = "R13";
        bestates[28][14] = null;
        bestates[28][15] = null;
        bestates[28][16] = null;
        bestates[28][17] = null;
        bestates[28][18] = null;
        bestates[28][19] = null;

        bestates[29][0] = null;
        bestates[29][1] = null;
        bestates[29][2] = "S5";
        bestates[29][3] = "S6";
        bestates[29][4] = "S8";
        bestates[29][5] = null;
        bestates[29][6] = null;
        bestates[29][7] = null;
        bestates[29][8] = null;
        bestates[29][9] = null;
        bestates[29][10] = null;
        bestates[29][11] = null;
        bestates[29][12] = null;
        bestates[29][13] = null;
        bestates[29][14] = "G34";
        bestates[29][15] = null;
        bestates[29][16] = "G3";
        bestates[29][17] = null;
        bestates[29][18] = "G4";
        bestates[29][19] = "G7";

        bestates[30][0] = "R15";
        bestates[30][1] = "R15";
        bestates[30][2] = null;
        bestates[30][3] = null;
        bestates[30][4] = null;
        bestates[30][5] = "R15";
        bestates[30][6] = null;
        bestates[30][7] = "R15";
        bestates[30][8] = "R15";
        bestates[30][9] = "R15";
        bestates[30][10] = "R15";
        bestates[30][11] = "R15";
        bestates[30][12] = null;
        bestates[30][13] = "R15";
        bestates[30][14] = null;
        bestates[30][15] = null;
        bestates[30][16] = null;
        bestates[30][17] = null;
        bestates[30][18] = null;
        bestates[30][19] = null;

        bestates[31][0] = "R16";
        bestates[31][1] = "R16";
        bestates[31][2] = null;
        bestates[31][3] = null;
        bestates[31][4] = null;
        bestates[31][5] = "R16";
        bestates[31][6] = null;
        bestates[31][7] = "R16";
        bestates[31][8] = "R16";
        bestates[31][9] = "R16";
        bestates[31][10] = "R16";
        bestates[31][11] = "R16";
        bestates[31][12] = null;
        bestates[31][13] = "R16";
        bestates[31][14] = null;
        bestates[31][15] = null;
        bestates[31][16] = null;
        bestates[31][17] = null;
        bestates[31][18] = null;
        bestates[31][19] = null;

        bestates[32][0] = null;
        bestates[32][1] = null;
        bestates[32][2] = "S5";
        bestates[32][3] = "S6";
        bestates[32][4] = "S8";
        bestates[32][5] = null;
        bestates[32][6] = null;
        bestates[32][7] = null;
        bestates[32][8] = null;
        bestates[32][9] = null;
        bestates[32][10] = null;
        bestates[32][11] = null;
        bestates[32][12] = null;
        bestates[32][13] = null;
        bestates[32][14] = "G35";
        bestates[32][15] = null;
        bestates[32][16] = "G3";
        bestates[32][17] = null;
        bestates[32][18] = "G4";
        bestates[32][19] = "G7";

        bestates[33][0] = "R12";
        bestates[33][1] = "R12";
        bestates[33][2] = null;
        bestates[33][3] = null;
        bestates[33][4] = null;
        bestates[33][5] = "R12";
        bestates[33][6] = null;
        bestates[33][7] = "R12";
        bestates[33][8] = "R12";
        bestates[33][9] = "R12";
        bestates[33][10] = "R12";
        bestates[33][11] = "R12";
        bestates[33][12] = null;
        bestates[33][13] = "R12";
        bestates[33][14] = null;
        bestates[33][15] = null;
        bestates[33][16] = null;
        bestates[33][17] = null;
        bestates[33][18] = null;
        bestates[33][19] = null;

        bestates[34][0] = "R14";
        bestates[34][1] = "R14";
        bestates[34][2] = null;
        bestates[34][3] = null;
        bestates[34][4] = null;
        bestates[34][5] = "R14";
        bestates[34][6] = null;
        bestates[34][7] = "R14";
        bestates[34][8] = "R14";
        bestates[34][9] = "R14";
        bestates[34][10] = "R14";
        bestates[34][11] = "R14";
        bestates[34][12] = null;
        bestates[34][13] = "R14";
        bestates[34][14] = null;
        bestates[34][15] = null;
        bestates[34][16] = null;
        bestates[34][17] = null;
        bestates[34][18] = null;
        bestates[34][19] = null;

        bestates[35][0] = "R17";
        bestates[35][1] = "R17";
        bestates[35][2] = null;
        bestates[35][3] = null;
        bestates[35][4] = null;
        bestates[35][5] = "R17";
        bestates[35][6] = null;
        bestates[35][7] = "R17";
        bestates[35][8] = "R17";
        bestates[35][9] = "R17";
        bestates[35][10] = "R17";
        bestates[35][11] = "R17";
        bestates[35][12] = null;
        bestates[35][13] = "R17";
        bestates[35][14] = null;
        bestates[35][15] = null;
        bestates[35][16] = null;
        bestates[35][17] = null;
        bestates[35][18] = null;
        bestates[35][19] = null;

        bestates[36][0] = "R9";
        bestates[36][1] = "R9";
        bestates[36][2] = null;
        bestates[36][3] = null;
        bestates[36][4] = null;
        bestates[36][5] = "R9";
        bestates[36][6] = null;
        bestates[36][7] = "R9";
        bestates[36][8] = "R9";
        bestates[36][9] = "R9";
        bestates[36][10] = "R9";
        bestates[36][11] = "R9";
        bestates[36][12] = null;
        bestates[36][13] = "R9";
        bestates[36][14] = null;
        bestates[36][15] = null;
        bestates[36][16] = null;
        bestates[36][17] = null;
        bestates[36][18] = null;
        bestates[36][19] = null;

        besides[0][0] = "BE";
        besides[1][0] = "BEE";
        besides[2][0] = "BEE";
        besides[3][0] = "BT";
        besides[4][0] = "BTT";
        besides[5][0] = "BTT";
        besides[6][0] = "BF";
        besides[7][0] = "BF";
        besides[8][0] = "BF";
        besides[9][0] = "BF";
        besides[10][0] = "BF";
        besides[11][0] = "AST";
        besides[12][0] = "AST";
        besides[13][0] = "AST";
        besides[14][0] = "AST";
        besides[15][0] = "AST";
        besides[16][0] = "AST";

    }

    private void printbestates() {
        for (int i = 0; i < statess; i++) {
            System.out.print(i + ": ");
            for (int j = 0; j < varr; j++) {
                System.out.print(bestates[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    private void fillbesides() {
        for (int i = 0; i < linee; i++) {
            if (berhst.get(i).contains("#")) {
                besides[i][1] = String.valueOf(0);
            } else
                besides[i][1] = String.valueOf(berhst.get(i).size());
        }
    }

    private void printbesides() {
        for (int i = 0; i < linee; i++) {
            System.out.println(besides[i][0] + " " + besides[i][1]);
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    private void initialize() throws Exception {
        fillEmpty();
        symbolsfill();
        grammersIntoArray("grammer.txt", grammers, terminals);
        grammersIntoArray("be-grammer.txt", begrammers, beterminals);
        calculate();
        becalculate();
        printFirstFollow();
        printbestates();
        predicts();
        printPredicts();
        fillParseTable();
        printParseTable();
        fillRhst(grammers, rhst);
        fillRhst(begrammers, berhst);
        fillbesides();
        printRhst(rhst);
        printRhst(berhst);
        printbesides();
    }

    private void printsymbols() {
        System.out.println("\n\n\n");
        for (int i = 0; i < funcymbols.size(); i++) {
            for (int j = 0; j < funcymbols.get(i).size(); j++) {
                if (j == 0) {
                    System.out.println("symbols of " + funcymbols.get(i).get(j) + ": ");
                } else {
                    System.out.println(funcymbols.get(i).get(j).toString());
                }
            }
        }
        System.out.println("symbols of main: ");
        for (int i = 0; i < symbols.size(); i++) {
            System.out.println(symbols.get(i).toString());
        }
    }

    private void accept() throws Exception {
        System.out.println("done!\n");
        System.out.println("generated codes :");
        printCodes();
        printsymbols();
    }

    private void reject() {
        System.out.println("error happend in line : " + lines + " and column : " + chars + " !");
    }

    public static void main(String[] args) throws Exception {
        Compiler compiler = new Compiler();
        compiler.initialize();
        try {
            compiler.topdown();
            compiler.accept();
            System.out.println(compiler.ssforpc);
        } catch (Exception e) {
            //throw e;
            compiler.reject();
        }
    }
}