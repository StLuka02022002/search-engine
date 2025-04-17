package searchengine.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Table(name = "pages",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"site_id", "path"})},
        indexes = {
                @Index(name = "idx_path", columnList = "path"),
                @Index(name = "idx_site", columnList = "site_id"),
                @Index(name = "idx_site_path", columnList = "site_id, path", unique = true)
        })
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Column(name = "path", columnDefinition = "VARCHAR(255)", nullable = false)
    private String path;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToMany(mappedBy = "page")
    private List<IndexEntity> indices = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageEntity that = (PageEntity) o;
        return Objects.equals(site, that.site) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, path);
    }

    @Override
    public String toString() {
        return "PageEntity{" +
                "code=" + code +
                ", path='" + path + '\'' +
                '}';
    }
}
