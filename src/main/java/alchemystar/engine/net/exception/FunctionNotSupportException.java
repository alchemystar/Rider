package alchemystar.engine.net.exception;

/**
 * FunctionNotSupportException
 *
 * @Author lizhuyang
 */
public class FunctionNotSupportException extends  RuntimeException {

    public FunctionNotSupportException() {
        super();
    }

    public FunctionNotSupportException(String message) {
        super(message);
    }

    public FunctionNotSupportException(String message, Throwable cause) {
        super(message, cause);
    }

    public FunctionNotSupportException(Throwable cause) {
        super(cause);
    }

    protected FunctionNotSupportException(String message, Throwable cause, boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
