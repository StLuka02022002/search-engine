package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractService<T, R extends JpaRepository<T, Long>> implements Service<T> {

    protected final R repository;

    @Override
    public List<T> getAll() {
        return repository.findAll();
    }

    @Override
    public T get(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public T save(T t) {
        return repository.save(t);
    }

    @Override
    public abstract T update(Long id, T t);

    @Override
    public void delete(Long id) {
        if (exists(id)) {
            repository.deleteById(id);
        }
    }

    @Override
    public boolean exists(Long id) {
        return repository.existsById(id);
    }
}
