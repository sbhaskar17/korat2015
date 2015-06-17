package korat.testing.impl;

/**
 * 
 * @author Kilnagar Bhaskar <sbhaskar17@gmail.com>
 *
 */
public class CannotAccessFieldException extends KoratMethodException {

    private static final long serialVersionUID = 4065045854470826333L;

    public CannotAccessFieldException(Class cls, String methodName, String message, Throwable cause) {
        super(cls, methodName, message, cause);
    }

    public CannotAccessFieldException(Class cls, String methodName, String message) {
        super(cls, methodName, message);
    }

    public CannotAccessFieldException(Class cls, String methodName, Throwable cause) {
        super(cls, methodName, cause);
    }

    public CannotAccessFieldException(Class cls, String methodName) {
        super(cls, methodName);
    }
    
}
