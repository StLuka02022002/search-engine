package searchengine.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "indices",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"page_id", "lemma_id"})},
        indexes = {
                @Index(name = "idx_page", columnList = "page_id"),
                @Index(name = "idx_lemma", columnList = "lemma_id"),
                @Index(name = "idx_page_lemma", columnList = "page_id, lemma_id", unique = true)
        })
public class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", nullable = false)
    private LemmaEntity lemma;

    @Column(name = "number", columnDefinition = "FLOAT", nullable = false)
    private Float rank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexEntity that = (IndexEntity) o;
        return Objects.equals(page, that.page) && Objects.equals(lemma, that.lemma) && Objects.equals(rank, that.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, lemma, rank);
    }
}
