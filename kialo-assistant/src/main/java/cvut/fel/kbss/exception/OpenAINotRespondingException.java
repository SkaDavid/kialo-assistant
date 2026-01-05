package cvut.fel.kbss.exception;

public class OpenAINotRespondingException extends Exception{
    public OpenAINotRespondingException(String message){
        super(message);
    }

    public OpenAINotRespondingException(String message, Throwable cause){
        super(message, cause);
    }
}
