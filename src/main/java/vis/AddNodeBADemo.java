
package vis;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.util.Graphs;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;


/**
 * Demonstrates visualization of a graph being actively updated.
 *
 * @author danyelf, MNYudina
 */
public class AddNodeBADemo extends javax.swing.JApplet {
	static Factory<Integer> vertexFactory = new Factory<Integer>() { 
		int i = 0;

		public Integer create() {
			return new Integer(i++);
		}
	};
	static GenNonIntBA genBA;
	static Factory<Integer> edgeFactory = new Factory<Integer>() { 
		int i = 0;

		public Integer create() {
			return new Integer(i++);
		}
	};

	private Graph<Number, Number> g = null;

	private VisualizationViewer<Number, Number> vv = null;

	private AbstractLayout<Number, Number> layout = null;

	Timer timer;

	boolean done;

	protected JButton switchLayout;

//    public static final LengthFunction<Number> UNITLENGTHFUNCTION = new SpringLayout.UnitLengthFunction<Number>(
//            100);
	public static final int EDGE_LENGTH = 100;

	static DirectedSparseGraph<Number, Number> seed_graph() {
		DirectedSparseGraph<Number, Number> gr = new DirectedSparseGraph<Number, Number>();
		for (int i = -1; i > -4; i--) {
			Integer n = new Integer(i);
			gr.addVertex(n);
		}
		int l = -1;
		Object[] mass = gr.getVertices().toArray();
		for (int i = 0; i < mass.length - 1; i++)
			for (int j = i + 1; j < mass.length; j++)
				if (i != j)
					gr.addEdge(new Integer(l--), (Integer) mass[i], (Integer) mass[j]);

		return gr;
	}

	static PrefferentialAttachment paBA = new PrefferentialAttachment() {
		@Override
		public double f(int k) {
			return k;
		}

		@Override
		public int getM() {
			// TODO Auto-generated method stub
			return 0;
		}
	};

	@Override
	public void init() {

		// create a graph

		Graph<Number, Number> ig = Graphs.<Number, Number>synchronizedDirectedGraph(seed_graph());
		double[] r_BA = new double[] { 0., 0, 1 };

		genBA = new GenNonIntBA(vertexFactory, edgeFactory, r_BA, paBA);

		ObservableGraph<Number, Number> og = new ObservableGraph<Number, Number>(ig);
		og.addGraphEventListener(new GraphEventListener<Number, Number>() {

			public void handleGraphEvent(GraphEvent<Number, Number> evt) {
				System.err.println("got " + evt);

			}
		});
		this.g = og;
		// create a graphdraw
		layout = new FRLayout2<Number, Number>(g);
//        ((FRLayout)layout).setMaxIterations(200);

		vv = new VisualizationViewer<Number, Number>(layout, new Dimension(600, 600));

		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(java.awt.Color.WHITE);
		getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));
		vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<Number, Paint>() {
			
			@Override
			public Paint transform(Number arg0) {
				int i = (Integer)arg0;
				if (i < 0)
					return Color.BLACK;
				if (i == g.getVertexCount()-4)
					return Color.RED;

				return Color.GREEN;
			}
		});
		vv.getModel().getRelaxer().setSleepTime(1000);
		vv.setGraphMouse(new DefaultModalGraphMouse<Number, Number>());
		//vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		//vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Number>());
		vv.setForeground(Color.WHITE);
		getContentPane().add(vv);
		switchLayout = new JButton("Switch to SpringLayout");
		switchLayout.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent ae) {
				Dimension d = new Dimension(600, 600);
				if (switchLayout.getText().indexOf("Spring") > 0) {
					switchLayout.setText("Switch to FRLayout");
					layout = new SpringLayout<Number, Number>(g, new ConstantTransformer(EDGE_LENGTH));
					layout.setSize(d);
					vv.getModel().setGraphLayout(layout, d);
				} else {
					switchLayout.setText("Switch to SpringLayout");
					layout = new FRLayout<Number, Number>(g, d);
					vv.getModel().setGraphLayout(layout, d);
				}
			}
		});

		getContentPane().add(switchLayout, BorderLayout.SOUTH);

		timer = new Timer();
	}

	@Override
	public void start() {
		validate();
		// set timer so applet will change
		timer.schedule(new RemindTask(), 3000, 3000); // subsequent rate
		vv.repaint();
	}

	Integer v_prev = null;

	public void process() {

		try {

			if (g.getVertexCount() < 100) {
				layout.lock(true);
				// add a vertex
				Integer v1 = new Integer(g.getVertexCount());

				Relaxer relaxer = vv.getModel().getRelaxer();
				relaxer.pause();

				// v_prev = v1;

				genBA.evolve(1, g);

				layout.initialize();
				relaxer.resume();
				layout.lock(false);
			} else {
				done = true;
			}

		} catch (Exception e) {
			System.out.println(e);

		}
	}

	class RemindTask extends TimerTask {

		@Override
		public void run() {
			process();
			if (done)
				cancel();

		}
	}

	public static void main(String[] args) {
		AddNodeBADemo and = new AddNodeBADemo();
		JFrame frame = new JFrame("Barabasi - Albert graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(and);
		and.init();
		and.start();
		frame.pack();
		frame.setVisible(true);
	}
}