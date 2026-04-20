package cvut.fel.kbss.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table
public class Argument {
    public Argument(String text, ArgumentType type, Argument parent, User owner, Debate debate) {
        this.text = text;
        this.type = type;
        this.parent = parent;
        this.owner = owner;
        this.debate = debate;
    }

    public Argument(){

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="text")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private ArgumentType type;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Argument parent;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "debate_id")
    private Debate debate;
}
