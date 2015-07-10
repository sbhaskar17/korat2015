package korat.testing.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import korat.IFSMModel;
import korat.Korat;
import korat.Trigger;
import korat.utils.io.GraphPaths;


/**
 * Conducts tests for all regular candidates in FSM domain
 * state space
 * 
 * @author Kilnagar Bhaskar <sbhaskar17@gmail.com>
 * 
 */

public class TestCradleFSM {
	ArrayList<String> StateNames= null;
	int NUM_STATES=0;
	int NUM_TRIGGERS=0;

    private static TestCradleFSM instance = new TestCradleFSM();


    public static TestCradleFSM getInstance() {
        return instance;
    }

    protected long validCasesGenerated;

    protected long totalExplored;

    public long getValidCasesGenerated() {
        return validCasesGenerated;
    }

    public long getTotalExplored() {
        return totalExplored;
    }

//	public TestCradleFSM () {
//		
//	}

	class MethodPair {
		Method trigger;
		Method guard;

		public MethodPair (Method t, Method g) {
			trigger=t;
			guard=g;		
		}	
	}

	class FromToStates {
		int fromState;
		int toState;

		public FromToStates (int f, int t) {
			fromState= f;
			toState= t;
		}
	}

	class Coverage { 
		String FromState;
		String Transition;
		String ToState;
		boolean valid;

		public Coverage (String fs, String tx, String ts, boolean v) {
			FromState= fs;
			Transition= tx;
			ToState= ts;
			valid= v;
		}

		@Override
		public String toString() {
			return (FromState+":"+Transition+":"+ToState);
		}

