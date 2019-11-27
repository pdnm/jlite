package backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class GraphColorer {
    public static int[] color(ArrayList<List<Integer>> adj, int nColors) {
        int n = adj.size();
        Stack<Integer> stack = new Stack<>();
        boolean[] marked = new boolean[n];
        int[] deg = new int[n];
        Arrays.fill(marked, false);
        Arrays.fill(deg, 0);
        for (int u = 0; u < n; u++)
            deg[u] = adj.get(u).size();

        while (stack.size() < n) {
            int min_deg = n, chosen = -1;
            for (int u = 0; u < n; u++) if (!marked[u]) {
                if (deg[u] < min_deg) {
                    min_deg = deg[u];
                    chosen = u;
                }
            }
            marked[chosen] = true;
            stack.push(chosen);
            for (var v : adj.get(chosen))
                deg[v]--;
        }

        int[] color = new int[n];
        Arrays.fill(color, -1);
        while (!stack.empty()) {
            int u = stack.pop();
            boolean[] used = new boolean[nColors];
            Arrays.fill(used, false);
            for (int v = 0; v < n; v++) if (!marked[v] && color[v] != -1) {
                used[color[v]] = true;
            }
            for (int c = 0; c < nColors; c++) if (!used[c]) {
                color[u] = c;
                break;
            }
            marked[u] = false;
        }

        return color;
    }
}
