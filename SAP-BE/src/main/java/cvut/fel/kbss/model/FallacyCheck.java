package cvut.fel.kbss.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table
public class FallacyCheck {
    public FallacyCheck(){
        this.fallacyResult = FallacyResult.NOT_TESTED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="fallacy_result")
    private FallacyResult fallacyResult;

    @Column(name="score")
    private double score;

    @Column(name="falllacy")
    private String fallacy;

    @Column(name="explanation", columnDefinition = "TEXT")
    private String explanation;
}
