package library.repositories;

// todo нет форматирования -> в IDE есть автоформатирования, можно его юзать

import library.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BooksRepositories extends JpaRepository<Book, Long> {
    List<Book> findByTitleStartingWith(String title);
}