package cvut.fel.kbss.client;

import cvut.fel.kbss.dto.response.AIDebateResponse;
import cvut.fel.kbss.exception.APIkeyNotFoundException;
import cvut.fel.kbss.exception.ServiceNotRespondingException;

public interface AIClient {
    public AIDebateResponse generateDebate(String thesis) throws APIkeyNotFoundException, ServiceNotRespondingException;
    public String generateArgument(String type, String argumentText, String debate) throws APIkeyNotFoundException, ServiceNotRespondingException;
}
