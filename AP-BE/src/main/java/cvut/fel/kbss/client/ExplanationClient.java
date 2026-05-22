package cvut.fel.kbss.client;

import cvut.fel.kbss.dto.response.ValidationResponse;
import cvut.fel.kbss.exception.APIkeyNotFoundException;
import cvut.fel.kbss.exception.ServiceNotRespondingException;

public interface ExplanationClient {
    public ValidationResponse explainFallacy(String label, String text) throws APIkeyNotFoundException, ServiceNotRespondingException;
}
