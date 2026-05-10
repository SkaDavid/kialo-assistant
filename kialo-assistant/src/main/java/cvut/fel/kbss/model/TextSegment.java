package cvut.fel.kbss.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TextSegment {
    @Enumerated(EnumType.STRING)
    private TextSegmentType type;

    @Column(name = "content")
    private String content;

    @Column(name = "explanation")
    private String explanation;

    @Column(name = "resource")
    private String resource;
}
