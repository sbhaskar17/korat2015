package korat.exploration;

/**
 * Test for korat.examples.fsm.FSM example
 * 
 * @author Kilnagar Bhaskar <sbhaskar17@gmail.com> PAR
 * 
 */
public class FSMExplorationTest extends BaseFSMExplorationTest {

    public void testFSM() throws Exception {

        String cmdLine = "-c korat.examples.fsm.FSM -a 3";
        doTestForAllConfigs(cmdLine, 1, 3);

    }

}
