package yudinev.graph.projects.gensim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import org.apache.commons.cli.ParseException;
import org.apache.commons.collections15.Factory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.io.PajekNetReader;

/**
 * Main Class of the project
 *
 */
public class App {
	private static final Logger LOG = LogManager.getLogger(App.class.getName());
	private static ProgramParameters parameters;
	static Factory<Graph<Integer, Integer>> graphFactory = new Factory<Graph<Integer, Integer>>() {

		@Override
		public Graph<Integer, Integer> create() {
			return new UndirectedSparseGraph();
		}

	};
	public static void main(String[] args) {
		String inputArgs = "";
		for (String string : args) {
			inputArgs += string + " ";
		}
		LOG.debug("Input arguments: " + inputArgs);
		long startTime;
		Graph<Integer, Integer> graph;
		ProgramParameters parameters = parseCmd(args);
		startTime = System.nanoTime();
		graph = initGraph(parameters.type);
		LOG.info("Elapsed time = {}.", FormatUtils.durationToHMS(System.nanoTime() - startTime));

		if (graph.getVertexCount() == 0) {
			LOG.error("Graph is empty.");
			System.exit(1);
		}
		LOG.info("Vertices = {}.", graph.getVertexCount());

		if (parameters.getVDistr() > 0) {
			int[] degrees = getNodesDegrees(graph, parameters.getVDistr() + 1);
			for (int i = 0; i < degrees.length; i++) {
				System.out.println(degrees[i]);

			}

		}
		if (parameters.getEDistr() > 0) {
			int[][] mass = getQMatrix(graph, parameters.getEDistr());
			File degreeDegreeFile = new File("C:\\temp\\e_out.txt");
			try (FileWriter writeFile = new FileWriter(degreeDegreeFile)) {
				for (int i = 0; i < mass.length; i++) {
					for (int j = 0; j < mass.length; j++) {
						writeFile.write(String.format("%.8f", mass[i][j] / 2. / (double) graph.getEdgeCount()) + " ");
					}
					writeFile.write("\n");

				}
			} catch (IOException e) {
				LOG.error("Problem with  degree-degree file saving.");
			}
		}

	}

	/**
	 * This is a wrapper for <code>parseCmdParameters(String[])</code> method of the
	 * <code>yudinev.graph.projects.gensim.ArgumentParser</code> instance.
	 * 
	 * @author yudinev, Gleepa
	 * @param args input arguments
	 * @see yudinev.graph.projects.gensim.ArgumentParser#parseCmdParameters(String[])
	 * @return the instance of
	 *         <codeyudinev.graph.projects.gensim.ProgramParameters</code> if input
	 *         arguments were parsed successfully, otherwise thrown exception is
	 *         logged and the program shuts down
	 */
	private static ProgramParameters parseCmd(String[] args) {
		ArgumentParser parser = new ArgumentParser();
		try {
			parameters = parser.parseCmdParameters(args);
		} catch (ParseException | NumberFormatException e) {
			LOG.error("Can't parse cmd parameters.");
			LOG.debug("Can't parse cmd parameters.", e);
			System.exit(1);
		}
		return parameters;
	}

	/**
	 * This is a wrapper for <code>loadGraph(String)</code> method.
	 * 
	 * @author yudinev, Gleepa
	 * @param type
	 * @see yudinev.graph.projects.gensim.App#loadGraph(String)
	 * @return the instance of a class which implements
	 *         <code>edu.uci.ics.jung.graph.Hypergraph</code> interface if specific
	 *         graph was loaded successfully, otherwise thrown exception is logged
	 *         and the program shuts down
	 */
	private static Graph<Integer, Integer> initGraph(GraphType type) {
		long startTime;
		Graph<Integer, Integer> graph = null;

		if (type == GraphType.AS_GRAPH) {
			startTime = System.nanoTime();
			try {
				graph = loadGraph("graph/myAs.net");
				LOG.info("Graph successfully loaded in {}.", FormatUtils.durationToHMS(System.nanoTime() - startTime));
			} catch (IOException e) {
				LOG.error("Failed to load graph from {} file.");
				LOG.debug("Failed to load graph from {} file.", e);
				System.exit(1);
			}
		}else if(type == GraphType.NPA_GENERATOR) {
			graph = getNPAGraph();
		}else if(type == GraphType.BA_GENERATOR) {
			graph = getBAGraph();
		}
		return graph;
	}

	private static Graph<Integer, Integer> getBAGraph() {
		Graph<Integer, Integer> g = genereateFastShrekBAGraph(new double[] { 0, 0, 1. }, (Integer k) -> {return 1. * k;}, 22963);
		return g;
	}

	private static Graph<Integer, Integer> getNPAGraph() {
		double[] d_pn2 = { 
				0, 0.0737, 0.1416, 0.7014, 1.455, 2.546, 3.393, 4.198, 4.490, 5.122, 5.783, 6.047, 7.216, 8.516, 8.861, 9.306, 
				9.704, 10.07, 10.57, 11.06, 11.49, 11.92, 12.18, 12.44, 12.66, 12.86, 13.43, 13.98, 14.11, 14.27, 20.21, 26.2
		};
		 Function<Integer, Double> pn2 = k -> {
//--------------------старая функция предпочтения-------------------------------	
			if (d_pn2.length > k) return d_pn2[k];
			return (0.829353 * k);

		};
		double[] rnDood1 = new double[] { 
				0, 0.3936, 0.4945, 0.1062, 0.00577};
		
		 double[] rnDood2 = new double[] { 
				0, 0.0773, 0, 0.2658, 0.3285, 0, 0.127, 0, 0, 0.0465, 0, 0, 0.0706, 0, 0, 0, 0, 0, 
				0.0055, 0, 0.0036, 0, 0, 0, 0, 0, 0.0063, 0, 0, 0, 0.0687 };
		Graph<Integer, Integer> g = genereateDonkeyShrekGraph(rnDood1, rnDood2, pn2,0.04096,1, 22963);

		return g;
	}
	
	public static Graph<Integer, Integer> genereateDonkeyShrekGraph(double[] r_mon, double[] r_di,
			Function<Integer, Double> f, double gamma, double P, int numVertices) {
		GraphGenerator<Integer, Integer> gn = new StohIncrementNPAGenerator(graphFactory, createIntegerFactory(), createIntegerFactory(), r_mon,
				r_di, f, gamma, P, numVertices);
		return gn.create();
	}

	public static Graph<Integer, Integer> genereateFastShrekBAGraph(double[] m, Function<Integer, Double> f,
			int numVertices) {
		GraphGenerator<Integer, Integer> gn = new PAGenerator(graphFactory, createIntegerFactory(), createIntegerFactory(), m, f,
				numVertices);
		return gn.create();
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
	private static Graph<Integer, Integer> loadGraph(String path) throws IOException {
		return new PajekNetReader<>(createIntegerFactory(), createIntegerFactory()).load(path,
				new UndirectedSparseGraph<>());
	}

	/**
	 * @author yudinev, Gleepa
	 * @return the factory to create vertices or edges
	 */
	private static Factory<Integer> createIntegerFactory() {
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

}
