import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;


public class Compiler {

    static String keywords[] = new String[]{"void", "int", "char", "float", "string", "bool",
            "return", "break", "if", "do", "while", "for", "switch", "case", "default", "continue",
            "define", "#include", "enum", "struct", "union", "false", "true", "typdef", "unsigned",
            "repeat", "until", "override", "else", "in", "sizeof", "null"};

    class Token {
        TokenType type;
        String value;

        public Token(TokenType type, String value) {
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

    char ch;
    int lines = 1;
    int chars = 1;

    int count = 14, n = 0, m = 0;
    String[][] grammers = new String[100][100];
    String[][] calc_first = new String[100][100];
    String[][] calc_follow = new String[100][100];
    String[] f = new String[100];
    String[] first = new String[100];

    Stack<Token> parsstack = new Stack();
    int[][] parstable;
    ArrayList<ArrayList<String>> rhst = new ArrayList<>();

    ArrayList<ArrayList<String>> predicts = new ArrayList<>();
    ArrayList<String> terminals = new ArrayList<>();
    ArrayList<String> vars = new ArrayList<>();


    enum TokenType {keyword, integernum, realnum, character, stringliteral, error, identifier, specialtoken, include, end, variable}

    boolean iskeyword(String word) {
        for (int i = 0; i < keywords.length; i++) {
            if (word.equals(keywords[i]))
                return true;
        }
        return false;
    }

    Token scanner(FileReader inf) throws IOException {
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
                token.type = TokenType.specialtoken;
                token.value = ":=";
                ch = (char) inf.read();
                chars++;
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

    Token reader(FileReader inf) throws IOException {
        return scanner(inf);
    }

    void fillParseTable() {
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

    int getProduction(Token pop, Token token) {
        return parstable[vars.indexOf(pop.value)][terminals.indexOf(token.value)];
    }

    void grammersIntoArray() throws IOException {
        FileReader inf = new FileReader("grammer.txt");
        char save = (char) inf.read();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                grammers[i][j] = "";
            }
        }
        int i = 0, j = 0;
        while (save != '@') {
            if (save == '\n') {
                j = 0;
                i++;
            }
            if (save != ' ' && save != '.' && save != '\n' && save != '\r') {
                while (save != ' ') {
                    grammers[i][j] += save;
                    save = (char) inf.read();
                }
                if (!grammers[i][j].equals("#") && !grammers[i][j].equals("="))
                    if (!terminals.contains(grammers[i][j])) {
                        if (isNotUpper(grammers[i][j]))
                            terminals.add(grammers[i][j]);
                    }
                j++;
            }
            save = (char) inf.read();
        }
        terminals.add("$");
    }

    boolean isNotUpper(String check) {
        for (int i = 0; i < check.length(); i++) {
            if (check.charAt(i) >= 65 && check.charAt(i) <= 90) {
                return false;
            }
        }
        return true;
    }

    void firstAndFollow() {

        int jm = 0;
        int km = 0;

        String gotfirst;
        String gotfollow;

        String[] done = new String[count];
        String[] donee = new String[count];
        int ptr = -1;

        for (int k = 0; k < count; k++) {
            for (int kay = 0; kay < 100; kay++) {
                calc_first[k][kay] = "!";
            }
        }
        int point1 = 0, point2, xxx;

        for (int k = 0; k < count; k++) {
            gotfirst = grammers[k][0];
            point2 = 0;
            xxx = 0;

            for (int kay = 0; kay <= ptr; kay++)
                if (gotfirst.equals(done[kay]))
                    xxx = 1;

            if (xxx == 1)
                continue;

            findfirst(gotfirst, 0, 0);
            ptr += 1;

            done[ptr] = gotfirst;
            calc_first[point1][point2++] = gotfirst;

            for (int i = 0 + jm; i < n; i++) {
                int lark = 0, chk = 0;
                for (lark = 0; lark < point2; lark++) {
                    if (first[i].equals(calc_first[point1][lark])) {
                        chk = 1;
                        break;
                    }
                }
                if (chk == 0) {
                    calc_first[point1][point2++] = first[i];
                }
            }
            jm = n;
            point1++;
        }
        ptr = -1;

        for (int k = 0; k < count; k++) {
            for (int kay = 0; kay < 100; kay++) {
                calc_follow[k][kay] = "!";
            }
        }
        point1 = 0;
        for (int e = 0; e < count; e++) {
            gotfollow = grammers[e][0];
            point2 = 0;
            xxx = 0;

            for (int kay = 0; kay <= ptr; kay++)
                if (gotfollow.equals(donee[kay]))
                    xxx = 1;

            if (xxx == 1)
                continue;
            follow(gotfollow);
            ptr += 1;

            donee[ptr] = gotfollow;
            calc_follow[point1][point2++] = gotfollow;

            for (int i = 0 + km; i < m; i++) {
                int lark = 0, chk = 0;
                for (lark = 0; lark < point2; lark++) {
                    if (f[i].equals(calc_follow[point1][lark])) {
                        chk = 1;
                        break;
                    }
                }
                if (chk == 0) {
                    calc_follow[point1][point2++] = f[i];
                }
            }
            km = m;
            point1++;
        }
    }

    void follow(String gotfollow) {
        int i, j;
        if (grammers[0][0].equals(gotfollow)) {
            f[m++] = "$";
        }
        for (i = 0; i < 100; i++) {
            for (j = 2; j < 100; j++) {
                if (grammers[i][j].equals(gotfollow)) {
                    if (!grammers[i][j + 1].equals("")) {
                        followfirst(grammers[i][j + 1], i, (j + 2));
                    }
                    if (grammers[i][j + 1].equals("") && (!gotfollow.equals(grammers[i][0]))) {
                        follow(grammers[i][0]);
                    }
                }
            }
        }
    }

    void findfirst(String gotfirst, int q1, int q2) {
        int j;
        if ((isNotUpper(gotfirst))) {
            first[n++] = gotfirst;
        }
        for (j = 0; j < count; j++) {
            if (grammers[j][0].equals(gotfirst)) {
                if (grammers[j][2].equals("#")) {
                    if (grammers[q1][q2].equals(""))
                        first[n++] = "#";
                    else if (!grammers[q1][q2].equals("")
                            && (q1 != 0 || q2 != 0)) {
                        findfirst(grammers[q1][q2], q1, (q2 + 1));
                    } else
                        first[n++] = "#";
                } else if (isNotUpper(grammers[j][2])) {
                    first[n++] = grammers[j][2];
                } else {
                    findfirst(grammers[j][2], j, 3);
                }
            }
        }
    }

    void followfirst(String gotfollow, int c1, int c2) {
        if ((isNotUpper(gotfollow))) {
            f[m++] = gotfollow;
        } else {
            int i = 0, j = 1;
            for (i = 0; i < count; i++) {
                if (calc_first[i][0].equals(gotfollow))
                    break;
            }
            while (!calc_first[i][j].equals("!")) {
                if (!calc_first[i][j].equals("#")) {
                    f[m++] = calc_first[i][j];
                } else {
                    if (grammers[c1][c2].equals("")) {
                        follow(grammers[c1][0]);
                    } else {
                        followfirst(grammers[c1][c2], c1, c2 + 1);
                    }
                }
                j++;
            }
        }
    }

    void printFirstFollow() {
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < 100; j++) {
                if (!(calc_first[i][j].equals("!"))) {
                    if (j == 0) {
                        System.out.print("First " + calc_first[i][j] + " : ");
                        vars.add(calc_first[i][j]);
                    } else {
                        System.out.print(calc_first[i][j] + " ");
                    }
                } else {
                    break;
                }
            }
            System.out.println();
        }
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < 100; j++) {
                if (!(calc_follow[i][j].equals("!"))) {
                    if (j == 0) {
                        System.out.print("Follow " + calc_follow[i][j] + " : ");
                    } else {
                        System.out.print(calc_follow[i][j] + " ");
                    }
                } else {
                    break;
                }
            }
            System.out.println();
        }
    }

    void printParseTable() {
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

    void printPredicts() {
        for (int i = 0; i < predicts.size(); i++) {
            System.out.print("predist " + (i + 1) + ": ");
            for (int j = 0; j < predicts.get(i).size(); j++) {
                System.out.print(predicts.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    ArrayList<String> getFirst(String var) {
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < vars.size(); i++) {
            if (calc_first[i][0].equals(var)) {
                for (int j = 1; j < 100; j++) {
                    if (!calc_first[i][j].equals("!")) {
                        res.add(calc_first[i][j]);
                    }
                }
            }
        }
        return res;
    }

    ArrayList<String> getFollow(String var) {
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < vars.size(); i++) {
            if (calc_follow[i][0].equals(var)) {
                for (int j = 1; j < 100; j++) {
                    if (!calc_follow[i][j].equals("!")) {
                        res.add(calc_follow[i][j]);
                    }
                }
            }
        }
        return res;
    }

    void predicts() {
        for (int i = 0; i < count; i++) {
            ArrayList<String> temp = new ArrayList<>();
            String var = grammers[i][0];
            ArrayList<String> right = new ArrayList<>();
            for (int j = 2; j < 100; j++) {
                if (!grammers[i][j].equals("")) {
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

    void fillRhst() {
        for (int i = 0; i < grammers.length; i++) {
            ArrayList<String> temp = new ArrayList();
            for (int j = 99; j >= 2; j--) {
                if (!grammers[i][j].equals("")) {
                    temp.add(grammers[i][j]);
                }
            }
            if (temp.size() > 0)
                rhst.add(temp);
        }
    }

    void printRhst() {
        for (int i = 0; i < rhst.size(); i++) {
            for (int j = 0; j < rhst.get(i).size(); j++) {
                System.out.print(rhst.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void parse() throws Exception {
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
                        throw new Exception();
                    }
                    parsstack.pop();
                    for (int i = 0; i < rhst.get(product).size(); i++) {
                        if (rhst.get(product).get(i).equals("id")) {
                            parsstack.push(new Token(TokenType.identifier, rhst.get(product).get(i)));
                        } else if (rhst.get(product).get(i).equals("num")) {
                            parsstack.push(new Token(TokenType.integernum, rhst.get(product).get(i)));
                        } else if (rhst.get(product).get(i).equals("real")) {
                            parsstack.push(new Token(TokenType.realnum, rhst.get(product).get(i)));
                        } else if (rhst.get(product).get(i).equals("include")) {
                            parsstack.push(new Token(TokenType.include, rhst.get(product).get(i)));
                        } else if (rhst.get(product).get(i).equals("#")) {
                        } else if (isNotUpper(rhst.get(product).get(i))) {
                            parsstack.push(new Token(TokenType.specialtoken, rhst.get(product).get(i)));
                        } else {
                            parsstack.push(new Token(TokenType.variable, rhst.get(product).get(i)));
                        }
                    }
                    break;
                case identifier:
                case integernum:
                case realnum:
                case include:
                    if (token.type == TokenType.identifier || token.type == TokenType.integernum || token.type == TokenType.include || token.type == TokenType.realnum) {
                        parsstack.pop();
                        token = reader(inf);
                    } else {
                        throw new Exception();
                    }
                    break;
                case keyword:
                case specialtoken:
                    if (token.value.equals(parsstack.peek().value)) {
                        parsstack.pop();
                        token = reader(inf);
                    } else {
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

    public static void main(String[] args) throws Exception {
        Compiler compiler = new Compiler();
        compiler.grammersIntoArray();
        compiler.firstAndFollow();
        compiler.printFirstFollow();
        compiler.predicts();
        compiler.printPredicts();
        compiler.fillParseTable();
        compiler.printParseTable();
        compiler.fillRhst();
        compiler.printRhst();
        try {
            compiler.parse();
            System.out.println("done!");
        } catch (Exception e) {
            System.out.println("error happend in line : " + compiler.lines + " and column : " + compiler.chars + " !");
        }
    }
}