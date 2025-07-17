package kcz.rfid.backend.service;

import kcz.rfid.backend.model.entity.EntityBase;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface EntityService <E extends EntityBase>{

    Optional<E> findById(UUID id);

    Collection<E> getAll();

    <T extends E> T save(T entity);

    <T extends E> T saveAndFlush(T entity);

    void delete(E entity);

    void deleteById(UUID id);

    E getReference(UUID id);
}
