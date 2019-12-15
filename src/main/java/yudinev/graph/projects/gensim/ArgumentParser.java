package yudinev.graph.projects.gensim;

import org.apache.commons.cli.*;

/**
 * @author yudinev
 */
public class ArgumentParser {

	private final Option graphPath;

	private final Option simulations;

	private final Option vDistr;

	private final Option eDistr;

	private final Options options;

	private final HelpFormatter help;

	public ArgumentParser() {
		graphPath = OptionBuilder.withType(String.class).isRequired(true).hasArg(true).withLongOpt("graph")
				.withDescription("This mandatory parameter sets method to the graph generation:\n"
						+ "ba - when using barabasi-albert model,\n"
						+ "npa - when using NPA-graph with complex stohastic increments,\n"
						+ "as- when using M. Newman AS-snapshot graph \n")
				.create("g");
		simulations = OptionBuilder.withType(String.class).isRequired(false).hasArg(true).withLongOpt("simulations")
				.withDescription("List of available simulations:\n" + "n - nodes removal process simulation ,\n"
						+ "e - nodes and edges removal process simulation,\n" + "v - SIS-model simulation,\n")
				.create("s");
		vDistr = OptionBuilder.withType(Integer.class).isRequired(false).hasArg(true).withLongOpt("v_distr")
				.withDescription("This parameter sets number of runs used by sampling algorithms only.").create("d");
		eDistr = OptionBuilder.withType(Integer.class).isRequired(false).hasArg(true).withLongOpt("e_dister")
				.withDescription("This parameter sets number of parallel threads.").create("e");
		options = new Options().addOption(graphPath).addOption(simulations).addOption(vDistr).addOption(eDistr);
		help = new HelpFormatter();
	}

	/**
	 * Parses input <code>args</code> and returns the instance of
	 * <code>yudinev.graph.projects.gensim.ProgramParameters</code>.
	 *
	 * @author yudinev, Gleepa
	 * @param args input arguments
	 * @return the the instance of
	 *         <code>yudinev.graph.projects.gensim.ProgramParameters</code> if input
	 *         arguments were parsed successfully, otherwise
	 *         <code>org.apache.commons.cli.ParseException</code> or
	 *         <code>NumberFormatException</code> is thrown
	 */
	public ProgramParameters parseCmdParameters(final String[] args) throws ParseException, NumberFormatException {
		ProgramParameters parameters = new ProgramParameters();
		try {
			final CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);
			
			switch (cmd.getOptionValue(graphPath.getOpt())) {
			case "ba":
				parameters.setTypeGraph(GraphType.BA_GENERATOR);
				break;
			case "npa":
				parameters.setTypeGraph(GraphType.NPA_GENERATOR);
				break;
			case "as":
				parameters.setTypeGraph(GraphType.AS_GRAPH);
				break;
			default:
				break;

			}
			//parameters.setEDistr(Integer.parseInt(cmd.getOptionValue(eDistr.getOpt())));

			if (cmd.hasOption(simulations.getOpt())) {
				for (String string : cmd.getOptionValue(simulations.getOpt()).split(",")) {
					switch (string) {
					case "n":
						parameters.setIsNodeRemovalSimulationFlag();
						break;
					case "e":
						parameters.setIsEdgeRemovalSimulationFlag();
						break;
					case "v":
						parameters.setIsSISSimulationFlag();
						break;
					default:
						break;
					}
				}
			}
			if (cmd.hasOption(vDistr.getOpt())) {
				parameters.setVDistr(Integer.parseInt(cmd.getOptionValue(vDistr.getOpt())));
			}
			if (cmd.hasOption(eDistr.getOpt())) {
				parameters.setEDistr(Integer.parseInt(cmd.getOptionValue(eDistr.getOpt())));
			}
		} catch (ParseException e) {
			help.printHelp("GenSim", options);
			throw new ParseException(e.getMessage());
		}
		return parameters;
	}

}