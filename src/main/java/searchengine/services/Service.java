package searchengine.services;

import java.util.List;

public interface Service<T> {

    List<T> getAll();
    T get(Long id);
    T save(T t);
    T update(Long id,T t);
    void delete(Long id);
    boolean exists(Long id);
}
