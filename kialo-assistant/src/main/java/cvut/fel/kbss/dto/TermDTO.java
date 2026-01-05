package cvut.fel.kbss.dto;
public class TermDTO {

    public String authorization;
    private String label;
    private String description;
    public TermDTO(String label, String description, String authorization){
        this.label = label;
        this.description = description;
        this.authorization = authorization;
    }
    public TermDTO(){

    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
