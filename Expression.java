package apps;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;

	/**
	 * Scalar symbols in the expression
	 */
	ArrayList<ScalarSymbol> scalars;

	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;

	/**
	 * String containing all delimiters (characters other than variables and
	 * constants), to be used with StringTokenizer
	 */
	public static final String delims = " \t*+-/()[]";

	/**
	 * Initializes this Expression object with an input expression. Sets all
	 * other fields to null.
	 * 
	 * @param expr
	 *            Expression
	 */
	public Expression(String expr) {
		this.expr = expr;
	}

	/**
	 * Populates the scalars and arrays lists with symbols for scalar and array
	 * variables in the expression. For every variable, a SINGLE symbol is
	 * created and stored, even if it appears more than once in the expression.
	 * At this time, values for all variables are set to zero - they will be
	 * loaded from a file in the loadSymbolValues method.
	 */
	public void buildSymbols() {
		scalars = new ArrayList<ScalarSymbol>();
		arrays = new ArrayList<ArraySymbol>();
		Pattern scalarPattern = Pattern.compile("[A-Za-z]\\w*");
		Pattern arrayPattern = Pattern.compile("[A-Za-z]\\w*(\\[)");
		Matcher scalarName = scalarPattern.matcher(expr);
		Matcher arrayName = arrayPattern.matcher(expr);
		while (scalarName.find()) {
			String s = scalarName.group();
			System.out.println(scalarName.start());
			System.out.println(scalarName.end());
			if (scalarName.end() == expr.length() || expr.charAt(scalarName.end()) != '[') {
				System.out.println("S: " + s.trim());
				scalars.add(new ScalarSymbol(s.trim()));
			}
		}
		while (arrayName.find()) {
			String a = arrayName.group().trim();
			System.out.println("A: " + arrayName.group().trim());
			System.out.println(arrayName.start());
			System.out.println(arrayName.end());
			for (int i = 0; i < a.length(); i++) {
				if (a.charAt(i) != '[')
					continue;
				else
					arrays.add(new ArraySymbol(a.substring(0, i)));
			}
		}
		/** COMPLETE THIS METHOD **/
	}

	/**
	 * Loads values for symbols in the expression
	 * 
	 * @param sc
	 *            Scanner for values input
	 * @throws IOException
	 *             If there is a problem with the input
	 */
	public void loadSymbolValues(Scanner sc) throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String sym = st.nextToken();
			ScalarSymbol ssymbol = new ScalarSymbol(sym);
			ArraySymbol asymbol = new ArraySymbol(sym);
			int ssi = scalars.indexOf(ssymbol);
			int asi = arrays.indexOf(asymbol);
			if (ssi == -1 && asi == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				scalars.get(ssi).value = num;
			} else { // array symbol
				asymbol = arrays.get(asi);
				asymbol.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					asymbol.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression, using RECURSION to evaluate subexpressions and
	 * to evaluate array subscript expressions.
	 * 
	 * @return Result of evaluation
	 */
	public float evaluate() {
		return evaluate(expr);
		/** COMPLETE THIS METHOD **/
		// following line just a placeholder for compilation
	}

	private float evaluate(String s) {
		String vary = "";
		int num = 0;
		boolean isParen = false;
		Stack<Integer> numbers = new Stack<Integer>();
		Stack<Integer> paren = new Stack<Integer>();
		Stack<String> operations = new Stack<String>();
		for (int i = 0; i < s.length(); i++) {
			String val = s.substring(i, i + 1);
			if (isParen && !val.equals("(") && !val.equals(")") && !val.equals("]") && !val.equals("["))
				continue;
			try {
				int a = Integer.parseInt(val);
				num = num * 10 + a;
				continue;
			} catch (NumberFormatException e) {
			}
			if (val.equals("(") || val.equals("[")) {
				paren.push(i);
				isParen = true;
			} else if (val.equals(")")) {
				int h = paren.pop() + 1;
				if (paren.isEmpty()) {
					isParen = false;
					vary = "";
					num = (int) evaluate(s.substring(h, i));
				}
			} else if (val.equals("]")) {
				int h = paren.pop() + 1;
				if (paren.isEmpty()) {
					isParen = false;
					int g = (int) evaluate(s.substring(h, i));
					for (ArraySymbol z : arrays)
						if (z.name.equals(vary)) {
							try {
								num = z.values[g];
							} catch (ArrayIndexOutOfBoundsException e) {
							}
						}
					vary = "";
				}
			} else if (val.equals("+")) {
				if (!operations.isEmpty()) {
					if (operations.peek().equals("*")) {
						operations.pop();
						numbers.push(numbers.pop() * num);
					} else if (operations.peek().equals("/")) {
						operations.pop();
						numbers.push(numbers.pop() / num);
					} else
						numbers.push(num);
				} else
					numbers.push(num);
				num = 0;
				operations.push("+");
			} else if (val.equals("-")) {
				if (!operations.isEmpty()) {
					if (operations.peek().equals("*")) {
						operations.pop();
						numbers.push(numbers.pop() * num);
					} else if (operations.peek().equals("/")) {
						operations.pop();
						numbers.push(numbers.pop() / num);
					} else
						numbers.push(num);
				} else
					numbers.push(num);
				num = 0;
				operations.push("-");
			} else if (val.equals("*")) {
				if (!operations.isEmpty()) {
					if (operations.peek().equals("*")) {
						operations.pop();
						numbers.push(numbers.pop() * num);
					} else if (operations.peek().equals("/")) {
						operations.pop();
						numbers.push(numbers.pop() / num);
					} else
						numbers.push(num);
				} else
					numbers.push(num);
				num = 0;
				operations.push("*");
			} else if (val.equals("/")) {
				if (!operations.isEmpty()) {
					if (operations.peek().equals("*")) {
						operations.pop();
						numbers.push(numbers.pop() * num);
					} else if (operations.peek().equals("/")) {
						operations.pop();
						numbers.push(numbers.pop() / num);
					} else
						numbers.push(num);
				} else
					numbers.push(num);
				num = 0;
				operations.push("/");
			} else if (val.equals(" ")) {
				continue;
			} else {
				vary += val;
				for (ScalarSymbol x : scalars)
					if (x.name.equals(vary)) {
						if (i != s.length() - 1)
							if (s.substring(i + 1, i + 2).equals("["))
								continue;
						num = x.value;
						vary = "";
					}
			}
		}
		float tot = 0;
		if (!(operations.isEmpty())) {
			String op = operations.pop();
			if (op.equals("+") || op.equals("-")) {
				operations.push(op);
				numbers.push(num);
			} else if (op.equals("*"))
				numbers.push(numbers.pop() * num);
			else if (op.equals("/"))
				numbers.push(numbers.pop() / num);
		}
		while (!operations.isEmpty()) {
			String op = operations.pop();
			if (op.equals("+"))
				tot += numbers.pop();
			else if (op.equals("-"))
				tot -= numbers.pop();
		}
		if (!numbers.isEmpty())
			tot += numbers.pop();
		else
			tot = num;
		return tot;

	}

	/**
	 * Utility method, prints the symbols in the scalars list
	 */
	public void printScalars() {
		for (ScalarSymbol ss : scalars) {
			System.out.println(ss);
		}
	}

	/**
	 * Utility method, prints the symbols in the arrays list
	 */
	public void printArrays() {
		for (ArraySymbol as : arrays) {
			System.out.println(as);
		}
	}

}
