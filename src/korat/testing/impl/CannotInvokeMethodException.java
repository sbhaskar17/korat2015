package korat.testing.impl;

/**
 * 
 * @author Kilnagar Bhaskar <sbhaskar17@gmail.com>
 *
 */
public class CannotInvokeMethodException extends KoratMethodException {

    private static final long serialVersionUID = 4065045854470826333L;

    public CannotInvokeMethodException(Class cls, String methodName, String message, Throwable cause) {
        super(cls, methodName, message, cause);
    }

    public CannotInvokeMethodException(Class cls, String methodName, String message) {
        super(cls, methodName, message);
    }

    public CannotInvokeMethodException(Class cls, String methodName, Throwable cause) {
        super(cls, methodName, cause);
    }

    public CannotInvokeMethodException(Class cls, String methodName) {
        super(cls, methodName);
    }
    
}
