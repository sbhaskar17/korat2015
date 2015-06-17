package korat.exploration;

import junit.framework.TestCase;
import korat.Korat;
import korat.testing.impl.TestCradleFSM;

/**
 * @author Kilnagar Bhaskar <sbhaskar17@gmail.com>
 * 
 */
class TestConfigsFSM {

    private static TestConfigsFSM instance = new TestConfigsFSM();

    public static TestConfigsFSM getInstance() {
        return instance;
    }

    private TestConfigsFSM() {

    }

    private int curr = 0;

    private int numOfConfigs = 1;

    public void reset() {
        curr = 0;
    }

    public boolean hasNext() {
        return curr < numOfConfigs;
    }

    public void next() {
        setConfig(curr);
        curr++;
    }

    private void setConfig(int curr) {
        if (curr == 0) {
            setConfig0();
        }
    }

    private void setConfig0() {
    }

}

/**
 * @author Kilnagar Bhaskar <sbhaskar17@gmail.com>
 * 
 */
public class BaseFSMExplorationTest extends TestCase {

    protected void doTestForAllConfigs(String cmdLine, int newCases, int tested) {
        doTestForAllConfigs(cmdLine.split(" "), newCases, tested);
    }

    private void doTestForAllConfigs(String[] args, int newCases, int tested) {

        TestConfigsFSM it = TestConfigsFSM.getInstance();
        it.reset();
        while (it.hasNext()) {

            it.next(); 
            Korat.main(args);
            assertEquals(newCases, TestCradleFSM.getInstance().getValidCasesGenerated());
            if (tested > 0) {
                assertEquals(tested, TestCradleFSM.getInstance().getTotalExplored());
            }

        }

    }

}
