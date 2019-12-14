package yudinev.graph.projects.gensim;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.collections15.Factory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;

/**
 * Main Class of the project
 *
 */
public class App {
	private static final Logger LOG = LogManager.getLogger(App.class.getName());
    private static ProgramParameters parameters;


	public static void main(String[] args) {
		String inputArgs = "";
		for (String string : args) {
			inputArgs += string + " ";
		}
		LOG.debug("Input arguments: " + inputArgs);
		long startTime;
		Graph<Integer, Integer> graph;
		ProgramParameters parameters = parseCmd(args);
		graph = initGraph();
	}

	/**
	 * This is a wrapper for <code>parseCmdParameters(String[])</code> method of the
	 * <code>yudinev.graph.projects.gensim.ArgumentParser</code> instance.
	 * 
	 * @author yudinev
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
	 * @author yudinev
	 * @see yudinev.graph.projects.gensim.App#loadGraph(String)
	 * @return the instance of a class which implements
	 *         <code>edu.uci.ics.jung.graph.Hypergraph</code> interface if specific
	 *         graph was loaded successfully, otherwise thrown exception is logged
	 *         and the program shuts down
	 */
	private static Graph<Integer, Integer> initGraph() {
		long startTime;
		Graph<Integer, Integer> graph = null;

		LOG.info("Loading graph from {} file.", parameters.getGraphFile());
		startTime = System.nanoTime();
		try {
			graph = loadGraph(parameters.getGraphFile());
			LOG.info("Graph successfully loaded in {}.", FormatUtils.durationToHMS(System.nanoTime() - startTime));
		} catch (IOException e) {
			LOG.error("Failed to load graph from {} file.", parameters.getGraphFile());
			LOG.debug("Failed to load graph from {} file.", parameters.getGraphFile(), e);
			System.exit(1);
		}
		return graph;
	}

	/**
	 * Loads information about specific graph by using
	 * <code>load(String, edu.uci.ics.jung.graph.Graph)</code> method of
	 * <code>edu.uci.ics.jung.io.PajekNetReader</code> instance.
	 * 
	 * @author yudinev
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
	 * @author yudinev
	 * @return the factory object
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
}
