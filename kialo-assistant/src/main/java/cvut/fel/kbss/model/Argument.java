package cvut.fel.kbss.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name="text", columnDefinition = "TEXT")
    private String text;

    @ElementCollection
    @CollectionTable(name = "argument_segments", joinColumns = @JoinColumn(name = "argument_id"))
    @OrderColumn(name = "segment_order")
    private List<TextSegment> segments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private ArgumentType type;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Argument parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Argument> children;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "debate_id")
    private Debate debate;
}
