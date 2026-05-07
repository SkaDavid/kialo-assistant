package cvut.fel.kbss.client;

import cvut.fel.kbss.dto.response.FallacyResponseDto;
import cvut.fel.kbss.exception.ServiceNotRespondingException;

public interface FallacyClient{
    public FallacyResponseDto testFallacy(String text) throws ServiceNotRespondingException;
}
