package ava.io.authentication_manager.services;

import ava.io.authentication_manager.db.entities.BaseEntity;
import ava.io.authentication_manager.db.repositories.BaseRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

@Getter
public abstract class BaseService<T> {
    private BaseRepository<T> repository;

    @Autowired
    private ApplicationEventPublisher events;

    public BaseService(BaseRepository<T> baseRepository) {
        this.repository = baseRepository;
    }


    //___________________________ BASE SERVICES __________________________//

    public T saveAndPublishEvent(T t) {
        var response = repository.save(t);
        events.publishEvent(createEvent(t));
        return response;
    }

    public void deleteAndPublishEvent(UUID id) {
        var t = repository.findById(id);
        if (t.isPresent()) {
            repository.deleteById(id);
            events.publishEvent(deleteEvent(t.get()));
        }
    }

    public T updateAndPublishEvent(T t) {

        var response = repository.save(t);
        events.publishEvent(updateEvent(t));
        return response;
    }

    public List<T> listAll() {
        return repository.findAll();
    }
//    public List<T> listLates(int maxValue){
//        return listAll().stream().sorted((o1,o2) -> o1.getName().compareTo(o2.getName())).limit(maxValue).collect(Collectors.toList());
//    }


    //___________________________ Publisher ______________________________//
    public EntityCreated<T> createEvent(T source) {
        return new EntityCreated<T>(source);
    }

    public EntityUpdated<T> updateEvent(T source) {
        return new EntityUpdated<T>(source);
    }

    public EntityDeleted<T> deleteEvent(T source) {
        return new EntityDeleted<T>(source);
    }


}

@AllArgsConstructor
class EntityCreated<T> {
    public T source;
}

@AllArgsConstructor
class EntityUpdated<T> {
    public T source;
}

@AllArgsConstructor
class EntityDeleted<T> {
    public T source;
}
