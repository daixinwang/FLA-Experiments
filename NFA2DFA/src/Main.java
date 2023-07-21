//实现从NFA到DFA的转换

import java.util.*;

//DFA转移函数类
class DFATransition {
    public ArrayList<Integer> from; //转移前状态
    public ArrayList<Integer> to; //转移后状态
    public String ch; //转移字符

    public DFATransition(ArrayList<Integer> from, String ch, ArrayList<Integer> to) {
        this.from = from;
        this.to = to;
        this.ch = ch;
    }
}

//DFA类
class DFA {

    public ArrayList<Integer> start; //起始状态
    public ArrayList<ArrayList<Integer>> end; //终止状态
    public ArrayList<DFATransition> transitions; //转移函数
    public ArrayList<ArrayList<Integer>> states; //状态集合
    public ArrayList<String> alphabet; //字母表

    //构造函数
    public DFA() {
        this.start = new ArrayList<>();
        this.end = new ArrayList<>();
        this.transitions = new ArrayList<>();
        this.states = new ArrayList<>();
        this.alphabet = new ArrayList<>();
    }

    //打印DFA,包括状态集合、字母表、起始状态、终止状态、转移函数
    public void printDFA() {
        //输出DFA的各个部分
        System.out.println("状态集合：");
        for (ArrayList<Integer> arrayList : this.states) {
            for (Integer integer : arrayList) {
                System.out.print(integer + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("字母表：");
        for (String string : this.alphabet) {
            System.out.print(string + " ");
        }
        System.out.println();
        System.out.println();
        System.out.println("起始状态：");
        for (Integer integer : this.start) {
            System.out.print(integer + " ");
        }
        System.out.println();
        System.out.println();
        System.out.println("终止状态：");
        for (ArrayList<Integer> arrayList : this.end) {
            for (Integer integer : arrayList) {
                System.out.print(integer + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("转移函数：");
        for (DFATransition transition : this.transitions) {
            System.out.println(transition.from + " " + transition.ch + " " + transition.to);
        }
    }

    //打印DFA，要求将状态集合中的状态用大写字母A-Z表示
    //打印状态集合、字母表、起始状态、终止状态、转移函数
    //打印时将状态集合、起始状态、终止状态和转移函数中的状态用大写字母A-Z表示
    public void printDFA2() {
        //输出DFA的各个部分
        System.out.println("状态集合：");
        for (int i = 0; i < this.states.size(); i++) {
            System.out.print((char)('A' + i) + " = ");
            for (Integer integer : this.states.get(i)) {
                System.out.print(integer + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("字母表：");
        for (String string : this.alphabet) {
            System.out.print(string + " ");
        }
        System.out.println();
        System.out.println();
        System.out.println("起始状态：");
        ArrayList<Integer> startArrayList = new ArrayList<>(this.start);
        System.out.print((char)('A' + this.states.indexOf(startArrayList)) + " ");
        System.out.println();
        System.out.println();
        System.out.println("终止状态：");
        for (ArrayList<Integer> arrayList : this.end) {
            ArrayList<Integer> endArrayList = new ArrayList<>(arrayList);
            System.out.print((char)('A' + this.states.indexOf(endArrayList)) + " ");
            System.out.println();
        }
        System.out.println();
        System.out.println("转移函数：");
        for (DFATransition transition : this.transitions) {
            System.out.println((char)('A' + this.states.indexOf(transition.from)) + " " + transition.ch + " " + (char)('A' + this.states.indexOf(transition.to)));
        }
    }

}

//NFA转移函数类
class NFATransition {
    public int from; //转移前状态
    public int to; //转移后状态
    public String ch; //转移字符

    public NFATransition(int from, String ch, int to) {
        this.from = from;
        this.to = to;
        this.ch = ch;
    }
}

//NFA类
class NFA{
    public int start; //起始状态
    public ArrayList<Integer> end; //终止状态
    public ArrayList<NFATransition> transitions; //转移函数
    public ArrayList<Integer> states; //状态集合
    public ArrayList<String> alphabet; //字母表

    //构造函数
    public NFA() {
        this.start = 0;
        this.end = new ArrayList<>();
        this.transitions = new ArrayList<>();
        this.states = new ArrayList<>();
        this.alphabet = new ArrayList<>();
    }

    //根据输入构建NFA
    public NFA buildNFA(Scanner sc){
        NFA nfa = new NFA();
        //首先输入起始状态和终止状态，具有用户交互性
        System.out.println("请输入起始状态：");
        nfa.start = sc.nextInt();
        System.out.println("请输入终止状态个数：");
        int endNum = sc.nextInt();
        for (int i = 0; i < endNum; i++) {
            System.out.println("请输入第" + (i + 1) + "个终止状态：");
            nfa.end.add(sc.nextInt());
        }
        //输入转移函数，具有用户交互性，并在添加转移函数的同时添加状态和字母表
        System.out.println("请输入转移函数个数：");
        int transitionNum = sc.nextInt();
        for (int i = 0; i < transitionNum; i++) {
            System.out.println("请输入第" + (i + 1) + "个转移函数：");
            int from = sc.nextInt();
            String ch = sc.next();
            int to = sc.nextInt();
            nfa.transitions.add(new NFATransition(from, ch, to));
            if (!nfa.states.contains(from)) {
                nfa.states.add(from);
            }
            if (!nfa.states.contains(to)) {
                nfa.states.add(to);
            }
            if (!nfa.alphabet.contains(ch)) {
                nfa.alphabet.add(ch);
            }
        }
        return nfa;
    }

    //求出某个状态的ε闭包
    public ArrayList<Integer> epsilonClosure(int state) {
        ArrayList<Integer> closure = new ArrayList<>();
        closure.add(state);
        for (NFATransition transition : this.transitions) {
            if (transition.from == state && transition.ch.equals("&")) {
                if (transition.from == transition.to) {
                    continue;
                }
                closure.addAll(epsilonClosure(transition.to));
            }
        }
        return closure;
    }

    //求出某个状态集合中所有状态经由某个字符转移后的状态集合
    public ArrayList<Integer> move(ArrayList<Integer> states, String ch) {
        ArrayList<Integer> move = new ArrayList<>();
        for (Integer state : states) {
            for (NFATransition transition : this.transitions) {
                if (transition.from == state && transition.ch.equals(ch)) {
                    move.add(transition.to);
                }
            }
        }
        return move;
    }

    //求出某个状态集合经由某个字符转移后的ε闭包
    public ArrayList<Integer> moveEpsilonClosure(ArrayList<Integer> states, String ch) {
        ArrayList<Integer> move = move(states, ch);
        ArrayList<Integer> moveEpsilonClosure = new ArrayList<>();
        for (Integer state : move) {
            moveEpsilonClosure.addAll(epsilonClosure(state));
        }
        //删除重复元素并排序
        HashSet<Integer> set = new HashSet<>(moveEpsilonClosure);
        moveEpsilonClosure.clear();
        moveEpsilonClosure.addAll(set);
        Collections.sort(moveEpsilonClosure);
        return moveEpsilonClosure;
    }

    //由NFA构造DFA
    public DFA buildDFA() {
        DFA dfa = new DFA();
        //首先求出NFA的起始状态的ε闭包，作为DFA的起始状态
        ArrayList<Integer> startClosure = epsilonClosure(this.start);
        dfa.start = startClosure;
        //将DFA的起始状态加入到DFA的状态集合中
        dfa.states.add(startClosure);
        //将NFA的字母表作为DFA的字母表
        dfa.alphabet = this.alphabet;
        //对于每个状态集合，求出经由每个字符转移后的ε闭包，作为DFA的转移函数,并将转移函数添加到DFA的转移函数集合中,并将转移函数的终点加入到DFA的状态集合
        int dfaStateFinish = 0;
        while (true){
            //假如存在未遍历的状态集合，则继续遍历
            //假如不存在未遍历的状态集合，则跳出循环
            int dfaStateNumBefore = dfa.states.size();
            for (int i = dfaStateFinish; i < dfaStateNumBefore; i++) {
                for (String ch : dfa.alphabet) {
                    if (Objects.equals(ch, "&")) {
                        continue;
                    }
                    ArrayList<Integer> moveEpsilonClosure = moveEpsilonClosure(dfa.states.get(i), ch);
                    if(!moveEpsilonClosure.isEmpty()){
                        if (!dfa.transitions.contains(new DFATransition(dfa.states.get(i), ch, moveEpsilonClosure))) {
                            dfa.transitions.add(new DFATransition(dfa.states.get(i), ch, moveEpsilonClosure));
                            if (!dfa.states.contains(moveEpsilonClosure)) {
                                dfa.states.add(moveEpsilonClosure);
                            }
                        }
                    }

                }
            }
            dfaStateFinish = dfaStateNumBefore;
            int dfaStateNumAfter = dfa.states.size();
            if (dfaStateNumBefore == dfaStateNumAfter) {
                break;
            }
        }
        //求出DFA的终止状态集合
        //对于每个状态集合，如果该状态集合中包含NFA的终止状态，则将该状态集合作为DFA的终止状态集合
        for (ArrayList<Integer> state : dfa.states) {
            for (Integer end : this.end) {
                if (state.contains(end)) {
                    dfa.end.add(state);
                    break;
                }
            }
        }
        return dfa;
    }
}

//NFA2DFA类
class NFA2DFA{
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        NFA nfa = new NFA();
        nfa = nfa.buildNFA(sc);
        DFA dfa = nfa.buildDFA();
        dfa.printDFA2();
    }
}