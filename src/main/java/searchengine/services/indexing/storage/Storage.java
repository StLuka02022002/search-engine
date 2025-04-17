package searchengine.services.indexing.storage;

import searchengine.entity.PageEntity;

import java.util.List;
import java.util.Set;

public interface Storage<E> {

    List<E> save();

    boolean contains(E e);

    boolean add(E e);

    int size();

    boolean remove(E e);

    void update(E e);

    Set<E> getSet();
}
