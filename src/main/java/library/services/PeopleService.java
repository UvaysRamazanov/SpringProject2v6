package library.services;

import jakarta.persistence.EntityNotFoundException;
import library.models.Book;
import library.models.Person;
import library.repositories.PeopleRepositories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeopleService {
    private final PeopleRepositories peopleRepositories;
    private static final long OVERDUE_TERM_MILLIS = 864000000; // 10 дней в миллисекундах
    private static final Logger logger = LoggerFactory.getLogger(PeopleService.class);

    public List<Person> findAll() {
        logger.info("Получение всех пользователей.");
        return peopleRepositories.findAll();
    }

    public Person findOne(long id) {
        logger.info("Поиск пользователя с ID: {}", id);
        return peopleRepositories.findById(id)
                .orElseThrow(() -> {
                    logger.error("Пользователь с ID: {} не найден.", id);
                    return new EntityNotFoundException("Пользователь не найден");
                });
    }

    public Person findOne(String name) {
        logger.info("Поиск пользователя с именем: {}", name);
        return peopleRepositories.findByName(name);
    }

    @Transactional
    public void save(Person person) {
        logger.info("Сохранение пользователя: {}", person);
        peopleRepositories.save(person);
    }

    @Transactional
    public void update(long id, Person personUpdated) {
        if (!peopleRepositories.existsById(id)) {
            logger.warn("Попытка обновления несуществующего пользователя с ID: {}", id);
            throw new EntityNotFoundException("Пользователь не найден");
        }
        logger.info("Обновление пользователя с ID: {}", id);
        personUpdated.setId(id);
        peopleRepositories.save(personUpdated);
        logger.info("Пользователь с ID: {} обновлен.", id);
    }


    @Transactional
    public void delete(long id) {
        if (!peopleRepositories.existsById(id)) {
            logger.warn("Попытка удаления несуществующего пользователя с ID: {}", id);
            throw new EntityNotFoundException("Пользователь не найден");
        }
        logger.info("Удаление пользователя с ID: {}", id);
        peopleRepositories.deleteById(id);
        logger.info("Пользователь с ID: {} удален.", id);
    }


    @Transactional
    public List<Book> getBooksByPersonId(long id) {
        Person person = peopleRepositories.findById(id)
                .orElseThrow(() -> {
                    logger.error("Пользователь с ID: {} не найден при попытке получить книги.", id);
                    return new EntityNotFoundException("Пользователь не найден");
                });

        long nowMillis = System.currentTimeMillis();
        logger.info("Получение книг для пользователя с ID: {}", id);

        for (Book book : person.getBooks()) {
            long diffMillis = nowMillis - book.getTakenAt().getTime();
            book.setOverdue(diffMillis > OVERDUE_TERM_MILLIS);
        }

        logger.info("Книги для пользователя с ID: {} получены.", id);
        return person.getBooks();
    }

    public void deletePerson(Long personId) {
        logger.info("Попытка удалить пользователя с ID: {}", personId);
        Person person = peopleRepositories.findById(personId)
                .orElseThrow(() -> {
                    logger.error("Пользователь с ID: {} не найден при удалении.", personId);
                    return new EntityNotFoundException("Пользователь не найден");
                });

        person.releaseBooks(); // Освобождаем книги пользователя
        peopleRepositories.delete(person); // Удаляем пользователя
        logger.info("Пользователь с ID: {} успешно удален.", personId);
    }
}
