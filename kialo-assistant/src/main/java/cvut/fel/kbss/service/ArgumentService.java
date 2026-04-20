package cvut.fel.kbss.service;


import cvut.fel.kbss.repository.ArgumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArgumentService {
    private final ArgumentRepository argumentRepository;

    @Autowired
    public ArgumentService(ArgumentRepository argumentRepository){
        this.argumentRepository = argumentRepository;
    }



}
