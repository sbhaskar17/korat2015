package korat.examples.fsm;

import korat.*;

public class FSM implements IFSMModel {
	public String State;
	public String FSM_STATE_1="NO COINS";
	public String FSM_STATE_2="ONE COIN";	
	public String FSM_STATE_3="TWO COINS";
					
	public boolean add1coinGuard() {
		return (State.equals(FSM_STATE_1) || State.equals(FSM_STATE_2)); 
	}
	
	public @Trigger void add1coin() {
		State = State.equals(FSM_STATE_1) ? FSM_STATE_2 : FSM_STATE_3; 
	}
	
	public boolean add2coinsGuard() { 
		return (State.equals(FSM_STATE_1));
	}
	
	public @Trigger void add2coins() {
		State = FSM_STATE_3; 
	}
	
	public boolean vendgumGuard() {
		return (State.equals(FSM_STATE_3)); 
	}
	
	public @Trigger void vendgum() {
		State = FSM_STATE_1; 
	}
	

	@Override
	public void resetState() {
		State = FSM_STATE_1;		
	}
		
}