		@Override
		public boolean equals(Object o) {
			Coverage c;
			if (o instanceof Coverage) {
				c= (Coverage)o;
				if (c.FromState.equals(this.FromState) &&
						c.Transition.equals(this.Transition) &&
						c.ToState.equals(this.ToState)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public int hashCode() {
			return ((FromState+Transition+ToState).hashCode());
		}

	}


	protected ArrayList<MethodPair> getTriggers (Class<?> clazz) {
		ArrayList<MethodPair> tgpairs = new ArrayList<MethodPair>();   	    	
		ArrayList<Method> allMethods = new ArrayList<Method>(Arrays.asList(clazz.getMethods()));    

		for (Method m: allMethods) {
			ArrayList<Annotation> annotations = new ArrayList<Annotation>(Arrays.asList(m.getDeclaredAnnotations()));       
			for (Annotation a : annotations) {
				if(a instanceof Trigger) {
					tgpairs.add(new MethodPair(m,null)); // add just triggers, guards=null
				}
			}
		}
		return tgpairs;
	}


	protected ArrayList<MethodPair> getTriggersGuards (Class<?> clazz) throws CannotInvokeMethodException {
		ArrayList<MethodPair> triggers = new ArrayList<MethodPair>();
		ArrayList<MethodPair> tgpairs = new ArrayList<MethodPair>();
		String gs;
		Method g=null;

		try {
			triggers= getTriggers(clazz);

			// get guards
			for (MethodPair t: triggers) {
				gs= t.trigger.getName()+"Guard";
				g = clazz.getMethod(gs);
				tgpairs.add(new MethodPair(t.trigger, g));
			}		
		} catch (Exception e) {
			throw new CannotInvokeMethodException(clazz, "getTriggersGuards", e);			
		}
		return tgpairs;
	}

	protected void startTestGenerationFSM (Class<?> clazz, String[] finArgs) throws CannotInvokeMethodException {
		int runs = Integer.parseInt(finArgs[0]);
		ArrayList<MethodPair> tgpairs;

        totalExplored = 0;
        validCasesGenerated = 0;

		try {
			tgpairs= getTriggersGuards (clazz);
			startCoverageFSM (clazz, tgpairs, runs);
		} catch (Exception e) {
			throw new CannotInvokeMethodException(clazz, "getTriggersGuards", e);			
		}

	}

	protected int getNumberOfStates () {
		int size= 0;
		
		if (StateNames != null) {
			size= StateNames.size();
		}
		
		return size;
	}
	
	protected String getStateName (int sid) {
		String name= null;		
		String[] s;
		
		if (StateNames != null) {
			s= StateNames.toArray(new String[StateNames.size()]);
			name= s[sid];
		}

		return name;
	}

	
	protected void getAllStateNames (Class<?> clazz, IFSMModel fsmObj) {
		String s=null;
		int i;
		
		if (StateNames == null) {
			StateNames= new ArrayList<String>();
			i= 1;
			try {
				while (i< 25) { // arbitrary high number of variables
					s= "FSM_STATE_"+Integer.toString(i);
					StateNames.add((String)clazz.getField(s).get(fsmObj));
					i ++;
				}
			} catch (Exception e) {
				// end of while loop
			}
		}

		return;
	}
	

	protected void startCoverageFSM (Class<?> clazz, ArrayList<MethodPair> tgpairs, int runs) throws 
	CannotAccessFieldException {
		HashSet<Coverage> coverage= new HashSet<Coverage>();
		GraphPaths gp= new GraphPaths();
		Method t, g;
		IFSMModel fsmObj=null;
		String snBefore, snAfter;
		boolean mresult;
		int totCoverCount=0;
		int idx=0;
		
		Random random = new Random();
		try {
			fsmObj = (IFSMModel)clazz.newInstance();
			getAllStateNames (clazz, fsmObj);

			NUM_STATES= getNumberOfStates();
			NUM_TRIGGERS= getTriggersGuards(clazz).size();

			System.out.println("FSM state reset probability: " + Double.toString(Korat.FSM_RESET_PROBABILITY * 100) +" %");
			System.out.println("");

			while (totCoverCount < runs) { // arbitrary high number of explorations
				for (MethodPair mp: tgpairs) {	
					if (totCoverCount >= runs) break;
					
					t= mp.trigger;
					g= mp.guard;

					for (int i= 0; i < getNumberOfStates(); i++) {
						if (totCoverCount >= runs) break;
						
						if (random.nextDouble() < Korat.FSM_RESET_PROBABILITY) {
							// random reset
							resetFSMState (clazz, fsmObj);
							System.out.println("(" + "RANDOM RESET" + ") !!!!");
						}
						
						snBefore= getStateName (i);
						clazz.getField("State").set(fsmObj, snBefore);
						mresult= (Boolean) g.invoke(fsmObj); // execute guard
						
						if (mresult) {
							t.invoke(fsmObj); // execute trigger only if guard returned true
							snAfter=(String) clazz.getField("State").get(fsmObj);
							
							if (coverage.add(new Coverage(snBefore, t.getName(), snAfter, true))) { //  print **** only if it does not already exist
								System.out.println("(" + snBefore + ", " + t.getName() + ", " + snAfter + ") ****");
								
							} else {
								System.out.println("(" + snBefore + ", " + t.getName() + ", " + snAfter + ")");
							}
							
						} else { // guard returned false
							snAfter=(String) clazz.getField("State").get(fsmObj);
							System.out.println("(" + snBefore + ", " + t.getName() + ", " + snAfter + ")");
							coverage.add(new Coverage(snBefore, t.getName(), snAfter, false));
						}	
						
						totCoverCount ++;
					}
				}				
			}

		} catch (Exception e) {
			throw new CannotAccessFieldException(clazz, "runTransitionCoverageFSM", e);
		}

		calculateCoverageMetrics(coverage, totCoverCount);
		
		gp.initFSMGfxPath();
		gp.fInit();
		
		for (Coverage cv: coverage) {
			if (cv.valid) { // consider only valid paths
				gp.addFSMGfxPath(Integer.toString(idx), "from_state", cv.FromState);   		
				gp.addFSMGfxPath(Integer.toString(idx), "to_state", cv.ToState);
				gp.addFSMGfxPath(Integer.toString(idx), "in_transition", cv.Transition);

				idx ++;
			}
		}
		
		gp.saveFSMGfxPathJson(idx);
		

	}


	protected void calculateCoverageMetrics (HashSet<Coverage>coverage, int totCoverCount) {
		HashSet<String> hsstate = new HashSet<String>();
		HashSet<String> hstgr = new HashSet<String>();
		HashSet<String> hstxn = new HashSet<String>();
		HashSet<String> hstxnpr = new HashSet<String>();

		int newCoverCount =0;
		for (Coverage cv: coverage) {
			if (cv.valid) {
				newCoverCount ++;
				hsstate.add(cv.ToState); // state coverage
				hstgr.add(cv.Transition); // trigger coverage
				hstxn.add(cv.FromState+":"+cv.Transition+":"+cv.ToState); // transition coverage
			}
			
			for (Coverage cv1: coverage) {
				if (cv.valid && cv1.valid && cv.ToState.equals(cv1.FromState)) {
					hstxnpr.add(cv.FromState+":"+cv1.ToState); // transition pair coverage
				}
			}
		}
		
		System.out.println("Total explored: " + totCoverCount);
		totalExplored = totCoverCount;

		System.out.println("New & Valid found: " + newCoverCount);
		System.out.println("");
		validCasesGenerated = newCoverCount;


		
		System.out.println("State coverage: ");
		for (String h: hsstate) {
			System.out.println(h);
		}
		System.out.println("covered: " + hsstate.size() + " / " + NUM_STATES + " (" +  (NUM_STATES!=0 ? Math.round((hsstate.size()*100)/NUM_STATES) : 0) +" %)");
		System.out.println("");
		
		
		System.out.println("Trigger coverage: ");
		for (String h: hstgr) {
			System.out.println(h);
		}
		System.out.println("covered: " + hstgr.size() + " / " + NUM_TRIGGERS + " (" +  (NUM_TRIGGERS!=0 ? Math.round((hstgr.size()*100)/NUM_TRIGGERS) : 0) +" %)");
		System.out.println("");

		
		System.out.println("Transition coverage: "); // look for A->x->B
		for (String h: hstxn) {
			System.out.println(h);
		}
		System.out.println("covered: " + hstxn.size());
		System.out.println("");

		
		System.out.println("Transition pair coverage: "); //look for A->X and X->B
		for (String h: hstxnpr) {
			System.out.println(h);
		}
		System.out.println("covered: " + hstxnpr.size());
		System.out.println("");

	}
	

	
	protected void resetFSMState (Class<?> clazz, IFSMModel fsmObj) throws CannotInvokeMethodException {
		Method g=null;

		try {
			g = clazz.getMethod("resetState");
		} catch (Exception e) {
			throw new CannotInvokeMethodException(clazz, "resetFSMState", e);			
		}

	}

}