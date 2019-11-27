package backend;

import ir3.ast.Id3;
import ir3.ast.Stmt3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Liveness {
    public static class LivenessRes {
        Set<Id3> gen;
        Set<Id3> kill;
        Set<Id3> in;
        Set<Id3> out;

        public LivenessRes(Set<Id3> gen, Set<Id3> kill) {
            this.gen = gen;
            this.kill = kill;
            this.in = new HashSet<>();
            this.out = new HashSet<>();
        }

        public void setIn(Set<Id3> in) {
            this.in = in;
        }

        public void setOut(Set<Id3> out) {
            this.out = out;
        }
    }

    public static ArrayList<LivenessRes> analyze(ArrayList<Stmt3> stmts) {
        ArrayList<LivenessRes> result = new ArrayList<>(stmts.size());
        for (var st : stmts) {
            Set<Id3> gen = st.process(
                    label -> Set.of(),
                    anIf -> Set.of(anIf.cond),
                    aGoto -> Set.of(),
                    asn -> Set.copyOf(asn.rvalue.vars()),
                    fdAsn -> Set.copyOf(fdAsn.rvalue.vars()),
                    fnCall -> Set.copyOf(fnCall.args),
                    aReturn -> Set.of(aReturn.value)
            );
            Set<Id3> kill = st.process(
                    label -> Set.of(),
                    anIf -> Set.of(),
                    aGoto -> Set.of(),
                    asn -> Set.of(asn.lvalue),
                    fdAsn -> Set.of(fdAsn.obj),
                    fnCall -> Set.of(),
                    aReturn -> Set.of()
            );
            result.add(new LivenessRes(gen, kill));
        }
        var succ = FlowGraph.buildFlowGraph(stmts);

        while (true) {
            var fixed = true;
            for (int i = stmts.size() - 1; i >= 0; i--) {
                var newOut = succ.get(i).stream()
                        .flatMap(j -> result.get(j).in.stream())
                        .collect(Collectors.toSet());
                if (!newOut.equals(result.get(i).out)) {
                    result.get(i).setOut(newOut);
                    fixed = false;
                }

                var newIn = new HashSet<>(result.get(i).gen);
                var tmp = new HashSet<>(newOut);
                tmp.removeAll(result.get(i).kill);
                newIn.addAll(tmp);
                if (!newIn.equals(result.get(i).in)) {
                    result.get(i).setIn(newIn);
                    fixed = false;
                }
            }
            if (fixed) break;
        }
        return result;
    }
}
