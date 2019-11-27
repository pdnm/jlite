package backend;

import ir3.ast.Expr3;
import ir3.ast.Id3;
import ir3.ast.Stmt3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegisterAllocator {
    int numRegs;

    public RegisterAllocator(int numRegs) {
        this.numRegs = numRegs;
    }

    public Map<Id3, Integer> allocate(ir3.ast.CMtd3 method, ArrayList<Liveness.LivenessRes> liveness) {
        Map<Id3, Integer> vertexId = new HashMap<>();
        ArrayList<Id3> vars = method.allVars();
        ArrayList<List<Integer>> adjList = new ArrayList<>();

        for (int i = 0; i < vars.size(); i++) {
            vertexId.put(vars.get(i), i);
            adjList.add(new ArrayList<>());
        }

        // Build interference graph
        var stmts = method.body.stmts;
        for (int i = 0; i < stmts.size(); i++) {
            for (var x : liveness.get(i).kill) {
                for (var y : liveness.get(i).out) {
                    if (!x.equals(y) && !stmts.get(i).equals(new Stmt3.Asn(x, new Expr3.IdExpr(y)))) {
                        adjList.get(vertexId.get(x)).add(vertexId.get(y));
                        try {
                            adjList.get(vertexId.get(y)).add(vertexId.get(x));
                        } catch (NullPointerException e) {

                        }
                    }
                }
            }
        }

        int[] color = GraphColorer.color(adjList, numRegs);
        Map<Id3, Integer> colorMap = new HashMap<>();
        for (int u = 0; u < color.length; u++) if (color[u] != -1) {
            colorMap.put(vars.get(u), color[u]);
        }
        return colorMap;
    }
}
