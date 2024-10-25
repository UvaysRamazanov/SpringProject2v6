package library.services;

import jakarta.transaction.Transactional;
import library.models.Book;
import library.models.Person;
import library.repositories.BooksRepositories;
import library.util.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BooksService {

    private final BooksRepositories booksRepositories;
    private final PeopleService peopleService;

    public List<Book> findAll(boolean sortByYear) {
        log.info("Получение всех книг, сортировка по году: {}", sortByYear);
        return sortByYear ? booksRepositories.findAll(Sort.by("yearOfRelease")) : booksRepositories.findAll();
    }

    public Book findOne(long id) {
        log.info("Поиск книги с ID: {}", id);
        return booksRepositories.findById(id)
                .orElseThrow(() -> {
                    log.error("Книга с ID: {} не найдена", id);
                    return new ResourceNotFoundException("Книга не найдена");
                });
    }

    @Transactional
    public void save(Book book) {
        log.info("Сохранение книги: {}", book);
        booksRepositories.save(book);
    }

    @Transactional
    public void update(long id, Book bookUpdated) {
        log.info("Обновление книги с ID: {}", id);
        Book existingBook = findOne(id);  // Используем проверенный метод для поиска
        bookUpdated.setId(existingBook.getId());
        bookUpdated.setOwner(existingBook.getOwner());
        booksRepositories.save(bookUpdated);
        log.info("Книга с ID: {} успешно обновлена", id);
    }

    @Transactional
    public void delete(long id) {
        log.info("Удаление книги с ID: {}", id);
        if (booksRepositories.existsById(id)) {
            booksRepositories.deleteById(id);
            log.info("Книга с ID: {} успешно удалена", id);
        } else {
            log.warn("Книга с ID: {} не найдена, не удалось удалить", id);
        }
    }

    public Person getBookOwner(long id) {
        log.info("Получение владельца книги с ID: {}", id);
        return booksRepositories.findById(id)
                .map(Book::getOwner)
                .orElse(null);
    }

    @Transactional
    public void release(Long bookId, Long personId) {
        log.info("Освобождение книги с ID: {}", bookId);
        Book book = findOne(bookId);

        // Проверяем, есть ли владелец и совпадает ли его ID с personId
        if (book.getOwner() != null && Long.valueOf(book.getOwner().getId()).equals(personId)) {
            book.setOwner(null);
            book.setTakenAt(null);
            booksRepositories.save(book);
            log.info("Книга с ID: {} успешно освобождена", bookId);
        } else {
            log.warn("Книга с ID: {} не может быть освобождена, так как она не назначена этому пользователю", bookId);
            throw new IllegalStateException("Ошибка: Книга не назначена пользователю или не принадлежит ему!");
        }
    }



    @Transactional
    public void assign(Long bookId, Long personId) {
        log.info("Назначение книги с ID: {} пользователю с ID: {}", bookId, personId);

        Book book = findOne(bookId);
        Person person = peopleService.findOne(personId); // Получение объекта Person

        if (book.getOwner() == null) { // Проверяем, что владелец отсутствует
            book.setOwner(person);
            book.setTakenAt(new Date());
            booksRepositories.save(book);
            log.info("Книга с ID: {} назначена пользователю: {}", bookId, person.getName());
        } else {
            // Если владелец уже есть, проверяем, совпадает ли он с переданным personId
            if (book.getOwner() != null && book.getOwner().getId() == person.getId()) {
                // Возможный вариант обработки, если книга уже назначена этому человеку
                log.info("Книга с ID: {} уже назначена пользователю с ID: {}", bookId, personId);
            } else {
                log.warn("Книга с ID: {} уже выдана", bookId);
                throw new IllegalStateException("Книга уже выдана!");
            }
        }
    }

    public List<Book> findAll(int page, int booksPerPage, boolean sortByYear) {
        log.info("Получение книг для страницы: {}, количество книг на странице: {}, сортировка по году: {}", page, booksPerPage, sortByYear);
        Pageable pageable = PageRequest.of(page, booksPerPage, sortByYear ? Sort.by("yearOfRelease") : Sort.unsorted());
        return booksRepositories.findAll(pageable).getContent();
    }

    public List<Book> searchByName(String query) {
        log.info("Поиск книг по запросу: {}", query);
        return booksRepositories.findByTitleStartingWith(query);
    }
}
