package yudinev.graph.projects.gensim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;

/**
 * граф Барабаши-Альберт (Shrek 2) использует генерацию дискретной случайной
 * величины для выбора очередной вершины для присоединения
 * 
 * @author Shrek
 * 
 * @param <V> вершины
 * @param <E> ребра
 */
public class PAGenerator<V, E> implements GraphGenerator<V, E> {

	private int numVertices;
	private Random mRandom = new Random();

	private double[] mass;
	private Factory<Graph<V, E>> graphFactory;
	private Factory<V> vertexFactory;
	private Factory<E> edgeFactory;
	Function<Integer, Double> f;
	private Graph<V, E> g;
	private Map<Integer, List<V>> layers;

	/**
	 * 
	 * @param graphFactory  - фабрика графов
	 * @param vertexFactory - фабрика вершины
	 * 
	 * @param edgeFactory   - фабрика ребер
	 * @param numVertices   - число в графе
	 * @param m             - параметр генератора графа
	 */
	public PAGenerator(Factory<Graph<V, E>> graphFactory, Factory<V> vertexFactory, Factory<E> edgeFactory, double[] mass, Function<Integer, Double> f,
			int numVertices) {

		this.graphFactory = graphFactory;
		this.vertexFactory = vertexFactory;
		this.edgeFactory = edgeFactory;
		this.numVertices = numVertices;
		this.mass = mass;
		this.f=f;;

	}

	@Override
	public Graph<V, E> create() {
		layers = new HashMap();
		g=createSeedGrh(9);
		int[] tr0= Statistics.getTriAndVilk2(g);
		System.out.println("initial"+tr0[0] * 3. / tr0[1]);

		do {
			int m=getM();
			V newVertex = vertexFactory.create();
			Set<V> setV = new HashSet();
			double sum = getSum();
			do {
				List<V> l = getRandomLayer(sum);
				V v = l.get(mRandom.nextInt(l.size()));
				setV.add(v);
				
				
			} while (setV.size() < m);
			addVertex(newVertex);
			for (V v : setV) {
				addEdge(newVertex,v);
			}
			
			if(g.getVertexCount()%5000==0) {
				int[] tr = Statistics.getTriAndVilk2(g);
				System.out.println(""+tr[0] * 3. / tr[1]);
			}

		}while(g.getVertexCount()<numVertices);
		
		return g;
	}

	private Graph<V, E> createSeedGrh(int m) {
		g = graphFactory.create();
		V[] mass = (V[]) new Object[m];
		for (int i = 0; i < mass.length; i++) {
			mass[i] = vertexFactory.create();
			addVertex(mass[i]);
		}
		for (int i = 0; i < mass.length - 1; i++) {
			for (int j = i + 1; j < mass.length; j++) {
				addEdge(mass[i], mass[j]);
			}
		}
		return g;
	}

	private void addVertex(V newVertex) {
		g.addVertex(newVertex);
		addToLayer(newVertex);
		
	}

	private void addEdge(V newVertex, V v) {
		layers.get(g.degree(newVertex)).remove(newVertex);
		layers.get(g.degree(v)).remove(v);
		g.addEdge(edgeFactory.create(), newVertex, v);
		addToLayer(newVertex);
		addToLayer(v);
	}

	private int getM() {
		double s=0.;  
		double r=mRandom.nextDouble();    
		int addEd =0;
		for (int j = 0; j < mass.length; j++) {
				s=s+mass[j];
				if(s>r)
				{addEd =	j;return addEd;	}
		 }
		return addEd;
	}

	private double getSum() {
		double sum = 0.;
		for (Integer k : layers.keySet()) {
			sum = sum + layers.get(k).size() * f.apply(k);
		}
		//throw  new IllegalStateException("Проблема при генерации числа ребер в приращении");
		return sum;
	}

	private List<V> getRandomLayer(double sum) {
		double d = Math.random();
		double t = 0;
		for (Integer k : layers.keySet()) {
			double pVV = layers.get(k).size() * f.apply(k) / sum;
			t = pVV + t;
			if (d < t)
				return layers.get(k);
		}
		throw  new IllegalStateException("Проблема при выборе слоя для приращения");
	}

	private void addToLayer(V v) {
		List<V> list = layers.get(g.degree(v));
		if (list == null) {
			list = new LinkedList();
			layers.put(g.degree(v), list);
		}
		list.add(v);
	}

	

}
