/**
 * 自动机实验二
 * 编程实现上下文无关文法的变换，包括：消除ε-产生式、单产生式以及无用符号
 * 小组成员：戴鑫旺、张一达、赵先哲
 */
import java.util.*;

// 产生式类
class Production {
    private final Character left; // 左部
    private final String right; // 右部

    /**
     * 构造函数
     *
     * @param left  产生式的左部
     * @param right 产生式的右部
     */
    public Production(Character left, String right) {
        this.left = left;
        this.right = right;
    }

    public Character getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    @Override
    public String toString() {
        return left + "->" + right;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Production) {
            Production production = (Production) obj;
            return left.equals(production.left) && right.equals(production.right);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}

// 文法变换类
class GrammarTransformer    {
    private Set<Character> nonTerminalSet; // 非终结符集合
    private Set<Character> terminalSet; // 终结符集合
    private Set<Production> productionSet; // 产生式集合
    private Character startSymbol; // 开始符号
    private Set<Character> nullableSymbols; // 可空符号集合

    // 删除无用产生式
    public void removeUselessProductions(){
        // 遍历产生式，假如左部或右部存在不在终结符集合和非终结符集合中的符号，则删除该产生式
        Set<Production> newProductionSet = new HashSet<>();
        for (Production production : productionSet) {
            boolean flag = true;
            for (char symbol : production.getRight().toCharArray()) {
                if (!nonTerminalSet.contains(symbol) && !terminalSet.contains(symbol)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                if (nonTerminalSet.contains(production.getLeft())) {
                    newProductionSet.add(production);
                }
            }
        }
        productionSet = newProductionSet;
    }

    // 消除epsilon产生式
    public void removeEpsilonProductions(){
        /*
          找出能推出空串的非终结符集合N’：
          (1)N0=空集
          (2)N'={A|A->ε属于P}
          (3)如果N'≠N0，转向(4)，否则结束
          (4)N0=N'
          (5)N’=N0∪{A|如果A->α且α属于(ε∪N0)*}，转向(3)
         */
        Set<Character> N0 = new HashSet<>();
        Set<Character> N1 = new HashSet<>();
        for (Production production : productionSet) {
            if (production.getRight().equals("ε")) {
                N1.add(production.getLeft());
            }
        }
        while (!N1.equals(N0)) {
            N0 = N1;
            for (Production production : productionSet) {
                boolean flag = true;
                for (char symbol : production.getRight().toCharArray()) {
                    if (!N0.contains(symbol)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    N1.add(production.getLeft());
                }
            }
        }
        nullableSymbols = N1;
        if (nullableSymbols.size() == 0) {
            return;
        }
        Set<Production> newProductionSet = new HashSet<>();
        /*
          如果生成式A->β0C1β1C2...Cnβn，n≥0，且每个Ck(1≤k≤n)均在N‘内，而对于βj(0≤j≤n),没有βj在N’内
          则P‘应加入A->β0Y1β1Y2...Ynβn，其中Yk是Ck或是ε，但A->ε不应加入P’
         */
        for (Production production : productionSet) {
            if (production.getRight().equals("ε")) {
                continue;
            }
            StringBuilder right = new StringBuilder();
            for (char symbol : production.getRight().toCharArray()) {
                if (nullableSymbols.contains(symbol)) {
                    right.append(symbol);
                }
            }
            int length = right.length();
            if (length == 0) {
                continue;
            }
            // 根据length的长度，按照二进制的顺序生成所有可能的组合
            // 遍历右部的每个字符，记录是第几个可空字符，按照二进制是0还是1，如果是0则输出空串，如果是1则输出字符
            for (int i = 0; i < (1 << length); i++) {
                StringBuilder newRight = new StringBuilder();
                int index = 0;
                for (char symbol : production.getRight().toCharArray()) {
                    if (nullableSymbols.contains(symbol)) {
                        if (((i >> index) & 1) == 1) {
                            newRight.append(symbol);
                        }
                        index++;
                    } else {
                        newRight.append(symbol);
                    }
                }
                if (newRight.length() == 0) {
                    continue;
                }
                newProductionSet.add(new Production(production.getLeft(), newRight.toString()));
            }
        }
        // 如果S在N’内，则P’应加入Z->S|ε，Z是一个新的非终结符，N1=N∪{Z}，N是原来的非终结符集合;如果S不在N’内，则N1=N，Z=S,最后G’=(N1,Σ,P’,Z)
        if (nullableSymbols.contains(startSymbol)) {
            newProductionSet.add(new Production('Z', String.valueOf(startSymbol)));
            newProductionSet.add(new Production('Z', "ε"));
            startSymbol = 'Z';
            // 非终结符集合中加入Z
            nonTerminalSet.add('Z');
        }
        // 合并产生式并删除原来的epsilon产生式
        productionSet.addAll(newProductionSet);
        Set<Production> newProductionSet2 = new HashSet<>();
        for (Production production : productionSet) {
            // 除非左部是Z，否则不加右部是空串的产生式
            if (production.getRight().equals("ε") && production.getLeft() != 'Z') {
                continue;
            }
            newProductionSet2.add(production);
        }
        productionSet = newProductionSet2;
    }

    // 消除单一产生式
    public void removeUnitProductions(){
        /*
         1.对于每个非终结符A，构造一个非终结符集合NA={B|A=>*B}，其中=>*表示A能推出B，按以下三步：
           (1)N0={A}
           (2)N'={C|如果B->C属于P，且B属于N0}∪N0
           (3)如果N'≠N0，则N0=N'，转向(2)，否则NA=N‘，转向2.
         2.之后构造P1：如果B->α属于P且不是单生成式，则对于B属于NA中的所有A，加入A->α到P1中
         3.得出文法G1=(N1,T1，P1,S)
         */
        // 1.构造NA集合
        Map<Character, Set<Character>> NA = new HashMap<>();
        for (char nonTerminal : nonTerminalSet) {
            Set<Character> N0 = new HashSet<>();
            N0.add(nonTerminal);
            Set<Character> N1 = new HashSet<>(N0);
            while (true) {
                for (Production production : productionSet) {
                    if (N0.contains(production.getLeft())) {
                        // 假如产生式右部是单个非终结符，则加入N1
                        if (production.getRight().length() == 1 && Character.isUpperCase(production.getRight().charAt(0))) {
                            N1.add(production.getRight().charAt(0));
                        }
                    }
                }
                if (N1.equals(N0)) {
                    break;
                }
                N0 = N1;
                N1 = new HashSet<>();
                N1.add(nonTerminal);
            }
            // NA的含义为：对于每个非终结符A，NA={B|A=>*B}
            NA.put(nonTerminal, N0);
        }
        // 2.构造P1集合
        Set<Production> newProductionSet = new HashSet<>();
        for (Production production : productionSet) {
            if (production.getRight().length() == 1 && Character.isUpperCase(production.getRight().charAt(0))) {
                continue;
            }
            for (char nonTerminal : NA.keySet()) {
                // 如果非终结符可以推导出生成式左部，则加入新的生成式
                if (NA.get(nonTerminal).contains(production.getLeft())) {
                    newProductionSet.add(new Production(nonTerminal, production.getRight()));
                }
            }
        }
        productionSet = newProductionSet;
    }

    // 删除无用非终结符
    public void removeUselessNonTerminals(){
         /*
         删除不能推出终结符串的非终结符
         (1)N0=空集，N0为非终结符集合
         (2)N'={A|A->α且α属于T*}，N’为非终结符集合
         (3)如果N'≠N0，则转(4)，否则转(6)
         (4)N0=N'
         (5)N‘=N0∪{A|A->α且α属于(N0∪T)*}，转(3)
         (6)N1=N',N1为新的非终结符集合
          */
        Set<Character> N0 = new HashSet<>();
        while (true) {
            Set<Character> N1 = new HashSet<>(N0);
            for (Production production : productionSet) {
                boolean flag = true;
                for (char symbol : production.getRight().toCharArray()) {
                    if (!terminalSet.contains(symbol) && !N0.contains(symbol)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    N1.add(production.getLeft());
                }
            }
            if (N1.equals(N0)) {
                break;
            }
            N0 = N1;
        }
        nonTerminalSet = N0;
        // 删除产生式集合中的无用产生式
        removeUselessProductions();
    }

    // 消除无用符号
    public void removeUselessSymbols(){
        // 删除无用非终结符
        removeUselessNonTerminals();
        /*
         删除无用符号
         (1)N0={S}
         (2)N'={X|A属于N0且A->αXβ}∪N0，N’为有用符号集合，X属于N∪T
         (3)如果N'≠N0，则转(4)，否则转(5)
         (4)N0=N'转(2)
         (5)N1=N'∩N，T1=N‘∩T
         */
        Set<Character> N0 = new HashSet<>();
        N0.add(startSymbol);
        while (true) {
            Set<Character> N1 = new HashSet<>(N0);
            for (Production production : productionSet) {
                if (N0.contains(production.getLeft())) {
                    for (char symbol : production.getRight().toCharArray()) {
                        if (nonTerminalSet.contains(symbol) || terminalSet.contains(symbol)) {
                            N1.add(symbol);
                        }
                    }
                }
            }
            if (N1.equals(N0)) {
                break;
            }
            N0 = N1;
        }
        // 删除原终结符集合和非终结符集合中的无用符号
        Set<Character> newNonTerminalSet = new HashSet<>();
        Set<Character> newTerminalSet = new HashSet<>();
        for (char symbol : nonTerminalSet) {
            if (N0.contains(symbol)) {
                newNonTerminalSet.add(symbol);
            }
        }
        for (char symbol : terminalSet) {
            if (N0.contains(symbol)) {
                newTerminalSet.add(symbol);
            }
        }
        nonTerminalSet = newNonTerminalSet;
        terminalSet = newTerminalSet;
        // 删除产生式集合中的无用产生式
        removeUselessProductions();
    }

    // 根据产生式集合构造文法
    public GrammarTransformer(Set<Production> productionSet) {
        this.productionSet = productionSet;
        nonTerminalSet = new HashSet<>();
        terminalSet = new HashSet<>();
        nullableSymbols = new HashSet<>();
        // 开始符号默认为S
        startSymbol = 'S';
        // 大写符号为非终结符，其他符号为终结符
        for (Production production : productionSet) {
            nonTerminalSet.add(production.getLeft());
            for (char symbol : production.getRight().toCharArray()) {
                if (Character.isUpperCase(symbol)) {
                    nonTerminalSet.add(symbol);
                } else {
                    terminalSet.add(symbol);
                }
            }
        }
    }

    // 输出文法的非终结符、终结符、产生式集合、开始符号
    public void print() {
        System.out.println("非终结符：");
        System.out.println(nonTerminalSet);
        System.out.println("终结符：");
        // 不输出ε但不删除终结符集合中的ε
        Set<Character> newTerminalSet = new HashSet<>(terminalSet);
        newTerminalSet.remove('ε');
        System.out.println(newTerminalSet);
        // 输出开始符号
        System.out.println("开始符号：");
        System.out.println(startSymbol);
        // 按照左部的字典序对产生式集合排序
        // 输出产生式时，首先输出左部->右部，之后遍历后面的产生式，如果左部相同则输出|右部，否则输出换行符
        System.out.println("产生式：");
        List<Production> productionList = new ArrayList<>(productionSet);
        productionList.sort(Comparator.comparing(Production::getLeft));
        int index = 0;
        while (index < productionList.size()) {
            Production production = productionList.get(index);
            if (production.getLeft() == startSymbol) {
                System.out.print(production.getLeft() + "->" + production.getRight());
                index++;
                while (index < productionList.size() && productionList.get(index).getLeft() == startSymbol) {
                    System.out.print("|" + productionList.get(index).getRight());
                    index++;
                }
                System.out.println();
            } else {
                System.out.print(production.getLeft() + "->" + production.getRight());
                index++;
                while (index < productionList.size() && productionList.get(index).getLeft() == production.getLeft()) {
                    System.out.print("|" + productionList.get(index).getRight());
                    index++;
                }
                System.out.println();
            }
        }
        System.out.println();
    }
}

public class Main {
    public static void main(String[] args) {
        // 输出提示信息
        System.out.println("请输入文法的产生式：\n1.每个产生式占一行，产生式的左部和右部用 -> 分隔\n2.产生式的右部可以用多个“|”分隔，产生式的左部是单个非终结符\n3.非终结符用大写字母表示，终结符用大写字母以外的字符表示\n4.空串用 ε 表示，输入以“#”结束\n输入：");
        // 根据输入的字符串构建产生式集合
        Set<Production> productionSet = new HashSet<>();
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        while (!input.equals("#")) {
            String[] split = input.split("->");
            String left = split[0].trim();
            String[] right = split[1].trim().split("\\|");
            for (String s : right) {
                productionSet.add(new Production(left.charAt(0), s.trim()));
            }
            input = scanner.nextLine();
        }
        // 构建文法
        GrammarTransformer grammarTransformer = new GrammarTransformer(productionSet);
        // 输出文法
        grammarTransformer.print();
        // 消除epsilon产生式
        grammarTransformer.removeEpsilonProductions();
        // 输出消除epsilon产生式后的文法
        System.out.println("消除epsilon产生式后的文法：");
        grammarTransformer.print();
        // 消除单一产生式
        grammarTransformer.removeUnitProductions();
        // 输出消除单一产生式后的文法
        System.out.println("消除单一产生式后的文法：");
        grammarTransformer.print();
        // 消除无用符号
        grammarTransformer.removeUselessSymbols();
        // 输出消除无用符号后的文法
        System.out.println("消除无用符号后的文法：");
        grammarTransformer.print();
    }
}