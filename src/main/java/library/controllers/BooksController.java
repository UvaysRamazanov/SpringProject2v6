package library.controllers;

import jakarta.validation.Valid;
import library.models.Book;
import library.models.Person;
import library.services.BooksService;
import library.services.PeopleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/book")
@RequiredArgsConstructor
@Slf4j
public class BooksController {
    private final BooksService bookService;
    private final PeopleService peopleService;

    @GetMapping
    public String showAll(Model model,
                          @RequestParam(value = "page", required = false) Integer page,
                          @RequestParam(value = "books_per_page", required = false) Integer booksPerPage,
                          @RequestParam(value = "sort_by_year", required = false) boolean sortByYear) {

        log.debug("Запрос списка книг: page = {}, books_per_page = {}, sortByYear = {}", page, booksPerPage, sortByYear);

        model.addAttribute("books", (page == null || booksPerPage == null)
                ? bookService.findAll(sortByYear)
                : bookService.findAll(page, booksPerPage, sortByYear));

        return "bookShowAll";
    }

    @GetMapping("/{id}")
    public String show(Model model, @PathVariable("id") int id, @ModelAttribute("person") Person person) {
        log.debug("Запрос информации о книге с id: {}", id);

        model.addAttribute("book", bookService.findOne(id));
        Optional<Person> bookOwner = Optional.ofNullable(bookService.getBookOwner(id));

        bookOwner.ifPresentOrElse(
                owner -> model.addAttribute("owner", owner),
                () -> model.addAttribute("people", peopleService.findAll())
        );

        return "bookShow";
    }

    @GetMapping("/new")
    public String newBook(Model model) {
        log.debug("Открытие формы для создания новой книги");
        model.addAttribute("book", new Book());
        return "bookNew";
    }

    @PostMapping
    public String create(@ModelAttribute("book") @Valid Book book, BindingResult bindingResult) {
        log.debug("Создание новой книги: {}", book);

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при создании книги: {}", bindingResult.getAllErrors());
            return "bookNew";
        }

        bookService.save(book);
        log.info("Книга успешно создана: {}", book);
        return "redirect:/book";
    }

    @GetMapping("/{id}/edit")
    public String edit(Model model, @PathVariable("id") long id) {
        log.debug("Запрос на редактирование книги с id: {}", id);
        model.addAttribute("book", bookService.findOne(id));
        return "bookEdit";
    }

    @PatchMapping("/{id}")
    public String update(@ModelAttribute("book") @Valid Book book, BindingResult bindingResult, @PathVariable("id") long id) {
        log.debug("Обновление книги с id: {} и данными: {}", id, book);

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при обновлении книги: {}", bindingResult.getAllErrors());
            return "bookEdit";
        }

        bookService.update(id, book);
        log.info("Книга успешно обновлена с id: {}", id);
        return "redirect:/book";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id) {
        log.debug("Удаление книги с id: {}", id);
        bookService.delete(id);
        log.info("Книга успешно удалена с id: {}", id);
        return "redirect:/book";
    }

    @PatchMapping("/{bookId}/release/{personId}")
    public String release(@PathVariable("bookId") Long bookId, @PathVariable("personId") Long personId) {
        log.debug("Освобождение книги с id: {} пользователем с id: {}", bookId, personId);
        bookService.release(bookId, personId);
        log.info("Книга с id: {} успешно освобождена", bookId);
        return "redirect:/book/" + bookId;
    }


    @PatchMapping("/{id}/assign")
    public String assign(@PathVariable("id") int id, @ModelAttribute("person") Person selectedPerson) {
        log.debug("Назначение книги с id: {} пользователю: {}", id, selectedPerson);

        long personId = selectedPerson.getId(); // Предполагаем, что у Person есть метод getId()

        bookService.assign((long) id, personId); // Прямое приведение id к long
        log.info("Книга с id: {} назначена пользователю: {}", id, selectedPerson.getName());

        // Перенаправление
        return "redirect:/book/" + id;
    }

    @GetMapping("/search")
    public String search() {
        log.debug("Открытие страницы поиска книг");
        return "bookSearch";
    }

    @PostMapping("/search")
    public String makeSearch(Model model, @RequestParam String query) {
        log.debug("Поиск книг по запросу: {}", query);
        model.addAttribute("books", bookService.searchByName(query));
        log.info("Поиск книг завершен для запроса: {}", query);
        return "bookSearch";
    }
}
