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
 * <p>The accelerated "preferential attachment" random graph generator. At each time
 * step, a new vertex is created and is connected to existing vertices
 * according to the principle of "preferential attachment", whereby 
 * vertices with higher degree have a higher probability of being 
 * selected for attachment.</p>
 * 
 * <p>At a given timestep, the probability <code>p</code> of creating an edge
 * between an existing vertex <code>v</code> and the newly added vertex is
 * <pre>
 * p = f(degree(v)) / Summ_j f(degree(v_j));
 * </pre></p>
 * 
 * <p>where <code>Summ_j f(degree(v_j))</code> is sum of given "preferential attachment" functions 
 * of the degree of connectivity of vertices </p>

 * <p> All nodes with the specified connection are stored in one  map's slot(layers),
 *  thereby accelerating the generation algorithm. The layer is specified node's degree. 
 *  The probability Qk of choosing such a layer k  is 
 * 	Qk=|A|/ Summ_j f(degree(v_j));
 * 	where |A| is the number of vertexes in layers of k  
 *  After the layer is selected nodes are getting randomly from it</p>
 *      
 * <p>The graph created may be either directed or undirected (controlled by type the given Graph  
 * If the graph is directed, then the edges added will be directed
 * from the newly added vertex u to the existing vertex v</p> 
 * <p>The <code>parallel</code> constructor parameter specifies whether parallel edges
 * may be created.</p>
 * 
 * @see "V.Zadorozhniy and E.Yudin, Definition, generating and application of statistical homogeneity random graphs
 * Herald of Omsk Science №3(83), 2009.(in Russian)"
 * @author  Eugene Yudin
 * @author  Vladimir Zadorozhniy
 * 
 */
public class GenNonIntBA<V,E> {
	 Map<Integer, List<V>> map = new HashMap<Integer, List<V>>();
	protected PrefferentialAttachment attachRule;
	protected Factory<V> vertexFactory;
	protected Factory<E> edgeFactory;
	protected double numEdgesToAttach[];
	private Random mRand;
	boolean parallel = false;

/**
 * 
     * Create an instance of the generator, with which you can
	 * on the basis of the graph-seed to grow by "preferential attachment" the graph   
     * @param vertexFactory defines a way to create nodes
     * @param edgeFactory defines a way to create edges
     * @param probEdgesToAttach the number of edges that should be attached
     * @param attachRule is interface specifying method which use in rule of "preferential attachment"
     * 
 */
	public GenNonIntBA(Factory<V> vertexFactory,
			Factory<E> edgeFactory, double[] probEdgesToAttach, PrefferentialAttachment attachRule)
	{	
		double s=0.;
		for (double d : probEdgesToAttach) {
			s=s+d;
		}
		assert Math.abs(s-1.)<0.000000001 : "Сумма вероятностей по различным значениям "
			+ "числа добавляемых на шаге ребер должна равняться 1";

		this.vertexFactory = vertexFactory;
		this.edgeFactory = edgeFactory;
		this.numEdgesToAttach= probEdgesToAttach;
		this.attachRule =attachRule;
		mRand = new Random();
	}
	/**
	 * 
	 * Create an instance of the generator, with which you can
	 * on the basis of the graph-seed to grow by "preferential attachment" the graph 	
	 * and which will use the current time as a seed for the random number generation.
	 * @param vertexFactory defines a way to create nodes
	 * @param edgeFactory  defines a way to create edges
	 * @param numEdgesToAttach the number of edges that should be attached
	 * @param parallel specifies whether the algorithm permits parallel edges
	 * @param seed random number seed
	 * @param attachRule is interface specifying method which use in rule of "preferential attachment"
	 */
	public GenNonIntBA(Factory<V> vertexFactory,
			Factory<E> edgeFactory, double[] numEdgesToAttach, boolean parallel, int seed, PrefferentialAttachment attachRule)
	{
		this(vertexFactory,
				edgeFactory, numEdgesToAttach,attachRule);
		this.parallel=parallel;
		mRand = new Random(seed);
		
	}
	
	public Graph<V, E> evolve(int step, Graph<V, E> graph) {
		maxlayer=0;
		graph.getEdgeCount();
		for (Iterator<V> iterator = graph.getVertices().iterator(); iterator.hasNext();) {
			V v =  iterator.next();
			addToLayer(v, graph.degree(v));
		}
		// ---------------
		for (int i = 0; i < step; i++) {
			V new_n = vertexFactory.create();
			Map<V, Integer> set = new HashMap<V, Integer>();
			Set<V> list = new HashSet<V>();
			// разыгрываю случайную величину
			double s=0.;
			double r=mRand.nextDouble();
			int addEd =0;
			for (int j = 0; j < numEdgesToAttach.length; j++) {
					s=s+numEdgesToAttach[j];
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
				// переношу в соответв слой новую вершину
				int tec = set.get(n);
				map.get(tec).remove(n);
				addToLayer(n, tec + 1);
				set.put(n, tec + 1);
			}
			addToLayer(new_n, addEd);
			// случайно удаляю дугу 
		/*	if(mRand.nextDouble()<-0.7){
				//List<E> l = new ArrayList(graph.getEdges());
				//E e =l.get(mRand.nextInt(l.size()));
				E e=null;
				int n=mRand.nextInt(graph.getEdges().size());
				Iterator<E> it=graph.getEdges().iterator();
				while (it.hasNext()) {
					if(n--==0)
					    {e =  it.next(); break;}
				}
				
				V v1=graph.getDest(e);
				V v2=graph.getSource(e);
				map.get(graph.degree(v1)).remove(v1);
				map.get(graph.degree(v2)).remove(v2);
				graph.removeEdge(e);

				addToLayer(v1, graph.degree(v1));
				addToLayer(v2, graph.degree(v2));
			
			}*/
		}
	
		
		return graph;
	}
	
	public Graph<V, E> evolve2(int step, Graph<V, E> graph, Collection<V> listV) {
		maxlayer=0;
		graph.getEdgeCount();
		for (Iterator<V> iterator = graph.getVertices().iterator(); iterator.hasNext();) {
			V v =  iterator.next();
			addToLayer(v, graph.degree(v));
		}
		// ---------------
		for (int i = 0; i < step; i++) {
			V new_n = vertexFactory.create();
			Map<V, Integer> set = new HashMap<V, Integer>();
			List<V> list = new ArrayList<V>();
			// разыгрываю случайную величину
			double s=0.;
			double r=mRand.nextDouble();
			int addEd =0;
			for (int j = 0; j < numEdgesToAttach.length; j++) {
					s=s+numEdgesToAttach[j];
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
				// переношу в соответв слой новую вершину
				int tec = set.get(n);
				map.get(tec).remove(n);
				addToLayer(n, tec + 1);
				set.put(n, tec + 1);
			}
			addToLayer(new_n, addEd);
			//вывод степеней вершин
			for (V v :listV) {
				System.out.print(graph.degree(v)+" ");
			}
			// считаем знаменатель
			double sum = 0.0;
			for (int op : map.keySet())
				sum = sum +  attachRule.f(op) * map.get(op).size();
			System.out.println(" "+sum);
		}
		return graph;
	}
	
	
	public int maxlayer=0;
	//------------------
	private void addToLayer(V n, int i) {
		List<V> list = map.get(i);
		if (list == null) {
			list = new LinkedList<V>();
			if(maxlayer<i)maxlayer=i;
			map.put(i, list);
		}
		if (!list.contains(n))
			list.add(n);
	}
	
	//------------------
	private int getLayer() {
		// разыгрываем
		int k = 0;
		double rand = mRand.nextDouble();
		double tr = 0;
		// считаем знаменатель
		double sum = 0.0;
		for (int op : map.keySet())
			sum = sum +  attachRule.f(op) * map.get(op).size();
		for (int l : map.keySet()) {
			int A = map.get(l).size();
			tr = tr + ((double) A *  attachRule.f(l)) / sum;
			if (rand < tr) {
				k = l;
				break;
			}
		}
		if(k==0){
			new Exception("Big numbers");
		}
		return k;
		//-----------пример использования для моделирования взаимодействия атомов -------
		//----биметаллической частицы в форме усечённого октаэдра -------------------

	}
}
