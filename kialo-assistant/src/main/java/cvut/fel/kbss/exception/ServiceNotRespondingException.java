package cvut.fel.kbss.exception;

public class ServiceNotRespondingException extends Exception{
    public ServiceNotRespondingException(String message){
        super(message);
    }

    public ServiceNotRespondingException(String message, Throwable cause){
        super(message, cause);
    }
}
