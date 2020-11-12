/* The following code was generated by JFlex 1.4.3 on 10/8/14 3:56 PM */

package com.intellij.json;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import static com.intellij.json.JsonElementTypes.*;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.3
 * on 10/8/14 3:56 PM from the specification file
 * <tt>/home/east825/develop/repos/IDEA/community/json/gen/com/intellij/json/_JsonLexer.flex</tt>
 */
public class _JsonLexer implements FlexLexer {
  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\20\1\3\1\2\1\0\1\3\1\1\16\20\4\0\1\3\1\0"+
    "\1\6\1\0\1\17\2\0\1\10\2\0\1\5\1\16\1\25\1\11"+
    "\1\14\1\4\1\12\11\13\1\26\6\0\4\17\1\15\25\17\1\23"+
    "\1\7\1\24\1\0\1\17\1\0\1\34\3\17\1\32\1\33\5\17"+
    "\1\35\1\17\1\37\3\17\1\30\1\36\1\27\1\31\5\17\1\21"+
    "\1\0\1\22\1\0\41\20\2\0\4\17\4\0\1\17\2\0\1\20"+
    "\7\0\1\17\4\0\1\17\5\0\27\17\1\0\37\17\1\0\u01ca\17"+
    "\4\0\14\17\16\0\5\17\7\0\1\17\1\0\1\17\21\0\160\20"+
    "\5\17\1\0\2\17\2\0\4\17\10\0\1\17\1\0\3\17\1\0"+
    "\1\17\1\0\24\17\1\0\123\17\1\0\213\17\1\0\5\20\2\0"+
    "\236\17\11\0\46\17\2\0\1\17\7\0\47\17\7\0\1\17\1\0"+
    "\55\20\1\0\1\20\1\0\2\20\1\0\2\20\1\0\1\20\10\0"+
    "\33\17\5\0\3\17\15\0\5\20\6\0\1\17\4\0\13\20\5\0"+
    "\53\17\37\20\4\0\2\17\1\20\143\17\1\0\1\17\10\20\1\0"+
    "\6\20\2\17\2\20\1\0\4\20\2\17\12\20\3\17\2\0\1\17"+
    "\17\0\1\20\1\17\1\20\36\17\33\20\2\0\131\17\13\20\1\17"+
    "\16\0\12\20\41\17\11\20\2\17\4\0\1\17\5\0\26\17\4\20"+
    "\1\17\11\20\1\17\3\20\1\17\5\20\22\0\31\17\3\20\104\0"+
    "\1\17\1\0\13\17\67\0\33\20\1\0\4\20\66\17\3\20\1\17"+
    "\22\20\1\17\7\20\12\17\2\20\2\0\12\20\1\0\7\17\1\0"+
    "\7\17\1\0\3\20\1\0\10\17\2\0\2\17\2\0\26\17\1\0"+
    "\7\17\1\0\1\17\3\0\4\17\2\0\1\20\1\17\7\20\2\0"+
    "\2\20\2\0\3\20\1\17\10\0\1\20\4\0\2\17\1\0\3\17"+
    "\2\20\2\0\12\20\4\17\7\0\1\17\5\0\3\20\1\0\6\17"+
    "\4\0\2\17\2\0\26\17\1\0\7\17\1\0\2\17\1\0\2\17"+
    "\1\0\2\17\2\0\1\20\1\0\5\20\4\0\2\20\2\0\3\20"+
    "\3\0\1\20\7\0\4\17\1\0\1\17\7\0\14\20\3\17\1\20"+
    "\13\0\3\20\1\0\11\17\1\0\3\17\1\0\26\17\1\0\7\17"+
    "\1\0\2\17\1\0\5\17\2\0\1\20\1\17\10\20\1\0\3\20"+
    "\1\0\3\20\2\0\1\17\17\0\2\17\2\20\2\0\12\20\1\0"+
    "\1\17\17\0\3\20\1\0\10\17\2\0\2\17\2\0\26\17\1\0"+
    "\7\17\1\0\2\17\1\0\5\17\2\0\1\20\1\17\7\20\2\0"+
    "\2\20\2\0\3\20\10\0\2\20\4\0\2\17\1\0\3\17\2\20"+
    "\2\0\12\20\1\0\1\17\20\0\1\20\1\17\1\0\6\17\3\0"+
    "\3\17\1\0\4\17\3\0\2\17\1\0\1\17\1\0\2\17\3\0"+
    "\2\17\3\0\3\17\3\0\14\17\4\0\5\20\3\0\3\20\1\0"+
    "\4\20\2\0\1\17\6\0\1\20\16\0\12\20\11\0\1\17\7\0"+
    "\3\20\1\0\10\17\1\0\3\17\1\0\27\17\1\0\12\17\1\0"+
    "\5\17\3\0\1\17\7\20\1\0\3\20\1\0\4\20\7\0\2\20"+
    "\1\0\2\17\6\0\2\17\2\20\2\0\12\20\22\0\2\20\1\0"+
    "\10\17\1\0\3\17\1\0\27\17\1\0\12\17\1\0\5\17\2\0"+
    "\1\20\1\17\7\20\1\0\3\20\1\0\4\20\7\0\2\20\7\0"+
    "\1\17\1\0\2\17\2\20\2\0\12\20\1\0\2\17\17\0\2\20"+
    "\1\0\10\17\1\0\3\17\1\0\51\17\2\0\1\17\7\20\1\0"+
    "\3\20\1\0\4\20\1\17\10\0\1\20\10\0\2\17\2\20\2\0"+
    "\12\20\12\0\6\17\2\0\2\20\1\0\22\17\3\0\30\17\1\0"+
    "\11\17\1\0\1\17\2\0\7\17\3\0\1\20\4\0\6\20\1\0"+
    "\1\20\1\0\10\20\22\0\2\20\15\0\60\17\1\20\2\17\7\20"+
    "\4\0\10\17\10\20\1\0\12\20\47\0\2\17\1\0\1\17\2\0"+
    "\2\17\1\0\1\17\2\0\1\17\6\0\4\17\1\0\7\17\1\0"+
    "\3\17\1\0\1\17\1\0\1\17\2\0\2\17\1\0\4\17\1\20"+
    "\2\17\6\20\1\0\2\20\1\17\2\0\5\17\1\0\1\17\1\0"+
    "\6\20\2\0\12\20\2\0\4\17\40\0\1\17\27\0\2\20\6\0"+
    "\12\20\13\0\1\20\1\0\1\20\1\0\1\20\4\0\2\20\10\17"+
    "\1\0\44\17\4\0\24\20\1\0\2\20\5\17\13\20\1\0\44\20"+
    "\11\0\1\20\71\0\53\17\24\20\1\17\12\20\6\0\6\17\4\20"+
    "\4\17\3\20\1\17\3\20\2\17\7\20\3\17\4\20\15\17\14\20"+
    "\1\17\17\20\2\0\46\17\1\0\1\17\5\0\1\17\2\0\53\17"+
    "\1\0\u014d\17\1\0\4\17\2\0\7\17\1\0\1\17\1\0\4\17"+
    "\2\0\51\17\1\0\4\17\2\0\41\17\1\0\4\17\2\0\7\17"+
    "\1\0\1\17\1\0\4\17\2\0\17\17\1\0\71\17\1\0\4\17"+
    "\2\0\103\17\2\0\3\20\40\0\20\17\20\0\125\17\14\0\u026c\17"+
    "\2\0\21\17\1\0\32\17\5\0\113\17\3\0\3\17\17\0\15\17"+
    "\1\0\4\17\3\20\13\0\22\17\3\20\13\0\22\17\2\20\14\0"+
    "\15\17\1\0\3\17\1\0\2\20\14\0\64\17\40\20\3\0\1\17"+
    "\3\0\2\17\1\20\2\0\12\20\41\0\3\20\2\0\12\20\6\0"+
    "\130\17\10\0\51\17\1\20\1\17\5\0\106\17\12\0\35\17\3\0"+
    "\14\20\4\0\14\20\12\0\12\20\36\17\2\0\5\17\13\0\54\17"+
    "\4\0\21\20\7\17\2\20\6\0\12\20\46\0\27\17\5\20\4\0"+
    "\65\17\12\20\1\0\35\20\2\0\13\20\6\0\12\20\15\0\1\17"+
    "\130\0\5\20\57\17\21\20\7\17\4\0\12\20\21\0\11\20\14\0"+
    "\3\20\36\17\15\20\2\17\12\20\54\17\16\20\14\0\44\17\24\20"+
    "\10\0\12\20\3\0\3\17\12\20\44\17\122\0\3\20\1\0\25\20"+
    "\4\17\1\20\4\17\3\20\2\17\11\0\300\17\47\20\25\0\4\20"+
    "\u0116\17\2\0\6\17\2\0\46\17\2\0\6\17\2\0\10\17\1\0"+
    "\1\17\1\0\1\17\1\0\1\17\1\0\37\17\2\0\65\17\1\0"+
    "\7\17\1\0\1\17\3\0\3\17\1\0\7\17\3\0\4\17\2\0"+
    "\6\17\4\0\15\17\5\0\3\17\1\0\7\17\16\0\5\20\32\0"+
    "\5\20\20\0\2\17\23\0\1\17\13\0\5\20\5\0\6\20\1\0"+
    "\1\17\15\0\1\17\20\0\15\17\3\0\33\17\25\0\15\20\4\0"+
    "\1\20\3\0\14\20\21\0\1\17\4\0\1\17\2\0\12\17\1\0"+
    "\1\17\3\0\5\17\6\0\1\17\1\0\1\17\1\0\1\17\1\0"+
    "\4\17\1\0\13\17\2\0\4\17\5\0\5\17\4\0\1\17\21\0"+
    "\51\17\u0a77\0\57\17\1\0\57\17\1\0\205\17\6\0\4\17\3\20"+
    "\2\17\14\0\46\17\1\0\1\17\5\0\1\17\2\0\70\17\7\0"+
    "\1\17\17\0\1\20\27\17\11\0\7\17\1\0\7\17\1\0\7\17"+
    "\1\0\7\17\1\0\7\17\1\0\7\17\1\0\7\17\1\0\7\17"+
    "\1\0\40\20\57\0\1\17\u01d5\0\3\17\31\0\11\17\6\20\1\0"+
    "\5\17\2\0\5\17\4\0\126\17\2\0\2\20\2\0\3\17\1\0"+
    "\132\17\1\0\4\17\5\0\51\17\3\0\136\17\21\0\33\17\65\0"+
    "\20\17\u0200\0\u19b6\17\112\0\u51cd\17\63\0\u048d\17\103\0\56\17\2\0"+
    "\u010d\17\3\0\20\17\12\20\2\17\24\0\57\17\1\20\4\0\12\20"+
    "\1\0\31\17\7\0\1\20\120\17\2\20\45\0\11\17\2\0\147\17"+
    "\2\0\4\17\1\0\4\17\14\0\13\17\115\0\12\17\1\20\3\17"+
    "\1\20\4\17\1\20\27\17\5\20\20\0\1\17\7\0\64\17\14\0"+
    "\2\20\62\17\21\20\13\0\12\20\6\0\22\20\6\17\3\0\1\17"+
    "\4\0\12\20\34\17\10\20\2\0\27\17\15\20\14\0\35\17\3\0"+
    "\4\20\57\17\16\20\16\0\1\17\12\20\46\0\51\17\16\20\11\0"+
    "\3\17\1\20\10\17\2\20\2\0\12\20\6\0\27\17\3\0\1\17"+
    "\1\20\4\0\60\17\1\20\1\17\3\20\2\17\2\20\5\17\2\20"+
    "\1\17\1\20\1\17\30\0\3\17\2\0\13\17\5\20\2\0\3\17"+
    "\2\20\12\0\6\17\2\0\6\17\2\0\6\17\11\0\7\17\1\0"+
    "\7\17\221\0\43\17\10\20\1\0\2\20\2\0\12\20\6\0\u2ba4\17"+
    "\14\0\27\17\4\0\61\17\u2104\0\u016e\17\2\0\152\17\46\0\7\17"+
    "\14\0\5\17\5\0\1\17\1\20\12\17\1\0\15\17\1\0\5\17"+
    "\1\0\1\17\1\0\2\17\1\0\2\17\1\0\154\17\41\0\u016b\17"+
    "\22\0\100\17\2\0\66\17\50\0\15\17\3\0\20\20\20\0\7\20"+
    "\14\0\2\17\30\0\3\17\31\0\1\17\6\0\5\17\1\0\207\17"+
    "\2\0\1\20\4\0\1\17\13\0\12\20\7\0\32\17\4\0\1\17"+
    "\1\0\32\17\13\0\131\17\3\0\6\17\2\0\6\17\2\0\6\17"+
    "\2\0\3\17\3\0\2\17\3\0\2\17\22\0\3\20\4\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\1\2\1\1\1\3\1\4\1\1\2\5"+
    "\1\6\1\7\1\10\1\11\1\12\1\13\1\14\3\6"+
    "\1\15\1\16\1\3\2\0\1\4\1\0\1\5\3\6"+
    "\1\0\2\5\3\6\1\16\1\17\1\6\1\20\1\21";

