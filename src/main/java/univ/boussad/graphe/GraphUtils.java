package univ.boussad.graphe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class GraphUtils {

    private GraphUtils() { throw new IllegalStateException("GraphUtils cannot be instantiated!"); } // Utility class, cannot be instantiated.

    public static Map<Integer, int[]> cloneGraph(Map<Integer, int[]> graph) {
        Map<Integer, int[]> clonedGraph = new HashMap<>();
        for (Map.Entry<Integer, int[]> entry : graph.entrySet()) {
            clonedGraph.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
        } // Make a deep copy of the original graph.
        return clonedGraph;
    }

    public static void removeEdge(Map<Integer, int[]> graph, int u, int v) {
        graph.put(u, removeNeighbor(graph.get(u), v));
        graph.put(v, removeNeighbor(graph.get(v), u));
    }

    public static boolean edgeExists(Map<Integer, int[]> graph, int u, int v) {
        if (!graph.containsKey(u)) return false;
        for (int neighbor : graph.get(u)) {
            if (neighbor == v) return true;
        }
        return false;
    }

    public static int countTotalEdges(Map<Integer, int[]> graph) {
        int edgeCount = 0;
        for (int[] neighbors : graph.values()) {
            edgeCount += neighbors.length;
        }
        return edgeCount / 2;
    }

    private static int[] removeNeighbor(int[] neighbors, int target) {
        return Arrays.stream(neighbors)
                .filter(neighbor -> neighbor != target)
                .toArray();
    }
}
