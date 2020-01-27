package yudinev.graph.projects.gensim;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Buffer;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.buffer.UnboundedFifoBuffer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.io.PajekNetReader;


public class Statistics {
	private static List mEdgesRemoved = new ArrayList();
	private static List mNodesRemoved = new ArrayList();
	static Map<Object, Pair<Object>> removedEdges = new HashMap<Object, Pair<Object>>();
	public static double[] percolationEdges(Graph gr, int iter, double start_prob,
			double end_prob, double step_prob)
	{
		double[] ret = new double[(int) (1+Math.ceil((end_prob-start_prob)/step_prob))];
		int c=0;
		for (double p = start_prob; p <= end_prob; p = p + step_prob) {
			int ch = 0;
			int s_ns = 0;
			for (int i = 0; i < iter; i++) {
				removeEdges(gr, p);
				Set<Set<Object>> clusterSet = getClusters(gr);
				// int s_ns1 = clusterSet.size();
				int max = 0;
				for (Set<Object> set : clusterSet) {
					if (max < set.size())
						max = set.size();
				}
				ch = ch + max;
				restoreNodes(gr);
			}
			ret[c]= (1.0 * ch) / (1.0*iter);
			c++;
		}
		return ret;
	}

	private static void removeEdges(Graph graph, double p) {
		Set setLink = new HashSet();
		Iterator it_e = graph.getEdges().iterator();

		while (it_e.hasNext()) {
			Object edge = it_e.next();

			if (Math.random() < p) {
				Pair removedEdgeEndpoints = graph.getEndpoints(edge);
				removedEdges.put(edge, removedEdgeEndpoints);
				mEdgesRemoved.add(edge);
			}
		}
		for (Object edge : mEdgesRemoved)
			graph.removeEdge(edge);
	}
	private static void restoreNodes(Graph graph) {
		for (Object node : mNodesRemoved) {
			graph.addVertex(node);
		}
		for (Object edge : mEdgesRemoved) {
			Pair endpoints = removedEdges.get(edge);
			graph.addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
		}
		mNodesRemoved.clear();
		mEdgesRemoved.clear();
		removedEdges.clear();
	}

	public static double[]  percolationNode(Graph gr, int iter, double start_prob,
			double end_prob, double step_prob) {
		double[] ret = new double[(int) (1+Math.ceil((end_prob-start_prob)/step_prob))];
		int c=0;

		for (double p = start_prob; p <= end_prob; p = p + step_prob) {
			int ch = 0;
			int s_ns = 0;

			for (int i = 0; i < iter; i++) {
				Map<Integer, Integer> map = new HashMap();
				removeNodes(gr, p);
				Set<Set<Object>> clusterSet = getClusters(gr);
				int max = 0;
				for (Set<Object> set : clusterSet) {
					if (max < set.size())
						max = set.size();
				}
				ch = ch + max;
				restoreNodes(gr);
			}
			NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
			format.setMaximumFractionDigits(8);
			format.setMinimumFractionDigits(4);
			ret[c]= (1.0 * ch) / (1.0*iter);
			c++;
		}
		return ret;

	}
	public static Set<Set<Object>> getClusters(Graph<Object,Object> graph) {

        Set<Set<Object>> clusterSet = new HashSet<Set<Object>>();

        HashSet<Object> unvisitedVertices = new HashSet<Object>(graph.getVertices());

        while (!unvisitedVertices.isEmpty()) {
        	Set<Object> cluster = new HashSet<Object>();
        	Object root = unvisitedVertices.iterator().next();
            unvisitedVertices.remove(root);
            cluster.add(root);

            Buffer<Object> queue = new UnboundedFifoBuffer<Object>();
            queue.add(root);

            while (!queue.isEmpty()) {
            	Object currentVertex = queue.remove();
                Collection<Object> neighbors = graph.getNeighbors(currentVertex);

                for(Object neighbor : neighbors) {
                    if (unvisitedVertices.contains(neighbor)) {
                        queue.add(neighbor);
                        unvisitedVertices.remove(neighbor);
                        cluster.add(neighbor);
                    }
                }
            }
            clusterSet.add(cluster);
        }
        return clusterSet;
    }
	private static void removeNodes(Graph graph, double p) {
		Iterator it_e = graph.getVertices().iterator();
		while (it_e.hasNext()) {
			Object node = it_e.next();
			if (Math.random() < p) {
				for (Object removedEdge : graph.getIncidentEdges(node)) {
					Pair removedEdgeEndpoints = graph
							.getEndpoints(removedEdge);
					removedEdges.put(removedEdge, removedEdgeEndpoints);
					mEdgesRemoved.add(removedEdge);
					// удаляем рёбра
					// old_graph.removeEdge(removedEdge);

					// edges.setInUse(true);
				}
				mNodesRemoved.add(node);
			}
		}
		// действительно удаляем
		for (Object edge : mEdgesRemoved)
			graph.removeEdge(edge);

		for (Object nod : mNodesRemoved)
			graph.removeVertex(nod);
	}
	/**
	 * 
	 * @param <V>
	 * @param graph  is implemented Graph
	 * @param length is the frame of the distribution. Only degrees 0 < k < size are
	 *               considered
	 * @return array mass, mass[k] define a number of vertices that have degree k
	 */
	public static <V> int[] getNodesDegrees(Graph<V, ?> graph, int length) {
		Iterator<V> it = graph.getVertices().iterator();
		int[] distr = new int[length];
		while (it.hasNext()) {
			V node = it.next();
			int n = graph.degree(node);
			if (n < length)
				distr[n] = distr[n] + 1;
		}
		return distr;
	}

