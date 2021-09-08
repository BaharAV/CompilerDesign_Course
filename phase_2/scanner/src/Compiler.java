import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Compiler {

    static String keywords[] = new String[]{"void", "int", "char", "float", "string", "bool", "return", "break", "if", "do", "while", "for", "switch", "case", "default", "continue", "define", "include", "enum", "struct", "union", "false", "true", "typdef", "unsigned", "repeat", "until", "override", "else", "in", "sizeof", "null"};

    class Token {
        TokenType type;
        String value;

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

    enum TokenType {keyword, integernum, realnum, character, stringliteral, error, identifier, specialtoken, include, empty}

    boolean iskeyword(String word) {
        for (int i = 0; i < keywords.length; i++) {
            if (word.equals(keywords[i]))
                return true;
        }
        return false;
    }

    Token scanner(FileReader inf) throws IOException {
        Token token = new Token();
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
            case '\t':
            case ' ':
            default:
                ch = (char) inf.read();
                chars++;
                return scanner(inf);
        }
    }

    void reader(FileReader inf) throws IOException {
        System.out.println(scanner(inf).toString());
    }

    public static void main(String[] args) throws IOException {
        FileReader inf = new FileReader("code.txt");
        Compiler compiler = new Compiler();
        compiler.ch = (char) inf.read();
        compiler.chars++;
        do {
            compiler.reader(inf);
        } while (compiler.ch != '$');
    }
}
