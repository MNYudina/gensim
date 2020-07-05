package vis;
/*
z * This software is open-source under the BSD license
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.Graph;

/**
 * @version 0.1, 01/12/10
 * 
 *          <p>
 * 			The accelerated "preferential attachment" random graph generator. At
 *          each time step, a new vertex is created and is connected to existing
 *          vertices according to the principle of "preferential attachment",
 *          whereby vertices with higher degree have a higher probability of
 *          being selected for attachment.
 *          </p>
 * 
 *          <p>
 * 			At a given timestep, the probability <code>p</code> of creating an
 *          edge between an existing vertex <code>v</code> and the newly added
 *          vertex is
 * 
 *          <pre>
 * p = f(degree(v)) / Summ_j f(degree(v_j));
 *          </pre>
 *          </p>
 * 
 *          <p>
 * 			where <code>Summ_j f(degree(v_j))</code> is sum of given
 *          "preferential attachment" functions of the degree of connectivity of
 *          vertices
 *          </p>
 * 
 *          <p>
 *          All nodes with the specified connection are stored in one map's
 *          slot(layers), thereby accelerating the generation algorithm. The
 *          layer is specified node's degree. The probability Qk of choosing
 *          such a layer k is Qk=|A|/ Summ_j f(degree(v_j)); where |A| is the
 *          number of vertexes in layers of k After the layer is selected nodes
 *          are getting randomly from it
 *          </p>
 * 
 *          <p>
 * 			The graph created may be either directed or undirected (controlled
 *          by type the given Graph If the graph is directed, then the edges
 *          added will be directed from the newly added vertex u to the existing
 *          vertex v
 *          </p>
 *          <p>
 * 			The <code>parallel</code> constructor parameter specifies whether
 *          parallel edges may be created.
 *          </p>
 * 
 * 
 */
public class GenNonIntBA5<V, E> {
	Map<Integer, List<V>> map = new HashMap<Integer, List<V>>();
	protected PrefferentialAttachment attachRule;
	protected Factory<V> vertexFactory;
	protected Factory<E> edgeFactory;
	protected double numEdgesToAttachMonad[];
	protected double numEdgesToAttachDiad[];

	private Random mRand;
	boolean parallel = false;

	/**
	 * 
	 * Create an instance of the generator, with which you can on the basis of
	 * the graph-seed to grow by "preferential attachment" the graph
	 * 
	 * @param vertexFactory
	 *            defines a way to create nodes
	 * @param edgeFactory
	 *            defines a way to create edges
	 * @param probEdgesToAttach
	 *            the number of edges that should be attached
	 * @param attachRule
	 *            is interface specifying method which use in rule of
	 *            "preferential attachment"
	 * 
	 */
	double gamma=0.;//0.171118345711874;
	double P=.0;
	public GenNonIntBA5(Factory<V> vertexFactory, Factory<E> edgeFactory, double[] probEdgesToAttachMonad, double[] probEdgesToAttachDiad,
			PrefferentialAttachment attachRule, double g, double p){
		gamma=g;
		P=p;
		double s = 0.;
		

		this.vertexFactory = vertexFactory;
		this.edgeFactory = edgeFactory;
		this.numEdgesToAttachMonad = probEdgesToAttachMonad;
		this.numEdgesToAttachDiad=probEdgesToAttachDiad;
		this.attachRule = attachRule;
		mRand = new Random();
	}

	/**
	 * 
	 * Create an instance of the generator, with which you can on the basis of
	 * the graph-seed to grow by "preferential attachment" the graph and which
	 * will use the current time as a seed for the random number generation.
	 * 
	 * @param vertexFactory
	 *            defines a way to create nodes
	 * @param edgeFactory
	 *            defines a way to create edges
	 * @param numEdgesToAttacMonad
	 *            the number of edges that should be attached
	 * @param parallel
	 *            specifies whether the algorithm permits parallel edges
	 * @param seed
	 *            random number seed
	 * @param attachRule
	 *            is interface specifying method which use in rule of
	 *            "preferential attachment"
	 */
	public GenNonIntBA5(Factory<V> vertexFactory, Factory<E> edgeFactory, double[] numEdgesToAttacMonad, double[] numEdgesToAttachDiad, boolean parallel,
			int seed, PrefferentialAttachment attachRule, double g, double p) {
		this(vertexFactory, edgeFactory, numEdgesToAttacMonad,numEdgesToAttachDiad, attachRule,g,p);
		this.parallel = parallel;
		mRand = new Random(seed);

	}

