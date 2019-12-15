package yudinev.graph.projects.gensim;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;

/**
 * @author yudinev
 */
public class ProgramParameters {

    private Map<String, Boolean> operations;
    
    GraphType type;

    private int eDistr=0;
    
    private int vDistr=0;
    
    public ProgramParameters() {
    	operations = new HashMap<String, Boolean>();
    	operations.put("isNodeRemovalRequested", false);
    	operations.put("isEdgeRemovalRequested", false);
    	operations.put("isSISRequested", false);
	}
   
    
	public void setIsNodeRemovalSimulationFlag() {
    	operations.put("isNodeRemovalRequested", true);
    }
    
    
    public void setIsEdgeRemovalSimulationFlag() {
    	operations.put("isEdgeRemovalRequested", true);
    }
    
    
    public void setIsSISSimulationFlag() {
    	operations.put("isSISRequested", true);
    }
    
    
	

    public int getVDistr() {
		return vDistr;
	}
    
    public void setVDistr(int vDistr) {
        this.vDistr = vDistr;
    }

    public int getEDistr() {
        return eDistr;
    }

    public void setEDistr(int eDistr) {
        this.eDistr = eDistr;
    }


	public void setTypeGraph(GraphType type) {
        this.type = type;

	}
    
}