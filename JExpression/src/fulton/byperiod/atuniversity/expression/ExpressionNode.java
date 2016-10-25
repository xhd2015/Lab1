package fulton.byperiod.atuniversity.expression;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionNode {

	public String val = "";
	public String coeff = "0";
	public String sign = "+";
	public boolean isItem = false;
	public ArrayList<ExpressionNode> siblings = null;
	public static double zeroRange = 0.0000000001;

	// @val default is ''
	// @coeff default is '0'
	// @sign default is '+'
	// @isTtem default is false , isItem标识一个项是否是单项
	// 约束条件:当isItem=false的时候,该coeff,sign为默认值;isItem=true时必须为有效值
	public ExpressionNode(String val, String coeff, String sign, boolean isItem) {
		this.val = val;
		this.coeff = coeff;
		this.sign = sign;
		this.isItem = isItem;
		this.siblings = new ArrayList<>();
	}

	// see python code
	public void parseAsList(ArrayList<ArrayList<String>> exlist, String prefix) {
		if (isItem) {
			ArrayList<String> toadd = new ArrayList<String>();
			toadd.add(this.sign);
			toadd.add(this.coeff);
			toadd.add(prefix + this.val);
			exlist.add(toadd);
		}
		if (!siblings.isEmpty()) {
			for (Object o : this.siblings) {
				if (!val.isEmpty())
					((ExpressionNode) o).parseAsList(exlist, prefix + this.val
							+ " * ");
				else
					((ExpressionNode) o).parseAsList(exlist, prefix);
			}
		}
	}

	// 执行的条件是val相等,相当于对nfrom的变量赋值1时的结果,当nfrom=false,时,ndst不更新
	// 将n1的参数转移到n2上,之后n2一定为非单项
	public ExpressionNode transmitionNodeTo(ExpressionNode ndst) {
		ExpressionNode nfrom = this;
		// 非item项,默认值sign="+",coeff="0"
		if (nfrom.isItem) {
			ndst.isItem = true;
			nfrom.isItem = false;

			double coeff = nfrom.getValueAsIsItem() + ndst.getValueAsIsItem();
			ndst.sign = "+";
			if (coeff < 0) {
				coeff = -coeff;
				ndst.sign = "-";
			}
			ndst.coeff = String.valueOf(coeff);
			nfrom.sign = "+";
			nfrom.coeff = "0";
		}
		return ndst;

	}

	// [ completed ][ important!!! ][ constraints ] n is not CONST NODE
	public void addNode(ExpressionNode n) {
		// 如果n是常数项,就直接使用相乘
		if (n.isConst()) {
			if (isItem)
				enlargeCoefficientAsIsItem(n.getValueAsIsItem());
			else {
				setCoefficient(n.getValueAsIsItem());
				isItem = true;
			}
		} else if (n.isItem && Math.abs(n.getValueAsIsItem()) <= zeroRange) {
			// do nothing
		} else {
			int i = findIndex(n);
			// 没有找到,需要插入
			if (i == siblings.size() || !siblings.get(i).val.equals(n.val)) {
				siblings.add(i, n);
			} else { // 找到同类项
				n.transmitionNodeTo(siblings.get(i));
				// 如果合并和为0
				if (siblings.get(i).isItem && siblings.get(i).isZero()) {
					siblings.get(i).isItem = false;
					siblings.get(i).sign = "+";
					siblings.get(i).coeff = "0";
					if (n.siblings.isEmpty())
						siblings.remove(i);
				}
				for (ExpressionNode e : n.siblings) {
					siblings.get(i).addNode(e);
				}
			}
		}
	}

	// 删除某一层的节点,但是保持其他节点
	public void deleteNode(int i) {
		siblings.get(i).transmitionNodeTo(this);
		ExpressionNode dE = siblings.remove(i);
		for (ExpressionNode n : dE.siblings) {
			addNode(n);
		}
	}

	public static ExpressionNode makeConst(double dvalue) {
		ExpressionNode e = new ExpressionNode("*1", "", "", true);
		e.setCoefficient(dvalue);
		return e;
	}

	public boolean isConst() {
		return isItem && val.equals("*1");
	}

	public ExpressionNode copyOf() {
		ExpressionNode en = new ExpressionNode(val, coeff, sign, isItem);
		for (ExpressionNode item : siblings) {
			en.addNode(item.copyOf());
		}
		return en;
	}

	public boolean isZero() {
		return Math.abs(getValueAsIsItem()) <= zeroRange;
	}

	// 要求dvalue不能是0值;0如果是0值,可以直接使用删除
	public void multiplyWith_ex(double dvalue) {
		if (isItem) {
			enlargeCoefficientAsIsItem(dvalue);
		}
		for (int i = 0; i < siblings.size(); i++) {
			siblings.get(i).multiplyWith_ex(dvalue);
		}

	}

	/*
	 * 合法的表达式树 1.头节点有效 2.不含常数项(用"*1"表示) 3.头节点可以是常数项,当且仅当头节点
	 * 仅有head=""会保留一个常数项,其他都不保留,这是可以保证的
	 */
	// @return 常数项,变项的表达式组
	public ArrayList<ExpressionNode> eval_ex(HashMap<String, Double> table) {
		ArrayList<ExpressionNode> nodesToAdd = new ArrayList<>();
		for (ExpressionNode e : siblings) {
			nodesToAdd.addAll(e.eval_ex(table));// e仍旧是合法的表达式树
		}
		siblings.clear();

		ArrayList<ExpressionNode> rtn = null;
		if (table.containsKey(val))// 此变量是赋值目标
		{
			double dvalue = table.get(val);
			if (Math.abs(dvalue) <= zeroRange) {
				nodesToAdd.clear();// 其他项都是0
				nodesToAdd.add(makeConst(0));
			} else {
				for (ExpressionNode e : nodesToAdd) {
					e.multiplyWith_ex(dvalue);
				}
				if (isItem)
					nodesToAdd.add(makeConst(dvalue * getValueAsIsItem()));
			}
			rtn = nodesToAdd;
		} else {// 此变量不是赋值目标
			for (ExpressionNode e : nodesToAdd) {
				addNode(e);
			}
			rtn = new ArrayList<>();
			rtn.add(this);
		}
		return rtn;
	}

	// 改变系数
	public void enlargeCoefficientAsIsItem(double dvalue) {
		setCoefficient(Double.parseDouble(sign + coeff) * dvalue);
	}

	public void setCoefficient(double dvalue) {
		sign = "+";
		if (Math.abs(dvalue) <= zeroRange) {
			dvalue = 0.0;
		} else if (dvalue < 0) {
			sign = "-";
			dvalue = -dvalue;
		}
		coeff = String.valueOf(dvalue);
	}

	public double getValueAsIsItem() {
		return Double.parseDouble(sign + coeff);
	}

	// 根据求导公式dab/dx=a*(db/dx)+(da/dx)*b
	public ArrayList<ExpressionNode> deriveon_ex(String variable) {

		ArrayList<ExpressionNode> nodesFromB = new ArrayList<>();
		if (siblings.size() == 0) {
			if (val.equals(variable))
				nodesFromB.add(makeConst(getValueAsIsItem()));
			else
				nodesFromB.add(makeConst(0));

			return nodesFromB;
		}
		ArrayList<ExpressionNode> nodesAddToA = new ArrayList<>();
		if (!val.equals(variable)) {
			nodesFromB.add(makeConst(0));// 添加0常数项
		} else {
			for (ExpressionNode e : siblings) {
				nodesFromB.add(e.copyOf());
			}
		}
		for (ExpressionNode e : siblings) {
			nodesAddToA.addAll(e.deriveon_ex(variable));// e仍旧是合法的表达式树
		}
		siblings.clear();

		for (ExpressionNode en : nodesAddToA) {
			addNode(en);
		}
		nodesFromB.add(this);
		return nodesFromB;
	}

	// [ tested ]
	public int findIndex(ExpressionNode n) {
		for (int i = 0; i != siblings.size(); i++) {
			if (siblings.get(i).val.compareTo(n.val) >= 0)
				return i;
		}
		return siblings.size();
	}

	// level default is 0
	public String dumpTree(int level) {
		StringBuilder s = new StringBuilder("");
		for (int i = 0; i != level; i++)
			s.append(" ");
		s.append("{sign : ").append(sign).append(" , coeff : ").append(coeff)
				.append(" , val : ").append(val);
		s.append(" , isItem : ").append(isItem).append("}\n");
		for (int i = 0; i != siblings.size(); i++) {
			s.append(siblings.get(i).dumpTree(level + 1));
		}
		return s.toString();
	}

	// [ completed ]
	public static ExpressionNode standardlizeExpression(String s)
			throws ExpressionException {
		// 去除空白字符 [tested]
		s = s.replaceAll(" ", "");
		s = s.replaceAll("\t", "");
		s = s.replaceAll("\n", "");

		// 检测是否乘号连乘
		if (s.indexOf("**") >= 0) {
			throw new ExpressionException(
					"[ error 1 ] two or more continous \"*\" found.");
		}

		String[] items = s.split("\\*");
		if (items == null) {
			throw new ExpressionException(
					"[ error 2 ] s has no other poly items.");
		}
		Double coeff = 1.0;
		ArrayList<String> additions = new ArrayList<>();
		for (int i = 0; i != items.length; i++) {
			// 允许小数作为系数
			Pattern itemp = Pattern
					.compile("([+-]?(?:\\d*|\\d*\\.\\d*))(?:([A-Za-z]+\\^\\d+)|([A-Za-z]+))?");// 2x*x^2
			Matcher itemm = itemp.matcher(items[i]);
			if (!itemm.matches()) {
				throw new ExpressionException(
						"[ error 3 ] s has invalid poly item.");
			} else {
				String g1 = itemm.group(1);// 系数
				String g2 = itemm.group(3);// 单项式符号
				String g3 = itemm.group(2);// 获得指数的单项,将其拆分为多个相同项之乘积
				if (g3 != null)// 是指数形式
				{
					int index = g3.indexOf("^");
					String sym = g3.substring(0, index);
					int nums = Integer.parseInt(g3.substring(index + 1));
					for (int i1 = 0; i1 != nums - 1; i1++) {
						additions.add(sym);
					}
					items[i] = sym;
				} else if (g2 != null)// 单项式形式
				{
					items[i] = g2;
				} else if (g1 != null && !g1.isEmpty())// 仅含数字的形式
				{
					items[i] = "";// 标记未空项
				} else // 否则是非法的表达式,比如2
				{
					throw new ExpressionException(
							"[ error 6 ] expression contains invalid symbols.");
				}
				// <note>只有+-号时添加系数
				if (g1 == null || g1.isEmpty()) {
					g1 = "+1";
				} else if (g1.equals("+") || g1.equals("-")) {
					g1 += "1";
				}
				// </note>
				try {
					coeff *= Double.parseDouble(g1);
				} catch (Exception e) {
					throw new ExpressionException(
							"[ error 4 ] invalid expression of coefficient.");
				}
			}
		}

		// 连接两个列表
		for (String item : items) {
			if (item.isEmpty())
				continue;// 空项是标记的项
			additions.add(item);
		}
		// 排序各个项
		additions.sort(new Comparator<String>() {

		@Override
		public int compare(String arg0, String arg1) {
			// TODO Auto-generated method stub
				return arg0.compareTo(arg1);
			}
		});
		ExpressionNode lastNode = null, head = null;
		if (additions.size() > 0)
			lastNode = new ExpressionNode(additions.get(0), "0", "+", false);
		else
			lastNode = new ExpressionNode("*1", "0", "+", false);// 使用_代表常数项
		head = lastNode;
		for (int i = 1; i < additions.size(); i++) {
			lastNode.siblings.add(new ExpressionNode(additions.get(i), "0",
					"+", false));
			lastNode = lastNode.siblings.get(0);
		}
		lastNode.sign = "+";
		if (coeff < 0) {
			coeff = -coeff;
			lastNode.sign = "-";
		}
		lastNode.coeff = String.valueOf(coeff);
		lastNode.isItem = true;

		return head;
	}

	/* 返回命令的类型和参数 */
	// @return type,*args
	public static ArrayList<String> parseCommand(String command)
			throws ExpressionException {
		HashMap<String, String> comRules = new HashMap<>();
		comRules.put("!d/d", "^\\!d\\/d\\s+([A-Za-z]+)$");
		comRules.put("!simplify",
				"^\\!simplify(?:\\s+([A-Za-z]+)\\=([+-]?(?:\\d+\\.\\d*|\\d+)))*\\s*$");
		ArrayList<String> comargs = new ArrayList<String>();
		for (Entry<String, String> o : comRules.entrySet()) {
			Matcher patm = Pattern.compile(o.getValue()).matcher(command);
			if (patm.matches()) {

				if (o.getKey().equals("!d/d")) {
					comargs.add("derive");
					comargs.add(patm.group(1));
				} else if (o.getKey().equals("!simplify")) {
					comargs.add("simplify");
					Matcher m2 = Pattern.compile(
							"([A-Za-z]+)\\=([+-]?(?:\\d+\\.\\d*|\\d+))")
							.matcher(command);
					while (m2.find()) {
						comargs.add(m2.group(1));
						comargs.add(m2.group(2));
					}
				}
				return comargs;
			}

		}
		throw new ExpressionException("[ error ]command has wrong format.");

	}

	public static ArrayList<String> splitToSingleExpressions(String multiEx)
			throws ExpressionException {

		// 检查首部是否以!开头,如果以!开头,去掉命令部分,获得整体表达式
		if (multiEx.startsWith("!")) {
			throw new ExpressionException(
					"[ error ] expression expected , command may be given.");
		}

		// 先检查数字之间不能有空格
		String noSpaceBetweenDD = "\\d\\s+\\d";
		if (Pattern.compile(noSpaceBetweenDD).matcher(multiEx).find())
			throw new ExpressionException(
					"[ error ] spaces appeared between numbers.");

		// 去除空白字符
		multiEx = multiEx.replaceAll(" ", "");
		multiEx = multiEx.replaceAll("\t", "");
		multiEx = multiEx.replaceAll("\n", "");

		// 检查字符集,不能包含!,/等等
		String invalidCharset = "[^A-Za-z0-9\\*\\-\\+\\^]";
		Pattern p1 = Pattern.compile(invalidCharset);
		Matcher p1m = p1.matcher(multiEx);
		if (p1m.find()) {
			throw new ExpressionException(
					"[ error ] expression contains invalid chars.");
		}

		// 检查不能以[+-]连续3次出现
		String notContinuousPlus = "[+-][+-][+-]";
		Pattern p2 = Pattern.compile(notContinuousPlus);
		if (p2.matcher(multiEx).find())
			throw new ExpressionException(
					"[ error ] expression contains continues \"+/-\".");

		// 检查两个乘号不能连续出现
		String notContinuousMultiply = "**";
		if (multiEx.contains(notContinuousMultiply))
			throw new ExpressionException(
					"[ error ] expression contains continues \"*\".");

		// 检查*之后不能跟两个+-符号
		String notFollowPlus = "\\*[+-][+-]";
		if (Pattern.compile(notFollowPlus).matcher(multiEx).find())
			throw new ExpressionException(
					"[ error ] expression has \"*\" followd by continoues \"+/-\".");

		// 检查字母后面不能跟数字
		String notAprecedD = "[A-Za-z]\\d";
		if (Pattern.compile(notAprecedD).matcher(multiEx).find())
			throw new ExpressionException(
					"[ error ] expression has LETTER followed by NUMBER.");

		// 检查^的后面不能出现非数字
		String notPowerVary = "\\^\\D";
		if (Pattern.compile(notPowerVary).matcher(multiEx).find())
			throw new ExpressionException(
					"[ error ] expression has un-fixed exponent.");

		// 开头和不能是*,++,--,-+,+- ; 结尾不能是*,-,+
		String startConstrain = "(?:^(?:\\*|[+-][+-])|(?:\\*|[+-])$)";
		if (Pattern.compile(startConstrain).matcher(multiEx).find())
			throw new ExpressionException(
					"[ error ] expression starts or ends with wrong symbols.");

		// 两个++,+-,-+,--替换成+,-,-,+
		multiEx = multiEx.replaceAll("\\+\\+|\\-\\-", "+");
		multiEx = multiEx.replaceAll("\\+\\-|\\+\\-", "-");

		// 开始分割单个的项
		ArrayList<String> exList = new ArrayList<>();
		String singleItem = "([+-]?(?:\\d*|\\d*\\.\\d*))(?:([A-Za-z]*\\^\\d+)|([A-Za-z]*))?";
		String multiItemP = singleItem + "(?:\\*" + singleItem + ")*";
		Pattern sp = Pattern.compile(multiItemP);
		Matcher spm = sp.matcher(multiEx);
		while (spm.find()) {
			String found = spm.group();
			if (found.isEmpty())
				continue;
			exList.add(found);
		}
		return exList;
	}

	public static void print(Object... o) {
		for (Object i : o) {
			System.out.print(i);
			System.out.print(" ");
		}
		System.out.println();
	}

	/**
	 * @param args
	 * @throws ExpressionException
	 */
	public static void main(String[] args) throws ExpressionException {
		// TODO Auto-generated method stub
		/*
		 * //-- [START] String testEx="45+6-jk+2*2*-0+5+-0*98";//*一项[+-]
		 * ArrayList<String> exlist=splitToSingleExpressions(testEx);
		 * 
		 * print(exlist.toString());
		 * 
		 * System.exit(0); //--[ END ]
		 */

		/*
		 * //--*--*--*--test some cases : standardlizeExpression worked [START]
		 * HashMap<String,String> testcases=new HashMap<>();
		 * testcases.put("+6*u","1"); testcases.put("5.3x^9*-7","1");
		 * testcases.put("5**x","0");//两个乘号应当失败
		 * testcases.put("5*x5","0");//变量后面出现数字,应当失败
		 * testcases.put("x^5y","0");//连续的变量应当使用*分割,应当失败 for(Entry<String,
		 * String> st:testcases.entrySet()) { try { print("=========");
		 * print(st.getKey()); ExpressionNode
		 * e=standardlizeExpression(st.getKey());
		 * 
		 * print(e.dumpTree(0));
		 * 
		 * } catch (ExpressionException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); print(e1.getMessage()); } }
		 * print("[ test ended ]"); //--*--*--*--test some cases [END]
		 */

		// --*--*--*-- [START]
		String s = "5.6x*y*z", s2 = "7.8x*y*t";
		ExpressionNode head = new ExpressionNode("", "0", "+", false);

		ExpressionNode e = standardlizeExpression(s);
		print(e.dumpTree(0));
		head.addNode(e);
		print(head.dumpTree(0));

		ExpressionNode e2 = standardlizeExpression(s2);
		print(e2.dumpTree(0));
		head.addNode(e2);
		print(head.dumpTree(0));

		head.addNode(standardlizeExpression("2.4x*y*z"));
		head.addNode(standardlizeExpression("-3*x*y"));
		print(head.dumpTree(0));

		ArrayList<ArrayList<String>> list = new ArrayList<>();
		head.parseAsList(list, "");
		print(list.toString());

		print(head.dumpTree(0));
		list.clear();
		head.parseAsList(list, "");
		print(list);

		print(head.dumpTree(0));
		list.clear();
		head.parseAsList(list, "");
		print(list);

		head.addNode(standardlizeExpression("9*t*z*y"));

		head.addNode(standardlizeExpression("+6*t*x*y"));

		head.addNode(standardlizeExpression("-8*t*x*x"));

		print(head.dumpTree(0));
		list.clear();
		head.parseAsList(list, "");
		print(list);

		print(head.dumpTree(0));
		list.clear();
		head.parseAsList(list, "");
		print(list);

		head.addNode(standardlizeExpression("t*x*y*z*x"));
		print(head.dumpTree(0));
		list.clear();
		head.parseAsList(list, "");
		print(list);

		/*
		 * head.deleteNode(0); print(head.dumpTree(0)); list.clear();
		 * head.parseAsList(list, ""); print(list);
		 * 
		 * head.muiltplyWith(0, 5); print(head.dumpTree(0)); list.clear();
		 * head.parseAsList(list, ""); print(list);
		 */
		// --*--*--*-- [END]
	}

}

