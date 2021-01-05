package graphs;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DirectedGraph<V extends DGVertex<E>, E extends DGEdge<V>> {

    private Map<String,V> vertices = new HashMap<>();

    /** representation invariants:
     1.  all vertices in the graph are unique by their implementation of the getId() method
     2.  all edges in the graph reference vertices from and to which are true members of the vertices map
     (i.e. by true object instance equality == and not just by identity equality from the getId() method)
     3.  all edges of a vertex are outgoing edges, i.e. FOR ALL e in v.edges: e.from == v
     **/

    public DirectedGraph() { }

    public Collection<V> getVertices() {
        return this.vertices.values();
    }

    /**
     * finds the vertex in the graph identified by the given id
     * @param id
     * @return  the vertex that matches the given id
     *          return null if none of the vertices matches the id
     */
    public V getVertexById(String id) {
        return this.vertices.get(id);
    }


    /**
     * Adds newVertex to the graph, if not yet present and in a way that maintains the representation invariants.
     * If (a duplicate of) newVertex (with the same id) already exists in the graph,
     *      nothing will be added, and the existing duplicate will be kept and returned.
     * @param newVertex
     * @return  the duplicate of newVertex with the same id that already existed in the graph,
     *          or newVertex itself if it has been added.
     */
    public V addOrGetVertex(V newVertex) {
        if (vertices.values().stream().anyMatch(e -> e.getId().equals(newVertex.getId()))) return vertices.values().stream().filter(e -> e.getId().equals(newVertex.getId())).findFirst().get();
        vertices.put(newVertex.getId(), newVertex);
        // a proper vertex shall be returned at all times
        return newVertex;
    }

    /**
     * Adds all newVertices to the graph, which are not present yet and and in a way that maintains the representation invariants.
     * @param newVertices   an array of vertices to be added, provided as variable length argument list
     * @return  the number of vertices that actually have been added.
     */
    public int addVertices(V ...newVertices) {
        int count = 0;
        for (V v : newVertices) {
            if (v == this.addOrGetVertex(v)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds newEdge to the graph, if not yet present and in a way that maintains the representation invariants:
     * If any of the newEdge.from or newEdge.to vertices does not yet exist in the graph, it is added now.
     * If newEdge does not exist yet in the edges list of the newEdge.from vertex, it is added now,
     *            otherwise no change is made to that list.
     * @param newEdge   the new edge to be added in the edges list of newEdge.from
     * @return  the duplicate of newEdge that already existed in the graph
     *          or newEdge itselves if it just has been added.
     * @throws  IllegalArgumentException if newEdge.from or newEdge.to are duplicate vertices that have not
     *          been added to the graph yet have the same id as another vertex in the graph
     */
    public E addOrGetEdge(E newEdge) {
        if (vertices.get(newEdge.getFrom().getId()) == null) {
            addOrGetVertex(newEdge.getFrom());
        }
        if (vertices.get(newEdge.getTo().getId()) == null) {
            addOrGetVertex(newEdge.getTo());
        }
        for (DGVertex<E> v : vertices.values()) {
            if (v.getId().equals(newEdge.getFrom().getId()) && System.identityHashCode(v) != System.identityHashCode(newEdge.getFrom())) {
                throw new IllegalArgumentException("Duplicate found");
            }
            if (v.getId().equals(newEdge.getTo().getId()) && System.identityHashCode(v) != System.identityHashCode(newEdge.getTo())) {
                throw new IllegalArgumentException("Duplicate found");
            }
        }
//        if (
//                vertices.values().stream()
//                        .filter(e -> e.getId().equals(newEdge.getFrom().getId()))
//                        .anyMatch(e -> System.identityHashCode(e) != System.identityHashCode(newEdge.getFrom()))
//                        || vertices.values().stream()
//                                .filter(e -> e.getId().equals(newEdge.getTo().getId()))
//                        .anyMatch(e -> System.identityHashCode(e) != System.identityHashCode(newEdge.getTo())))
//            throw new IllegalArgumentException("Duplicate found!, please use that instance!");
        Set<E> edges = vertices.get(newEdge.getFrom().getId()).getEdges();
        if (!edges.isEmpty() && edges.stream().filter(e -> e.getFrom().getId().equals(newEdge.getFrom().getId())).anyMatch(e -> e.getTo().getId().equals(newEdge.getTo().getId()))) {
            return edges.stream().filter(e -> e.getFrom().getId().equals(newEdge.getFrom().getId())).filter(e -> e.getTo().getId().equals(newEdge.getTo().getId())).findFirst().orElse(newEdge);
        }
        edges.add(newEdge);
        // a proper edge shall be returned at all times
        return newEdge;
    }

    /**
     * Adds all newEdges to the graph, which are not present yet and in a way that maintains the representation invariants.
     * @param newEdges   an array of vertices to be added, provides as variable length argument list
     * @return  the number of edges that actually have been added.
     */
    public int addEdges(E ...newEdges) {
        int count = 0;
        for (E e : newEdges) {
            if (e == this.addOrGetEdge(e)) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return  the total number of vertices in the graph
     */
    public int getNumVertices() {
        return this.vertices.size();
    }

    /**
     * @return  the total number of edges in the graph
     */
    public int getNumEdges() {
        return vertices.values().stream().mapToInt(e -> e.getEdges().size()).sum();
    }

    /**
     * Clean-up unconnected vertices in the graph
     */
    public void removeUnconnectedVertices() {
        Set<V> unconnected = new HashSet<>();

        this.getVertices().stream().filter(v -> v.getEdges().size() == 0).forEach(unconnected::add);
        this.getVertices().stream().flatMap(v -> v.getEdges().stream().map(E::getTo)).forEach(unconnected::remove);
        unconnected.stream().map(V::getId).forEach(this.vertices::remove);
    }

    /**
     * represents a path of connected vertices and edges in the graph
     */
    public class DGPath {
        private V start = null;
        private LinkedList<E> edges = new LinkedList<>();
        private double totalWeight = 0.0;
        private Set<V> visited = new HashSet<>();

        /**
         * representation invariants:
         * 1. The edges are connected by vertices, i.e. FOR ALL i: 0 < i < edges.length: edges[i].from == edges[i-1].to
         * 2. The path begins at vertex == start
         * 3. if edges is empty, the path also ends at vertex == start
         * otherwise edges[0].from == start and the path continues along edges[i].to for all 0 <= i < edges.length
         **/

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(
                    String.format("Weight=%f Length=%d Visited=%d (",
                            this.totalWeight, 1 + this.edges.size(), this.visited.size()));
            sb.append(start.getId());
            for (E e : edges) {
                sb.append(", " + e.getTo().getId());
            }
            sb.append(")");
            return sb.toString();
        }

        public V getStart() {
            return start;
        }

        public LinkedList<E> getEdges() {
            return edges;
        }

        public double getTotalWeight() {
            return totalWeight;
        }

        public Set<V> getVisited() {
            return visited;
        }
    }

    /**
     * Uses a depth-first search algorithm to find a path from the start vertex to the target vertex in the graph
     * The path.totalWeight should indicate the number of edges in the result path
     * All vertices that are being visited by the search should also be registered in path.visited
     * @param startId
     * @param targetId
     * @return  the path from start to target
     *          returns null if either start or target cannot be matched with a vertex in the graph
     *                          or no path can be found from start to target
     */
    public DGPath depthFirstSearch(String startId, String targetId) {

        V start = this.getVertexById(startId);
        V target = this.getVertexById(targetId);
        if (start == null || target == null) return null;

        DGPath path = new DGPath();
        path.start = start;
        path.visited.add(start);

        // easy target
        if (start == target) return path;

        if (dfsRecursionHelperMethod(start, target, path)) {
            Collections.reverse(path.getEdges());
            path.totalWeight = path.getEdges().size();
            return path;
        }


        // no path found, graph was not connected ???
        return null;
    }

    private boolean dfsRecursionHelperMethod(V currentNode, V targetNode, DGPath path) {
        path.visited.add(currentNode);
        if (!currentNode.equals(targetNode)) {
            for (E edge : currentNode.getEdges()) {
                if (path.visited.contains(edge.getTo())) continue;
                if (dfsRecursionHelperMethod(edge.getTo(), targetNode, path)) {
                    path.getEdges().add(edge);
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * Uses a breadth-first search algorithm to find a path from the start vertex to the target vertex in the graph
     * The path.totalWeight should indicate the number of edges in the result path
     * All vertices that are being visited by the search should also be registered in path.visited
     * @param startId
     * @param targetId
     * @return  the path from start to target
     *          returns null if either start or target cannot be matched with a vertex in the graph
     *                          or no path can be found from start to target
     */
    public DGPath breadthFirstSearch(String startId, String targetId) {

        V start = this.getVertexById(startId);
        V target = this.getVertexById(targetId);
        if (start == null || target == null) return null;

        DGPath path = new DGPath();
        path.start = start;
        path.visited.add(start);

        // easy target
        if (start == target) return path;

        if (bfsRecursiveHelperMethod(start, target, path)) {
            Collections.reverse(path.getEdges());
            return path;
        }

        // no path found, graph was not connected ???
        return null;
    }

    private boolean bfsRecursiveHelperMethod(V currentNode, V targetNode, DGPath path) {
        path.visited.add(currentNode);
        List<E> edges = new ArrayList<>();
        if (!currentNode.equals(targetNode)) {
            for (E edge : currentNode.getEdges()) {
                if (path.visited.contains(edge.getTo())) continue;
                path.visited.add(edge.getTo());
                edges.add(edge);
                if (edge.getTo().equals(targetNode)) {
                    path.getEdges().add(edge);
                    return true;
                }
            }
            for (E edge : edges) {
                if (bfsRecursiveHelperMethod(edge.getTo(), targetNode, path)) {
                    path.getEdges().add(edge);
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    // helper class to register the state of a vertex in dijkstra shortest path algorithm
    // your may change this class or delete it altogether follow a different approach in your implementation
    private class DSPNode implements Comparable<DSPNode> {
        public V vertex;                // the graph vertex that is concerned with this DSPNode
        public E fromEdge = null;        // the edge from the predecessor's vertex to this node's vertex
        public boolean marked = false;  // indicates DSP processing has been marked complete
        public double weightSumTo = Double.MAX_VALUE;   // sum of weights of current shortest path to this node's vertex

        public DSPNode(V vertex) {
            this.vertex = vertex;
        }

        // comparable interface helps to find a node with the shortest current path, sofar
        @Override
        public int compareTo(DSPNode dspv) {
            return Double.compare(this.weightSumTo, dspv.weightSumTo);
        }
    }

    /**
     * Calculates the edge-weighted shortest path from start to target
     * Uses a minimum distance heuristic from any vertex to the target
     * in order to reduce the number of visited vertices during the search
     * @param startId
     * @param targetId
     * @param weightMapper    provides a function, by which the weight of an edge can be retrieved or calculated
     * @return  the shortest path from start to target
     *          returns null if either start or target cannot be matched with a vertex in the graph
     *                          or no path can be found from start to target
     */
    public DGPath dijkstraShortestPath(String startId, String targetId,
                                       Function<E,Double> weightMapper) {

        V start = this.getVertexById(startId);
        V target = this.getVertexById(targetId);
        if (start == null || target == null) return null;

        // initialise the result path of the search
        DGPath path = new DGPath();
        path.start = start;
        path.visited.add(start);

        // easy target
        if (start == target) return path;

        // keep track of the DSP status of all visited nodes
        // you may choose a different approach of tracking progress of the algorithm, if you wish
        Map<V, DSPNode> progressData = new HashMap<>();

        // initialise the progress of the start node
        DSPNode nextDspNode = new DSPNode(start);
        nextDspNode.weightSumTo = 0.0;
        progressData.put(start, nextDspNode);

        V to;
        DSPNode node;

        while (nextDspNode != null) {
            //mark the current dspnode as marked
            nextDspNode.marked = true;

            //stop the loop als de target node gevonden is.
            if (nextDspNode.vertex.equals(target)) break;

            for (E edge : nextDspNode.vertex.getEdges()) {
                to = edge.getTo();
                path.visited.add(to);
                //check if the node that the edge goes to, is not marked.
                if (progressData.get(to) != null && progressData.get(to).marked) continue;

                //check if the progressData map already contains this node
                if (progressData.get(to) != null) {
                    //add the available dspnode to the node var
                    node = progressData.get(to);
                    //save the calculated new length to a var
                    double possibleNewLength = nextDspNode.weightSumTo + weightMapper.apply(edge);
                    //replace the old length with the new length when te new length is lower than the old length
                    if (possibleNewLength < node.weightSumTo) node.weightSumTo = possibleNewLength;
                } else {
                    //create a new dspnode
                    node = new DSPNode(to);
                    //calculate the weightsumto from the new node
                    node.weightSumTo = nextDspNode.weightSumTo + weightMapper.apply(edge);
                    //add the new node to the map
                    progressData.put(to, node);
                }
            }

            //create an instance to the next unvisited node that has the lowest weight value
            DSPNode comingNode = progressData.values().stream().filter(e -> !e.marked).min(DSPNode::compareTo).orElse(null);

            boolean breaking = false;

            //check if that node exists
            if (comingNode != null) {
                //if that node exists, calculate which edge is used to go to the new node
                for (DSPNode n : progressData.values()) {
                    if (breaking) break;
                    for (E edge : n.vertex.getEdges()) {
                        if (edge.getTo().equals(comingNode.vertex)) {
                            if (weightMapper.apply(edge) + n.weightSumTo == comingNode.weightSumTo) {
                                breaking = true;
                                comingNode.fromEdge = edge;
                                break;
                            }
                        }
                    }
                }
            }

            //replace the nextdspnode with the coming node
            nextDspNode = comingNode;
        }

        //get the instance of the target node
        node = progressData.get(target);

        //return null if that node doesn't exits
        if (node == null) return null;

        //set the totalweight of the path equals to the weight that is used to go to the target node.
        path.totalWeight = node.weightSumTo;

        //loop through the nodes that the path is made up of
        while (node.fromEdge != null) {
            //add the node to the edge at the front of the linkedlist
            path.getEdges().addFirst(node.fromEdge);
            //move the node to the previous node in the path
            node = progressData.get(node.fromEdge.getFrom());
        }

        // return the path
        return path;
    }


    // helper class to register the state of a vertex in A* shortest path algorithm
    private class ASNode extends DSPNode {
        double estimatedCost;

        private ASNode(V vertex) {
            super(vertex);
            estimatedCost = -1;
        }

        @Override
        public int compareTo(DSPNode dspv) {
            return Double.compare(estimatedCost, ((ASNode)dspv).estimatedCost);
        }
    }


    /**
     * Calculates the edge-weighted shortest path from start to target
     * Uses a minimum distance heuristic from any vertex to the target
     * in order to reduce the number of visited vertices during the search
     * @param startId
     * @param targetId
     * @param weightMapper    provides a function, by which the weight of an edge can be retrieved or calculated
     * @param minimumWeightEstimator provides a function, by which a lower bound of the cumulative weight
     *                        between two vertices can be calculated.
     * @return  the shortest path from start to target
     *          returns null if either start or target cannot be matched with a vertex in the graph
     *                          or no path can be found from start to target
     */
    public DGPath aStarShortestPath(String startId, String targetId,
                                     Function<E,Double> weightMapper,
                                     BiFunction<V,V,Double> minimumWeightEstimator ) {

        V start = this.getVertexById(startId);
        V target = this.getVertexById(targetId);
        if (start == null || target == null) return null;

        DGPath path = new DGPath();
        path.start = start;
        path.visited.add(start);

        // easy target
        if (start == target) return path;

        Map<V, ASNode> programData = new HashMap<>();
        ASNode nextNode = new ASNode(start);
        nextNode.weightSumTo = 0;
        nextNode.estimatedCost = minimumWeightEstimator.apply(start, target);
        programData.put(start, nextNode);

        V to;
        ASNode node;
        ASNode comingNode;

        while (nextNode != null) {
            nextNode.marked = true;

            if (nextNode.vertex.equals(target)) break;

            for (E edge : nextNode.vertex.getEdges()) {
                to = edge.getTo();
                path.visited.add(to);

                if (programData.get(to) != null) {
                    node = programData.get(to);

                    if (node.marked) continue;

                    if (node.estimatedCost == -1) node.estimatedCost = minimumWeightEstimator.apply(to, target);

                    double possibleNewLength = nextNode.weightSumTo + weightMapper.apply(edge);

                    if (possibleNewLength + nextNode.estimatedCost < node.weightSumTo + node.estimatedCost) node.weightSumTo = possibleNewLength;
                } else {
                    node = new ASNode(to);
                    node.weightSumTo = nextNode.weightSumTo + weightMapper.apply(edge);
                    node.estimatedCost = minimumWeightEstimator.apply(to, target);
                    programData.put(to, node);
                }
            }

            comingNode = programData.values().stream().filter(e -> !e.marked).min(ASNode::compareTo).orElse(null);

            if (comingNode != null) {
                for (ASNode n : programData.values()) {
                    if (n.vertex.equals(comingNode.vertex)) continue;
                    for (E edge : n.vertex.getEdges()) {
                        if (edge.getTo().equals(comingNode.vertex)) {
                            if (comingNode.weightSumTo == (n.weightSumTo + weightMapper.apply(edge))) {
                                comingNode.fromEdge = edge;
                                break;
                            }
                        }
                    }
                }
            }
            nextNode = comingNode;
        }

        node = programData.get(target);

        if (node == null) return null;

        path.totalWeight = node.weightSumTo;

        while (node.fromEdge != null) {
            path.getEdges().addFirst(node.fromEdge);
            node = programData.get(node.fromEdge.getFrom());
        }
        // no path found, graph was not connected ???
        return path;
    }

    /**
     * Calculates the edge-weighted shortest path from start to target
     * @param startId
     * @param targetId
     * @param weightMapper    provides a function by which the weight of an edge can be retrieved or calculated
     * @return  the shortest path from start to target
     *          returns null if either start or target cannot be matched with a vertex in the graph
     *                          or no path can be found from start to target
     */
    public DGPath dijkstraShortestPathByAStar(String startId, String targetId,
                                              Function<E,Double> weightMapper) {
        return aStarShortestPath(startId, targetId,
                weightMapper,
                (e, i) -> 0.
        );
    }

    @Override
    public String toString() {
        return this.getVertices().stream()
                .map(Object::toString)
                .collect(Collectors.joining(",\n  ","{ ","\n}"));
    }
}
