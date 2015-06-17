package korat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FSMModel {

	static boolean fsm_model;
	
	public FSMModel() {		
		
	}
		
	public boolean isFSMModel(Class clazz) {		
        List<Method> methods = new ArrayList<Method>(Arrays.asList(clazz.getMethods()));       
		for (Method m: methods) {
	        List<Annotation> annotations = new ArrayList<Annotation>(Arrays.asList(m.getDeclaredAnnotations()));       
			for(Annotation annotation : annotations){
			    if(annotation instanceof Trigger) {
			    	fsm_model = true;
			    	return true;
			    }
			}
		}	
		fsm_model = false;
		return false;
	}

	public boolean isFSMModel() {
		return fsm_model;
	}

}
