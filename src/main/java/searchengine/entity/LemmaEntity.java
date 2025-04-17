package searchengine.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Table(name = "lemmas",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"site_id", "lemma"})},
        indexes = {
                @Index(name = "idx_lemma", columnList = "lemma"),
                @Index(name = "idx_site_lemma", columnList = "site_id, lemma", unique = true)
        })
public class LemmaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Column(name = "lemma", columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(name = "frequency", columnDefinition = "INT", nullable = false)
    private Integer frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexEntity> indices = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LemmaEntity that = (LemmaEntity) o;
        return Objects.equals(site, that.site) && Objects.equals(lemma, that.lemma) && Objects.equals(frequency, that.frequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, lemma, frequency);
    }
}
