package cvut.fel.kbss.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table
public class Debate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="topic")
    private String topic;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(mappedBy = "debate", cascade = CascadeType.ALL)
    private List<Argument> arguments;

    @Column(name="visibility")
    @Enumerated(EnumType.STRING)
    private DebateVisibility visibility;

    @Column(name="kialo_id")
    private Long kialoId;

}