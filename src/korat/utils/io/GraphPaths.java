package korat.utils.io;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.collections15.Transformer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import java.awt.geom.Ellipse2D;

import korat.FSMModel;

/* Used to represent classical (non-FSM) exploration graph
 */
class GfxPath {
	String fromnode;
	String tonode;
	String relation;
	
	GfxPath (String f, String t, String r) {
		fromnode=f;
		tonode=t;
		relation=r;
	}	
}


/* Used to represent FSM exploration graph
 */
class GfxFSMPath {
	String idx;
	String type;
	String label;
	
	GfxFSMPath (String i, String t, String l) {
		idx=i;
		type=t;
		label=l;
	}	
}

class Node {
	public String label;
	
	public Node (String s) {
		label = s;
	}
	
    @Override
	public String toString() {
		return (label);
	}
	
    @Override
    public boolean equals(Object o) {
    	Node n = (Node) o;

        if (n != null) {
            return (this.label.equals(n.label));
        }

        return false;
    }
    
    @Override
    public int hashCode() {
        return (label.hashCode());
    }

}

/* Contains easily usable information to draw graph
 */
class Edge {
	public Node start;
	public Node end;
	public String label;

	public Edge (Node s, Node e, String l) {
		start = s;
		end = e;
		label = l;
	}
	
    @Override
	public String toString() {
		return (label);
	}
	
    @Override
    public boolean equals(Object o) {
    	Edge e = (Edge) o;
        if (e != null) {
            return ((this.start.equals(e.start)) &&
            		(this.end.equals(e.end)) &&
            		(this.label.equals(e.label)));
        }

        return false;
    }
    
    @Override
    public int hashCode() {
        //return (start.hashCode()+end.hashCode()+label.hashCode());
        return ((start.toString()+end.toString()+label.toString()).hashCode());
    }

}

class VertexSize<V,E> implements Transformer<Object,Shape> {
	public Shape transform(Object o){
		//Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
		Ellipse2D circle = new Ellipse2D.Double(-15, -15, 50, 50);
		return circle;
	}
};

class VertexPainter<V, E> implements Transformer<Object, Paint> {
	ArrayList<Node> marked_vertex = new ArrayList<Node>();

	public VertexPainter() {
		super();
	}
	
	public void add (Node n) {
		marked_vertex.add(n);
	}

	public Paint transform(Object o) {
		if (o instanceof Node) {
			Node n = (Node) o;
			if (marked_vertex.contains(n)) {
				return Color.GREEN;
			} else {
				return Color.WHITE;
			}
		}
		return Color.RED;
	}
}

class EdgePainter<V, E> implements Transformer<Object, Paint> {
	ArrayList<Edge> marked_edge = new ArrayList<Edge>();
	float dash[] = { 10.0f };

	public EdgePainter() {
		super();
	}
	
	public void add (Edge e) {
		marked_edge.add(e);
	}

	public Paint transform(Object o) {
		if (o instanceof Edge) {
			Edge e = (Edge) o;

			if (marked_edge.contains(e)) {
				return Color.GREEN;
			} else {
				return Color.BLACK;
			}
		}
		return Color.RED;
	}
}

public class GraphPaths {
	static final int MAX_EDGES = 20;
	// ["3","from_state","2","3","to_state","0","3","in_transition","2"]
	static final int IDX_INDEX = 0;
	static final int IDX_SSTATE = 2;
	static final int IDX_ESTATE = 5;	
	static final int IDX_TRANSITION = 8;
	
	//ArrayList<GPathElem> GPathElements;
	ArrayList<GfxFSMPath> gfxpfsm;
	ArrayList<GfxPath> gfxp;
    public static String JSON_FILE= "GFX.kjson";
    
	static boolean isFSM, isCLASSIC;

	//FileWriter outGfx = null;
	static ArrayList<ArrayList<String>> AllFSMSavedPaths = new ArrayList<ArrayList<String>> ();
	static ArrayList<ArrayList<GfxPath>> AllSavedPaths = new ArrayList<ArrayList<GfxPath>> ();
	Edge[] AllEdges = new Edge[MAX_EDGES];
	
	private DirectedSparseMultigraph<Object, Object> Graph;

	public GraphPaths () {

	}
	
