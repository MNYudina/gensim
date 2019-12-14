package yudinev.graph.projects.gensim;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yudinev
 */
public class ProgramParameters {

    private Map<String, Boolean> operations;
    
    private String graphFile;

    private int eDistr;
    
    private int vDistr;
    
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
    
    
	
	public String getGraphFile() {
        return graphFile;
    }

    public void setGraphFile(String graphFile) {
        this.graphFile = graphFile;
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
    
}