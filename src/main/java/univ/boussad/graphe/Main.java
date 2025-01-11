package univ.boussad.graphe;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private Main() {
        throw new IllegalStateException("Main cannot be instantiated!");
    } // No need to instantiate the Main class.

    public static void main(String[] args) throws IOException {
        var graphMap = new HashMap<Integer, int[]>();

        if(args.length < 1) {
            System.out.println("Missing <graph_file> argument.");
            return;
        }

        String graphPath = args[0];
        if(!Files.exists(Path.of(graphPath))) {
            System.out.println("The file specified does not exist.");
            return;
        }

        final var fileReader = new FileReader(graphPath);
        try (fileReader; var bufferedReader = new BufferedReader(fileReader)) {
            String line;
            int index = -1, edges = 0;

            while ((line = bufferedReader.readLine()) != null) {
                if (index == -1) {
                    edges = Integer.parseInt(line); // Read the edges count from the first line.
                    index = 1;
                    continue;
                }

                String[] edgeNeighbors = line.split(" "); // Neighbors are space separated.
                int[] parsedEdgeNeighbors = new int[edgeNeighbors.length];

                for (int i = 0; i < edgeNeighbors.length; i++) {
                    int n = Integer.parseInt(edgeNeighbors[i]);
                    if (n > edges)
                        throw new IllegalArgumentException("Edge: " + index + " references an unknown node.");

                    parsedEdgeNeighbors[i] = n;
                }

                graphMap.put(index, parsedEdgeNeighbors);
                index++;
            }

            if (edges != index - 1)
                throw new IllegalStateException("Read more or less neighbors than expected! (how did we get here?)");
        }

        // Do our operations.

        boolean isEulerianGraph = isEulerian(graphMap);
        System.out.println("Is eulerian: " + isEulerianGraph);

        if (!isEulerianGraph) { // Eulerian graphs cannot have isthmus.
            for (int i = 1; i <= graphMap.size(); i++) {
                for (int j = 1; j <= graphMap.size(); j++) {
                    if (i == j) continue;

                    if (!isIsthme(graphMap, i, j)) continue;
                    System.out.println("(" + i + ", " + j + ") is an isthme");
                }
            }
        } else
            for (int startNode = 1; startNode <= graphMap.size(); startNode++) {
                var cycle = fleury(graphMap, startNode);

                var prettyCycle = new StringBuilder();
                for (int i = 0; i < cycle.size(); i++) {
                    prettyCycle.append(cycle.get(i));
                    if (i + 1 != cycle.size()) prettyCycle.append(" -> ");
                }

                System.out.println("Starting from " + startNode + ": " + prettyCycle + " (" + isCycleEulerian(graphMap, cycle) + ")");
            }
    }

    private static List<Integer> fleury(Map<Integer, int[]> graph, int u) {
        List<Integer> cycle = new ArrayList<>();
        cycle.add(u); // Start cycle with the starting vertex u

        var graphCopy = GraphUtils.cloneGraph(graph);
        int currentVertex = u;

        while (graphCopy.get(currentVertex).length > 0) {
            int nextVertex = -1;
            int[] neighbors = graphCopy.get(currentVertex);

            for (int neighbor : neighbors) {
                if (!isIsthme(graphCopy, currentVertex, neighbor)) {
                    nextVertex = neighbor;
                    break;
                }
            }

            // If all edges are isthmuses or only one choice left, take any available edge
            if (nextVertex == -1) nextVertex = neighbors[0];

            cycle.add(nextVertex);
            GraphUtils.removeEdge(graphCopy, currentVertex, nextVertex);

            currentVertex = nextVertex;
        }

        return cycle;
    }

    private static boolean isEulerian(Map<Integer, int[]> graphMap) {
        for (int[] neighbors : graphMap.values())
            if (neighbors.length % 2 != 0) return false; // Check if we have an even number of neighbors on every node.

        // Use DFS to check if it is connected.
        return isConnected(graphMap);
    }

    private static boolean isConnected(Map<Integer, int[]> graphMap) {
        var visitedSet = new HashSet<Integer>();
        var loopStack = new Stack<Integer>();

        int startingNode = graphMap.keySet().iterator().next();
        loopStack.push(startingNode);

        while (!loopStack.isEmpty()) {
            int opNode = loopStack.pop();
            if (visitedSet.contains(opNode)) continue;

            visitedSet.add(opNode);

            for (int opNodeNeighbor : graphMap.get(opNode))
                if (!visitedSet.contains(opNodeNeighbor)) loopStack.push(opNodeNeighbor);
        }

        return visitedSet.size() == graphMap.size(); // If we haven't visited all the graph, it means that it isn't connected.
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // Intended behaviour.
    private static boolean isIsthme(Map<Integer, int[]> graph, int u, int v) {
        if (!isConnected(graph)) return false;

        var clone = GraphUtils.cloneGraph(graph);
        GraphUtils.removeEdge(clone, u, v);

        return !isConnected(clone); // If the graph is no longer connected when removing (u,v); it means that (u,v) is an "isthme"
    }

    private static boolean isCycleEulerian(Map<Integer, int[]> graph, List<Integer> cycle) {
        // It is literally in the name: "cycle". If the first element of the cycle isn't the last, then it isn't a "cycle"
        if (cycle.isEmpty() || !cycle.get(0).equals(cycle.get(cycle.size() -1))) return false;

        var visitedEdges = new HashSet<String>();
        for (int i = 0; i < cycle.size() - 1; i++) {
            int u = cycle.get(i), v = cycle.get(i + 1);

             if (!GraphUtils.edgeExists(graph, u, v))
                return false; // If the edges aren't connected, it isn't an eulerian cycle.

            // An edge identifier based on the direction we came in from (either "u->v" or "v->u")
            String edge = u < v ? u + "-" + v : v + "-" + u;

            // Check if the edge has already been visited
            if (visitedEdges.contains(edge))
                return false; // If we have visited the edge more than once from the same direction, it isn't an eulerian cycle.
            visitedEdges.add(edge);
        }

        return visitedEdges.size() == GraphUtils.countTotalEdges(graph);
    }
}