	/**
	 * 
	 * @param graph is implemented Graph
	 * @return two element array mass. The mass[1] define a number of triangles, the
	 *         mass[0] define a number of triplets
	 */
	public static int[] getTriAndVilk2(Graph graph) {
		int count = 0;
		int count2 = 0;
		Collection<Integer> list = graph.getVertices();

		for (Integer node : list) {

			int k = 0;
			for (Object link : graph.getIncidentEdges(node)) {
				k++;
			}
			count2 = count2 + k * (k - 1) / 2;

			Collection<Integer> neig_s = graph.getNeighbors(node);
			Iterator<Integer> it1 = neig_s.iterator();

			while (it1.hasNext()) {
				Integer node1 = it1.next();
				Iterator<Integer> it2 = neig_s.iterator();
				while (it2.hasNext()) {
					Integer node2 = it2.next();
					if ((node1 != node2) && graph.isNeighbor(node1, node2))
						count++;
				}
			}
		}
		return new int[] { count / 6, count2 };
	}
	/**
	 * Loads information about specific graph by using
	 * <code>load(String, edu.uci.ics.jung.graph.Graph)</code> method of
	 * <code>edu.uci.ics.jung.io.PajekNetReader</code> instance.
	 * 
	 * @author yudinev, Gleepa
	 * @param path a string representation of the path to the graph file
	 * @throws IOException
	 * @see edu.uci.ics.jung.io.PajekNetReader#load(String,
	 *      edu.uci.ics.jung.graph.Graph)
	 * @return the instance of a class which implements
	 *         <code>edu.uci.ics.jung.graph.Hypergraph</code> interface if specific
	 *         graph was loaded successfully, otherwise <code>IOException</code> is
	 *         thrown
	 */
	public static Graph<Integer, Integer> loadGraph(String path) throws IOException {
		return new PajekNetReader<>(createIntegerFactory(), createIntegerFactory()).load(path,
				new UndirectedSparseGraph<>());
	}

	/**
	 * @author yudinev, Gleepa
	 * @return the factory to create vertices or edges
	 */
	public static Factory<Integer> createIntegerFactory() {
		return new Factory<Integer>() {
			private int n = 0;

			@Override
			public Integer create() {
				return n++;
			}
		};
	}

	/**
	 * Method to get degree-degree distribution of undirected graph
	 * 
	 * @param graph - Graph must be undirected
	 * @param size  - frame of the distribution. Only degrees 0 < k < size are
	 *              considered
	 * @return array int[k][l] number define a number of edges (doubled) that have
	 *         edge endpoint degrees k and l,
	 */
	public static int[][] getQMatrix(Graph graph, int size) {
		int[][] ret = new int[size][size];
		Collection list = graph.getEdges();
		for (Object edge : list) {
			Pair<Object> p = graph.getEndpoints(edge);
			Object n1 = p.getFirst();
			Object n2 = p.getSecond();
			int degree_n1 = graph.degree(n1);
			int degree_n2 = graph.degree(n2);
			if (degree_n1 < size && degree_n2 < size) {
				ret[degree_n1][degree_n2] = ret[degree_n1][degree_n2] + 1;
				ret[degree_n2][degree_n1] = ret[degree_n2][degree_n1] + 1;
			}
		}
		return ret;
	}

	public static int getMaxDegree(Graph<Integer, Integer> graph) {
		int max=0;
		for (Integer v : graph.getVertices()) {
			if(max<graph.degree(v))max=graph.degree(v);
		}
		// TODO Auto-generated method stub
		return max;
	}

	
	
}