  private static int [] zzUnpackAction() {
    int [] result = new int[41];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\40\0\100\0\140\0\200\0\240\0\300\0\340"+
    "\0\u0100\0\u0120\0\40\0\40\0\40\0\40\0\40\0\40"+
    "\0\u0140\0\u0160\0\u0180\0\u01a0\0\u01c0\0\40\0\u01e0\0\u0200"+
    "\0\40\0\u0220\0\u0240\0\u0260\0\u0280\0\u02a0\0\u02c0\0\u02e0"+
    "\0\u0300\0\u0320\0\u0340\0\u0360\0\40\0\u0120\0\u0380\0\u0120"+
    "\0\u0120";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[41];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\3\3\1\4\1\2\1\5\1\2\1\6\1\7"+
    "\1\10\1\11\1\2\1\12\1\2\1\12\1\2\1\13"+
    "\1\14\1\15\1\16\1\17\1\20\1\21\3\12\1\22"+
    "\3\12\1\23\41\0\3\3\40\0\1\24\1\25\32\0"+
    "\1\5\2\0\3\5\1\26\1\27\30\5\1\6\2\0"+
    "\4\6\1\30\1\31\27\6\12\0\1\10\1\11\40\0"+
    "\1\32\1\33\14\0\1\33\17\0\2\11\1\32\1\33"+
    "\14\0\1\33\17\0\2\12\1\0\1\12\1\0\2\12"+
    "\6\0\11\12\12\0\2\12\1\0\1\12\1\0\2\12"+
    "\6\0\1\12\1\34\7\12\12\0\2\12\1\0\1\12"+
    "\1\0\2\12\6\0\5\12\1\35\3\12\12\0\2\12"+
    "\1\0\1\12\1\0\2\12\6\0\2\12\1\36\6\12"+
    "\2\24\1\0\35\24\5\25\1\37\32\25\1\5\2\0"+
    "\35\5\1\6\2\0\35\6\12\0\2\40\35\0\3\41"+
    "\2\0\1\41\33\0\2\12\1\0\1\12\1\0\2\12"+
    "\6\0\2\12\1\42\6\12\12\0\2\12\1\0\1\12"+
    "\1\0\2\12\6\0\6\12\1\43\2\12\12\0\2\12"+
    "\1\0\1\12\1\0\2\12\6\0\6\12\1\44\2\12"+
    "\4\25\1\45\1\37\32\25\12\0\2\40\1\0\1\33"+
    "\14\0\1\33\17\0\2\41\36\0\2\12\1\0\1\12"+
    "\1\0\2\12\6\0\3\12\1\46\5\12\12\0\2\12"+
    "\1\0\1\12\1\0\2\12\6\0\7\12\1\47\1\12"+
    "\12\0\2\12\1\0\1\12\1\0\2\12\6\0\6\12"+
    "\1\50\2\12\12\0\2\12\1\0\1\12\1\0\2\12"+
    "\6\0\3\12\1\51\5\12";

  private static int [] zzUnpackTrans() {
    int [] result = new int[928];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;
  private static final char[] EMPTY_BUFFER = new char[0];
  private static final int YYEOF = -1;
  private static java.io.Reader zzReader = null; // Fake

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\10\1\6\11\5\1\1\11\2\0\1\11"+
    "\1\0\4\1\1\0\5\1\1\11\4\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[41];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** this buffer may contains the current text array to be matched when it is cheap to acquire it */
  private char[] zzBufferArray;

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /* user code: */
  public _JsonLexer() {
    this((java.io.Reader)null);
  }


  public _JsonLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public _JsonLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 2218) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  public final int getTokenStart(){
    return zzStartRead;
  }

  public final int getTokenEnd(){
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end,int initialState){
    zzBuffer = buffer;
    zzBufferArray = com.intellij.util.text.CharArrayUtil.fromSequenceWithoutCopying(buffer);
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzPushbackPos = 0;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBufferArray != null ? zzBufferArray[zzStartRead+pos]:zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;
    char[] zzBufferArrayL = zzBufferArray;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL)
            zzInput = (zzBufferArrayL != null ? zzBufferArrayL[zzCurrentPosL++] : zzBufferL.charAt(zzCurrentPosL++));
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = (zzBufferArrayL != null ? zzBufferArrayL[zzCurrentPosL++] : zzBufferL.charAt(zzCurrentPosL++));
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 8: 
          { return R_CURLY;
          }
        case 18: break;
        case 7: 
          { return L_CURLY;
          }
        case 19: break;
        case 16: 
          { return NULL;
          }
        case 20: break;
        case 6: 
          { return INDENTIFIER;
          }
        case 21: break;
        case 9: 
          { return L_BRACKET;
          }
        case 22: break;
        case 13: 
          { return LINE_COMMENT;
          }
        case 23: break;
        case 11: 
          { return COMMA;
          }
        case 24: break;
        case 2: 
          { return com.intellij.psi.TokenType.WHITE_SPACE;
          }
        case 25: break;
        case 3: 
          { return DOUBLE_QUOTED_STRING;
          }
        case 26: break;
        case 1: 
          { return com.intellij.psi.TokenType.BAD_CHARACTER;
          }
        case 27: break;
        case 15: 
          { return TRUE;
          }
        case 28: break;
        case 12: 
          { return COLON;
          }
        case 29: break;
        case 4: 
          { return SINGLE_QUOTED_STRING;
          }
        case 30: break;
        case 5: 
          { return NUMBER;
          }
        case 31: break;
        case 17: 
          { return FALSE;
          }
        case 32: break;
        case 14: 
          { return BLOCK_COMMENT;
          }
        case 33: break;
        case 10: 
          { return R_BRACKET;
          }
        case 34: break;
        default:
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            return null;
          }
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
