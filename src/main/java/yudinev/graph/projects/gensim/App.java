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
			int[] degrees = Statistics.getNodesDegrees(graph, parameters.getVDistr() + 1);
			System.out.println("Degree distribution");

			for (int i = 0; i < degrees.length; i++) {
				System.out.println(degrees[i]);

			}

		}
		if (parameters.getEDistr() > 0) {
			int[][] mass = Statistics.getQMatrix(graph, parameters.getEDistr());
			File degreeDegreeFile = new File("e_out.txt");
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
		double[] resEdgeRem,resNodeRem;
		
		
		if (parameters.isNodeRemovalSimulation()) {
			System.out.println("NodeRemovalSimulation");
			resNodeRem=Statistics.percolationNode(graph, 100, 0.1, 1., 0.1);
			for (double d : resNodeRem) {
				System.out.println(d);
			}
		}
		if (parameters.isEdgeRemovalSimulation()) {
			System.out.println("EdgeRemovalSimulation");
			resEdgeRem=Statistics.percolationEdges(graph, 100, 0.1, 1., 0.1);
			for (double d : resEdgeRem) {
				System.out.println(d);
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
				graph = Statistics.loadGraph("graph/myAs.net");
				LOG.info("Graph successfully loaded in {}.", FormatUtils.durationToHMS(System.nanoTime() - startTime));
			} catch (IOException e) {
				LOG.error("Failed to load graph from {} file.");
				LOG.debug("Failed to load graph from {} file.", e);
				System.exit(1);
			}
		} else if (type == GraphType.NPA_GENERATOR) {
			graph = getNPAGraph();
		} else if (type == GraphType.BA_GENERATOR) {
			graph = getBAGraph();
		}
		return graph;
	}

	private static Graph<Integer, Integer> getBAGraph() {
		Graph<Integer, Integer> g = genereateFastShrekBAGraph(new double[] { 0, 0, 1. }, (Integer k) -> {
			return 1. * k;
		}, 22963);
		return g;
	}

	private static Graph<Integer, Integer> getNPAGraph() {
		double[] d_pn2 = { 0, 0.0737, 0.1416, 0.7014, 1.455, 2.546, 3.393, 4.198, 4.490, 5.122, 5.783, 6.047, 7.216,
				8.516, 8.861, 9.306, 9.704, 10.07, 10.57, 11.06, 11.49, 11.92, 12.18, 12.44, 12.66, 12.86, 13.43, 13.98,
				14.11, 14.27, 20.21, 26.2 };
		Function<Integer, Double> pn2 = k -> {
//--------------------старая функция предпочтения-------------------------------	
			if (d_pn2.length > k)
				return d_pn2[k];
			return (0.829353 * k);

		};
		double[] rnDood1 = new double[] { 0, 0.3936, 0.4945, 0.1062, 0.00577 };

		double[] rnDood2 = new double[] { 0, 0.0773, 0, 0.2658, 0.3285, 0, 0.127, 0, 0, 0.0465, 0, 0, 0.0706, 0, 0, 0,
				0, 0, 0.0055, 0, 0.0036, 0, 0, 0, 0, 0, 0.0063, 0, 0, 0, 0.0687 };
		Graph<Integer, Integer> g = genereateDonkeyShrekGraph(rnDood1, rnDood2, pn2, 0.04096, 1, 22963);

		return g;
	}

	public static Graph<Integer, Integer> genereateDonkeyShrekGraph(double[] r_mon, double[] r_di,
			Function<Integer, Double> f, double gamma, double P, int numVertices) {
		GraphGenerator<Integer, Integer> gn = new StohIncrementNPAGenerator(graphFactory,
				Statistics.createIntegerFactory(), Statistics.createIntegerFactory(), r_mon, r_di, f, gamma, P,
				numVertices);
		return gn.create();
	}

	public static Graph<Integer, Integer> genereateFastShrekBAGraph(double[] m, Function<Integer, Double> f,
			int numVertices) {
		GraphGenerator<Integer, Integer> gn = new PAGenerator(graphFactory, Statistics.createIntegerFactory(),
				Statistics.createIntegerFactory(), m, f, numVertices);
		return gn.create();
	}

}
