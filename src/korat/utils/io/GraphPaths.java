package korat.utils.io;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

class GPathElem {
	String idx;
	String type;
	String label;
	
	GPathElem (String i, String t, String l) {
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

	ArrayList<GPathElem> GPathElements;
    public static String JSON_FILE= "./GFX.kjson";
	FileWriter outGfx = null;
	static ArrayList<ArrayList<String>> AllSavedPaths = new ArrayList<ArrayList<String>> ();
	Edge[] AllEdges = new Edge[MAX_EDGES];
	
	private DirectedSparseMultigraph<Object, Object> Graph;

	public GraphPaths () {
		GPathElements = new ArrayList<GPathElem>();

	}
	
	public void fclear() {
		try {
			AllSavedPaths.clear();
			
			outGfx = new FileWriter(JSON_FILE, false); // empty the file
			outGfx.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public boolean addBitPath (String idx, String type, String label) {			
		// avoid adding duplicates
		for (GPathElem p: GPathElements) {
			if ((Integer.parseInt(p.idx) == Integer.parseInt(idx)) &&
					(p.type.equals(type)) &&
					(p.label.equals(label))) {
				
				return false;
			}				
		}		
		GPathElements.add(new GPathElem(idx, type, label));
		return true;
	}

	public ArrayList<String> getFullPath (int x) {
		ArrayList<String> f = new ArrayList<String>();
		for (GPathElem p: GPathElements) {
	    	//System.out.println ("AAB" +x+":"+Integer.parseInt(p.idx));
			if (Integer.parseInt(p.idx) == x) {
				f.add(p.idx);
				f.add(p.type);
				f.add(p.label);
			}				
		}		

		return f;
	}
	
	
	public int getMaxIdx () {
		return (AllSavedPaths.size());

	}

	public boolean freadPathsJson (String file) {
		Gson gson = new Gson();
		BufferedReader inGfx=null;
		String inline = null;
		ArrayList<String> fpe = new ArrayList<String>();
		
		AllSavedPaths.clear();

		try {
			inGfx = new BufferedReader(new FileReader(file));
			while ((inline=inGfx.readLine()) != null) {
				//System.out.println(inline);
				fpe=gson.fromJson(inline, new TypeToken<ArrayList<String>>(){}.getType());
				AllSavedPaths.add(fpe);
			}

			inGfx.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 

		return true;

	}

	public boolean fsavePathsJson (ArrayList<String> fpe) {
		Gson gson = new Gson();
		BufferedWriter outGfx = null;

		AllSavedPaths.add(fpe);

		try {
			outGfx = new BufferedWriter(new FileWriter(JSON_FILE, true));
			outGfx.write(gson.toJson(fpe.toArray(new String[fpe.size()])));
			outGfx.newLine();
			outGfx.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return true;
	}
	
	
	public boolean fsavePathsJson (String jfile) {
		Gson gson = new Gson();
		BufferedWriter outGfx = null;
		
		try {
			outGfx = new BufferedWriter(new FileWriter(jfile, false));
			for (ArrayList<String> f:AllSavedPaths) {
				outGfx.write(gson.toJson(f.toArray(new String[f.size()])));
				outGfx.newLine();
			}
			outGfx.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return true;
	}

	public void paths2Edges () {
		int idx;
		String[] fstr;

		for (ArrayList<String> f:AllSavedPaths) {
			fstr = f.toArray(new String[f.size()]);
			idx = Integer.parseInt(fstr[IDX_INDEX]);		
			AllEdges[idx] = new Edge(new Node(fstr[IDX_SSTATE]), new Node(fstr[IDX_ESTATE]), fstr[IDX_TRANSITION]);
		}				
	}
	
	public DirectedSparseMultigraph<Object, Object> emptyGraph() {
        Graph = new DirectedSparseMultigraph<Object, Object>();

		return Graph;
	}


	public String getGraphInfo (VisualizationViewer<Object, Object> vv, int idx) {
		
        paths2Edges();
        if (idx == -1) { // plain graph
        	return (" ");
        } else {
            return ("From State: "+AllEdges[idx].start.toString() + ", Trigger: " + AllEdges[idx].label.toString() + ", To State: " + AllEdges[idx].end.toString());
        }
	}
	
	// idx=-1 draw plain graph; else draw graph and highlight the edge corresponding to idx
	public DirectedSparseMultigraph<Object, Object> drawGraph(VisualizationViewer<Object, Object> vv, int idx) {
        Graph = new DirectedSparseMultigraph<Object, Object>();
        Node tsnode, tenode;
        VertexPainter<Object, Paint> vp;
        EdgePainter<Object, Paint> ep;
        VertexSize<Object, Shape> vz;
        
        paths2Edges();
        
        vp= new VertexPainter<Object, Paint>();
        ep= new EdgePainter<Object, Paint>();
        vz= new VertexSize<Object, Shape>();

        if (idx == -1) { // plain graph
        	
        } else {
            tsnode = AllEdges[idx].start;
            tenode = AllEdges[idx].end;       

            vp.add(tsnode);
            vp.add(tenode);
            
            ep.add(new Edge (AllEdges[idx].start, AllEdges[idx].end, AllEdges[idx].label));            
        }

        vv.getRenderContext().setVertexFillPaintTransformer(vp);
        vv.getRenderContext().setEdgeDrawPaintTransformer(ep);
    	vv.getRenderContext().setVertexShapeTransformer(vz);
    	vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Object>());
    	vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Object>());

    	vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
                
        for (int i=0; i<AllEdges.length; i++) {
        	if (AllEdges[i] != null) {
        		Graph.addVertex(AllEdges[i].start);
        		Graph.addVertex(AllEdges[i].end);
        		Graph.addEdge(AllEdges[i], AllEdges[i].start, AllEdges[i].end, EdgeType.DIRECTED);
        		//System.out.println("ARP~"+AllEdges[i].toString()+"~"+AllEdges[i].start+"~"+AllEdges[i].end);
        	}
        }    

        return Graph;
	}

}