	/* Used to gather GFX* files related to non-FSM exploration
	 */
	public File[] fetchFilesByWildCard (String dir) {
		File d = new File (dir);
		File[] files = d.listFiles( new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name ) {
				return name.matches( "GFX[0-9]+\\.kjson" );
			}
		} );
		return files;
	}
	
	/* Deletes json files and internal graph structures
	 */
	public void fInit() {
		AllFSMSavedPaths.clear();
		AllSavedPaths.clear();
		
		// create json directory
		File file = new File(korat.Korat.JSON_DIR);
		if (!file.exists()) {
			file.mkdir();
		} else {
			File fl = new File (korat.Korat.JSON_DIR+"/"+JSON_FILE);
			fl.delete();
			
			// delete files GFX*
			File[] files = fetchFilesByWildCard(korat.Korat.JSON_DIR+"/");
			for (File f : files ) {
				f.delete();
			}	
		}
	}
	
	/* Internal graph structure for FSM exploration 
	 */
	public void initFSMGfxPath() {
		gfxpfsm = new ArrayList<GfxFSMPath>();

	}
	
	/* Internal graph structure for non-FSM exploration 
	 */
	public void initGfxPath() {
		gfxp = new ArrayList<GfxPath>();

	}

	/* Internal exploration structure for path information that is added
	 * when a new exploration is found during non-FSM space analysis.
	 */
	public boolean addGfxPath (String f, String t, String r) {	

		// avoid adding duplicates
		for (GfxPath p: gfxp) {
			if ((p.fromnode.equals(f)) &&
					(p.tonode.equals(t)) &&
					(p.relation.equals(r))) {
				
				return false;
			}				
		}		
		gfxp.add(new GfxPath(f, t, r));
		return true;
	}
	
	/* Internal exploration structure for path information that is added
	 * when a new exploration is found during FSM state space analysis.
	 */
	public boolean addFSMGfxPath (String idx, String type, String label) {			
		// avoid adding duplicates
		for (GfxFSMPath p: gfxpfsm) {
			if ((Integer.parseInt(p.idx) == Integer.parseInt(idx)) &&
					(p.type.equals(type)) &&
					(p.label.equals(label))) {
				
				return false;
			}				
		}		
		gfxpfsm.add(new GfxFSMPath(idx, type, label));
		return true;
	}
	
	/* Used when navigating the "next"  button, to check if the end of graphs is reached. 
	 */
	public int getMaxIdx () {
		if (isFSM) {
			return (AllFSMSavedPaths.size());
		} else if (isCLASSIC) {
			return (AllSavedPaths.size());
		} else {
			// do nothing
			return 0;
		}
	}

	/* Check the current model being analyzed, based on presence of json files
	 * returns 0=CLASSIC; 1=FSM; 2=neither
	 */
	public int checkModel (String dir) {
		int isModel = 2; 
		File file;
		file = new File(dir+"/"+JSON_FILE);
		if (file.exists()) {
			isModel=1;
		} else {
			File[] files = fetchFilesByWildCard(dir);
			if (files.length>0)
				isModel=0;
			else
				isModel=2;
		}
		//System.out.println("ARP"+isModel);
		return isModel;
	}

	/* Read json file into the appropriate internal exploration structure depending
	 * on whether classical or FSM model is being analyzed.
	 */
	public boolean freadPathsJson () {
		Gson gson = new Gson();
		BufferedReader inGfx=null;
		String inline = null;
		
		isFSM = (korat.Korat.JSONLoaded && (checkModel(korat.Korat.JSONLoadedDir) == 1)) || 
				(!korat.Korat.JSONLoaded && (new FSMModel().isFSMModel()));
		
		isCLASSIC = (korat.Korat.JSONLoaded && (checkModel(korat.Korat.JSONLoadedDir) == 0)) || 
				(!korat.Korat.JSONLoaded && (!(new FSMModel().isFSMModel())));
		
		AllFSMSavedPaths.clear();
		AllSavedPaths.clear();

		try {			
			//if (new FSMModel().isFSMModel()) {
			if (isFSM) {
				ArrayList<String> fpe = new ArrayList<String>();
				inGfx = new BufferedReader(new FileReader(korat.Korat.JSONLoadedDir+"/"+JSON_FILE));
				//System.out.println("AAAPP"+dir+"/"+JSON_FILE);

				while ((inline=inGfx.readLine()) != null) {
					fpe=gson.fromJson(inline, new TypeToken<ArrayList<String>>(){}.getType());
					AllFSMSavedPaths.add(fpe);
				}
				inGfx.close();
				
			} else if (isCLASSIC) {
				ArrayList<GfxPath> fpe = new ArrayList<GfxPath>();

				File[] files = fetchFilesByWildCard(korat.Korat.JSONLoadedDir);

				for (File file : files ) {
					inGfx = new BufferedReader(new FileReader(file));
					if ((inline=inGfx.readLine()) != null) {
						fpe=gson.fromJson(inline, new TypeToken<ArrayList<GfxPath>>(){}.getType());
						AllSavedPaths.add(fpe);
					}
					inGfx.close();
				}	
			} else {
				// do nothing
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 

		return true;

	}
	
	/* Save json file from the appropriate internal exploration structure depending
	 * on whether classical or FSM model is being analyzed.
	 */
	public boolean fsavePathsJson (String dir) {
		Gson gson = new Gson();
		BufferedWriter outGfx = null;
		int idx=0;
		
		try {			
			if (new FSMModel().isFSMModel()) {
				outGfx = new BufferedWriter(new FileWriter(dir+"/"+JSON_FILE, false));
				for (ArrayList<String> f:AllFSMSavedPaths) {
					outGfx.write(gson.toJson(f.toArray(new String[f.size()])));
					outGfx.newLine();
				}
				outGfx.close();
			} else {
				for (ArrayList<GfxPath> f:AllSavedPaths) {
					outGfx = new BufferedWriter(new FileWriter(dir+"GFX" + Integer.toString(idx) + ".kjson", false));
					outGfx.write(gson.toJson(f.toArray(new GfxPath[f.size()])));
					outGfx.newLine();
		            outGfx.close();
		            idx ++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return true;
	}
	
	/* Used to save bits of graph in json file as and when a new 
	 * non-FSM (classical) exploration path is discovered.
	 */
	public void saveGfxPathJson (int idx) {
		Gson gson = new Gson();
    	BufferedWriter outGfx=null;
        
        try {
        	outGfx = new BufferedWriter(new FileWriter(korat.Korat.JSON_DIR+"/"+"GFX" + Integer.toString(idx) + ".kjson", false));
			AllSavedPaths.add(gfxp);
			outGfx.write(gson.toJson(gfxp.toArray(new GfxPath[gfxp.size()])));
			outGfx.newLine();      
            outGfx.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	/* Used to save bits of graph in json file as and when a new 
	 * FSM exploration path is discovered.
	 */
	public boolean saveFSMGfxPathJson (int maxid) {
		ArrayList<String> f = new ArrayList<String>();
		Gson gson = new Gson();
		BufferedWriter outGfx = null;

		try {
			outGfx = new BufferedWriter(new FileWriter(korat.Korat.JSON_DIR+"/"+JSON_FILE, true));
			
			// gather full paths i.e start node, end node and edge name
			for (int i=0; i<maxid; i++) {
				f.clear();
				
				for (GfxFSMPath p: gfxpfsm) {
					if (Integer.parseInt(p.idx) == i) {
						f.add(p.idx);
						f.add(p.type);
						f.add(p.label);
					}	
				}
				
				if ((f !=null) && (f.size() != 0)) {
					AllFSMSavedPaths.add(f);
					outGfx.write(gson.toJson(f.toArray(new String[f.size()])));
					outGfx.newLine();
				}
			}
			outGfx.close();

		} catch (IOException e) {
			e.printStackTrace();
		} 	

		return true;
	}

	/* Converts from internal exploration structure to edge structure
	 * that is easily usable to draw FSM graphs.
	 */
	public void paths2EdgesFSM () {
		int idx;
		String[] fstr;

		for (int i=0; i<MAX_EDGES;i++) //clear all
			AllEdges[i] = null;
		
		for (ArrayList<String> f:AllFSMSavedPaths) {
			fstr = f.toArray(new String[f.size()]);
			idx = Integer.parseInt(fstr[IDX_INDEX]);		
			AllEdges[idx] = new Edge(new Node(fstr[IDX_SSTATE]), new Node(fstr[IDX_ESTATE]), fstr[IDX_TRANSITION]);
		}				
	}
	
	/* Converts from internal exploration structure to edge structure
	 * that is easily usable to draw non-FSM graphs.
	 */
	public void paths2Edges (int idx) {
		GfxPath[] fp;
		ArrayList<GfxPath> fpa;

		for (int i=0; i<MAX_EDGES;i++) //clear all
			AllEdges[i] = null;
		
		fpa = AllSavedPaths.get(idx);
		fp = fpa.toArray(new GfxPath[fpa.size()]);

		int i =0;
		for (GfxPath g: fp) {
			AllEdges[i] = new Edge(new Node(g.fromnode), new Node(g.tonode), g.relation);
			i ++;
		}	
	}

	public DirectedSparseMultigraph<Object, Object> emptyGraph() {
        Graph = new DirectedSparseMultigraph<Object, Object>();

		return Graph;
	}


	/* This Graph info is displayed as additional text description 
	 * for the graph.
	 */
	public String getGraphInfo (VisualizationViewer<Object, Object> vv, int idx) {
		
		if (isFSM) {
	        //paths2EdgesFSM();
	        if ((idx == -1) || AllEdges[idx] == null ){ // plain graph
	        	return (" ");
	        } else {
	            return ("From State: "+AllEdges[idx].start.toString() + ", Trigger: " + AllEdges[idx].label.toString() + ", To State: " + AllEdges[idx].end.toString());
	        }

		} else if (isCLASSIC) {
			if (idx == -1) 
				return ("Exploration# " + Integer.toString(idx+2));
			else
				return ("Exploration# " + Integer.toString(idx+1));
		} else {
			return (" ");
		}

	}
	
	
	/* In FSM graph, draw plain graph when idx = -1, else highlight the edge corresponding to idx
	 * In nonf-FSM graph, draw the complete explored graphs in sequence indexed by idx 
	 */
	public DirectedSparseMultigraph<Object, Object> drawGraph(VisualizationViewer<Object, Object> vv, int idx) {
        Graph = new DirectedSparseMultigraph<Object, Object>();
        Node tsnode, tenode;
        VertexPainter<Object, Paint> vp;
        EdgePainter<Object, Paint> ep;
        VertexSize<Object, Shape> vz;
        
        if (idx == -1) { // plain graph; this is first time so load from json only if not manually imported
        	if (!korat.Korat.JSONLoaded)
        		freadPathsJson(); // current directory
        }
        
		//if (new FSMModel().isFSMModel()) {
        if (isFSM) {
			
	        paths2EdgesFSM();
	        
	        vp= new VertexPainter<Object, Paint>();
	        ep= new EdgePainter<Object, Paint>();
	        vz= new VertexSize<Object, Shape>();

	        if (idx == -1) { // plain graph
	        	
	        } else {
	        	if (AllEdges[idx] != null) {

	        		tsnode = AllEdges[idx].start;
	        		tenode = AllEdges[idx].end;       

	        		vp.add(tsnode);
	        		vp.add(tenode);

	        		ep.add(new Edge (AllEdges[idx].start, AllEdges[idx].end, AllEdges[idx].label));       
	        	}
	        }

	        vv.getRenderContext().setVertexFillPaintTransformer(vp);
	        vv.getRenderContext().setEdgeDrawPaintTransformer(ep);
	    	vv.getRenderContext().setVertexShapeTransformer(vz);
	    	vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Object>());
	    	vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Object>());

	    	vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
	                
	        for (int i=0; i<MAX_EDGES; i++) {
	        	if (AllEdges[i] != null) {
	        		Graph.addVertex(AllEdges[i].start);
	        		Graph.addVertex(AllEdges[i].end);
	        		Graph.addEdge(AllEdges[i], AllEdges[i].start, AllEdges[i].end, EdgeType.DIRECTED);
	        		//System.out.println("ARP~"+AllEdges[i].toString()+"~"+AllEdges[i].start+"~"+AllEdges[i].end);
	        	}
	        }    

		} else if (isCLASSIC) {
			
			int tidx;
			
			tidx = (idx == -1) ? 0: idx; // first graph ?
	        paths2Edges (tidx);

	        vp= new VertexPainter<Object, Paint>();
	        ep= new EdgePainter<Object, Paint>();
	        vz= new VertexSize<Object, Shape>();

	        vv.getRenderContext().setVertexFillPaintTransformer(vp);
	        vv.getRenderContext().setEdgeDrawPaintTransformer(ep);
	    	vv.getRenderContext().setVertexShapeTransformer(vz);
	    	vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Object>());
	    	vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Object>());

	    	vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
	                
	        for (int i=0; i<MAX_EDGES; i++) {
	        	if (AllEdges[i] != null) {
	        		Graph.addVertex(AllEdges[i].start);
	        		Graph.addVertex(AllEdges[i].end);
	        		Graph.addEdge(AllEdges[i], AllEdges[i].start, AllEdges[i].end, EdgeType.DIRECTED);
	        		//System.out.println("ARP~"+AllEdges[i].toString()+"~"+AllEdges[i].start+"~"+AllEdges[i].end);
	        	}
	        }    

		} else {
			//  do nothing
		}


        return Graph;
	}

}

