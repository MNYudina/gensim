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
public class StohIncrementNPAGenerator<V, E> implements GraphGenerator<V, E> {

	private int numVertices;
	private Random mRandom = new Random();

	private double[] r_mon,r_di;
	double gamma, P;
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
	 * @param e 
	 * @param d 
	 * @param r_di 
	 * @param numVertices   - число в графе
	 * @param m             - параметр генератора графа
	 */
	public StohIncrementNPAGenerator(Factory<Graph<V, E>> graphFactory, Factory<V> vertexFactory, Factory<E> edgeFactory, double[] r_mon, double[] r_di, 
			Function<Integer, Double> f, double gamma, double P, int numVertices) {

		this.graphFactory = graphFactory;
		this.vertexFactory = vertexFactory;
		this.edgeFactory = edgeFactory;
		this.numVertices = numVertices;
		this.r_mon = r_mon;
		this.r_di = r_di;
		this.gamma = gamma;
		this.P = P;
		this.f=f;
	}

	@Override
	public Graph<V, E> create() {
		layers = new HashMap();
		g=createSeedGrh(9);
		do {
			double sum = getSum();
			if (Math.random() < gamma) {
				V a = vertexFactory.create();
				V b = vertexFactory.create();

				int addEd_b = getM(r_di);
				int addEd_a = addEd_b;
				addEd_b--;
				
				// выбираю вершины для присоединения ведомой вершины
				Set<V> set_B = new HashSet<V>();
				Set<V> set_A = new HashSet<V>();

				if (addEd_b > 0)
					do {
						List<V> l = getRandomLayer(sum);
						V n = l.get(mRandom.nextInt(l.size()));
						//set_B.add(n);
						if (set_B.add(n)&&Math.random() < P)
							set_A.add(n);
					} while (set_B.size() != addEd_b);

				// выбираю вершины для присоединения ведущей вершины
				if (addEd_a > 0)
					 do {
						List<V> l = getRandomLayer(sum);
						V n2 = l.get(mRandom.nextInt(l.size()));
						set_A.add(n2);
					}while (set_A.size() != addEd_a);
				

				// добавляю первую вершину
				addVertex(b);
				for (V n : set_B) {
					addEdge(b, n);
				}
				// добавляю вторую вершину
				addVertex(a);
				for (V n2 : set_A) {
					addEdge(a, n2);
				}
				// добавляю связь между двумя вершинами
				addEdge(b, a);

			} else {
				int m = getM(r_mon);
				V newVertex = vertexFactory.create();
				Set<V> setV = new HashSet();
				do {
					List<V> l = getRandomLayer(sum);
					V v = l.get(mRandom.nextInt(l.size()));
					setV.add(v);
				} while (setV.size() < m);
				addVertex(newVertex);
				for (V v : setV) {
					addEdge(newVertex, v);
				}
			}
		}
		while(g.getVertexCount()<numVertices);
		
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

	private int getM(double[] mass) {
		double s=0.;  
		double r=mRandom.nextDouble();    
		for (int j = 0; j < mass.length; j++) {
				s=s+mass[j];
				if(s>r)		return j;
		 }
		throw new IllegalMonitorStateException("Проблемы при генерации слоев");
	}

	private double getSum() {
		double sum = 0.;
		for (Integer k : layers.keySet()) {
			sum = sum + layers.get(k).size() * f.apply(k);
		}
		return sum;
	}

	private List<V> getRandomLayer(double sum) {
		double rand = Math.random();
		double comul = 0;
		for (Integer k : layers.keySet()) {
			double p = layers.get(k).size() * f.apply(k) / sum;
			comul = p + comul;
			if (rand < comul)
				return layers.get(k);
		}
		throw new IllegalMonitorStateException("Проблемы при генерации слоев");
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
