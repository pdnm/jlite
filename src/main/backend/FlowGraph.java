package backend;

import ir3.ast.Stmt3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlowGraph {
    public static Map<Integer, Integer> getLabelPositionTable(ArrayList<Stmt3> stmts) {
        Map<Integer, Integer> labelPos = new HashMap<>();
        for (int i = 0; i < stmts.size(); i++) {
            var st = stmts.get(i);
            if (st instanceof Stmt3.Label) {
                labelPos.put(((Stmt3.Label) st).id, i);
            }
        }
        return labelPos;
    }

    public static ArrayList<List<Integer>> buildFlowGraph(ArrayList<Stmt3> stmts) {
        ArrayList<List<Integer>> result = new ArrayList<>(stmts.size());
        var labelPos = getLabelPositionTable(stmts);

        for (int i = 0; i < stmts.size(); i++) {
            var index = i;
            result.add(stmts.get(i).process(
                    label -> List.of(index + 1),
                    anIf -> List.of(index + 1, labelPos.get(anIf.target.id)),
                    aGoto -> List.of(aGoto.target.id),
                    asn -> List.of(index + 1),
                    fdAsn -> List.of(index + 1),
                    fnCall -> List.of(index + 1),
                    aReturn -> List.<Integer>of()
            ).stream().filter(x -> x < stmts.size()).collect(Collectors.toList()));
        }
        return result;
    }

    public static ArrayList<List<Integer>> getPredecessorLists(ArrayList<List<Integer>> succ) {
        ArrayList<List<Integer>> predec = new ArrayList<>(succ.size());
        for (int i = 0; i < succ.size(); i++)
            predec.add(new ArrayList<>());
        for (int i = 0; i < succ.size(); i++) {
            for (var j : succ.get(i))
                predec.get(j).add(i);
        }
        return predec;
    }
}