	public Graph<V, E> evolve(int step, Graph<V, E> graph) {
		maxlayer = 0;
		graph.getEdgeCount();
		for (Iterator<V> iterator = graph.getVertices().iterator(); iterator.hasNext();) {
			V v = iterator.next();
			addToLayer(v, graph.degree(v));
		}
		

		// ---------------
		for (int i = 0; i < step; i++) {

			if(Math.random()<gamma){
				V a = vertexFactory.create();
				V b = vertexFactory.create();

				Map<V, Integer> set = new HashMap<V, Integer>();
				Set<V> list_B = new HashSet<V>();

				// <V, Integer> set2 = new HashMap<V, Integer>();
				Set<V> list_A = new HashSet<V>();

				double s = 0.;
				double r = mRand.nextDouble();
				int addEd_b = 0;
				for (int j = 0; j < numEdgesToAttachDiad.length; j++) {
					s = s + numEdgesToAttachDiad[j];
					if (s > r) {
						addEd_b = j;
						break;
					}
				}
				int addEd_a = addEd_b;
				addEd_b--;
				double alpha = P;
				if (addEd_b > 0)
					do {
						int k = getLayer();
						V n = map.get(k).get(mRand.nextInt(map.get(k).size()));
						set.put(n, new Integer(k));
						list_B.add(n);
						if (Math.random() < alpha)
							list_A.add(n);
					} while (list_B.size() != addEd_b);

				if (addEd_a > 0)
					while (list_A.size() != addEd_a) {
						V n2 = null;
						int k2 = getLayer();
						n2 = map.get(k2).get(mRand.nextInt(map.get(k2).size()));
						set.put(n2, new Integer(k2));
						list_A.add(n2);
					}
				graph.addVertex(b);
				for (V n : list_B) {
					graph.addEdge(edgeFactory.create(), b, n);
					int tec = set.get(n);
					map.get(tec).remove(n);
					addToLayer(n, tec + 1);
					set.put(n, tec + 1);
				}
				graph.addVertex(a);
				for (V n2 : list_A) {
					graph.addEdge(edgeFactory.create(), a, n2);
					int tec = set.get(n2);
					map.get(tec).remove(n2);
					addToLayer(n2, tec + 1);
					set.put(n2, tec + 1);
				}
				graph.addEdge(edgeFactory.create(), b, a);
				addToLayer(a, graph.degree(a));
				addToLayer(b, graph.degree(b));
			}
			else
			{
				V new_n = vertexFactory.create();
				Map<V, Integer> set = new HashMap<V, Integer>();
				List<V> list = new ArrayList<V>();
				double s=0.;
				double r=mRand.nextDouble();
				int addEd =0;
				for (int j = 0; j < numEdgesToAttachMonad.length; j++) {
						s=s+numEdgesToAttachMonad[j];
						if(s>r)
						{
							addEd =	j;
							break;
						}
				 }
				if(addEd>0)
				do {
					int k = getLayer();
					V n = map.get(k).get(mRand.nextInt(map.get(k).size()));
					set.put(n, new Integer(k));
					list.add(n);
				} while (list.size() != addEd);
				graph.addVertex(new_n);
				for (V n : list) {
					graph.addEdge(edgeFactory.create(), new_n, n);
					int tec = set.get(n);
					map.get(tec).remove(n);
					addToLayer(n, tec + 1);
					set.put(n, tec + 1);
				}
				addToLayer(new_n, addEd);
			}
		}
		return graph;
	}

	public Graph<V, E> evolve2(int step, Graph<V, E> graph, Collection<V> listV) {
		maxlayer = 0;
		graph.getEdgeCount();
		for (Iterator<V> iterator = graph.getVertices().iterator(); iterator.hasNext();) {
			V v = iterator.next();
			addToLayer(v, graph.degree(v));
		}
		// ---------------
		for (int i = 0; i < step; i++) {
			V new_n = vertexFactory.create();
			V new_n2 = vertexFactory.create();

			Map<V, Integer> set = new HashMap<V, Integer>();
			List<V> list = new ArrayList<V>();
			double s = 0.;
			double r = mRand.nextDouble();
			int addEd = 0;
			for (int j = 0; j < numEdgesToAttachMonad.length; j++) {
				s = s + numEdgesToAttachMonad[j];
				if (s > r) {
					addEd = j;
					break;
				}
			}
			if (addEd > 0)
				do {
					int k = getLayer();
					V n = map.get(k).get(mRand.nextInt(map.get(k).size()));
					set.put(n, new Integer(k));
					list.add(n);
				} while (list.size() != addEd);

			Map<V, Integer> set2 = new HashMap<V, Integer>();
			List<V> list2 = new ArrayList<V>();
			int addEd2 = addEd - 1;
			double alpha = 0.1;
			if (addEd2 > 0)
				do {
					V n2 = null;
					if (Math.random() < alpha)
						n2 = list2.get(mRand.nextInt(list2.size()));
					else {
						int k2 = getLayer();
						n2 = map.get(k2).get(mRand.nextInt(map.get(k2).size()));
						set.put(n2, new Integer(k2));
					}
					list2.add(n2);
				} while (list2.size() != addEd2);

			graph.addVertex(new_n);
			for (V n : list) {
				graph.addEdge(edgeFactory.create(), new_n, n);
				int tec = set.get(n);
				map.get(tec).remove(n);
				addToLayer(n, tec + 1);
				set.put(n, tec + 1);
			}
			addToLayer(new_n, addEd);
			graph.addVertex(new_n2);
			for (V n2 : list2) {
				graph.addEdge(edgeFactory.create(), new_n2, n2);
				int tec = set.get(n2);
				map.get(tec).remove(n2);
				addToLayer(n2, tec + 1);
				set.put(n2, tec + 1);
			}
			addToLayer(new_n2, addEd);
		}
		return graph;
	}

	public int maxlayer = 0;

	// ------------------
	private void addToLayer(V n, int i) {
		List<V> list = map.get(i);
		if (list == null) {
			list = new LinkedList<V>();
			if (maxlayer < i)
				maxlayer = i;
			map.put(i, list);
		}
		if (!list.contains(n))
			list.add(n);
	}

	// ------------------
	private int getLayer() {
		int k = 0;
		double rand = mRand.nextDouble();
		double tr = 0;
		double sum = 0.0;
		for (int op : map.keySet())
			sum = sum + attachRule.f(op) * map.get(op).size();
		for (int l : map.keySet()) {
			int A = map.get(l).size();
			tr = tr + ((double) A * attachRule.f(l)) / sum;
			if (rand < tr) {
				k = l;
				break;
			}
		}
		if (k == 0) {
			new Exception("Big numbers");
		}
		return k;

	}
}
