package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.model.entity.EntityBase;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.service.EntityService;
import lombok.Getter;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Getter
public class EntityServiceBase<E extends EntityBase> implements EntityService<E> {

    protected final EntityRepository<E> repository;

    public EntityServiceBase(EntityRepository<E> repository) {
        this.repository = repository;
    }

    @Override
    public Optional<E> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Collection<E> getAll() {
        return repository.findAll();
    }

    @Override
    public <T extends E> T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public <T extends E> T saveAndFlush(T entity) {
        return repository.saveAndFlush(entity);
    }

    @Override
    public void delete(E entity) {
        repository.delete(entity);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public E getReference(UUID id) {
        return repository.getReferenceById(id);
    }

}
