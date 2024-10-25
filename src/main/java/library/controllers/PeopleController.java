package library.controllers;

import jakarta.validation.Valid;
import library.models.Person;
import library.services.PeopleService;
import library.validators.PersonValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/people")
@Slf4j
public class PeopleController {
    private final PeopleService peopleService;
    private final PersonValidator personValidator;

    @Autowired
    public PeopleController(PeopleService peopleService, PersonValidator personValidator) {
        this.peopleService = peopleService;
        this.personValidator = personValidator;
    }

    @GetMapping
    public String showAll(Model model) {
        log.debug("Запрос списка всех пользователей");
        model.addAttribute("people", peopleService.findAll());
        return "peopleShowAll";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable("id") int id, Model model) {
        log.debug("Запрос информации о пользователе с id: {}", id);
        model.addAttribute("person", peopleService.findOne(id));
        model.addAttribute("books", peopleService.getBooksByPersonId(id));
        log.debug("Получена информация о пользователе с id: {}", id);
        return "peopleShow";
    }

    @GetMapping("/new")
    public String newPerson(Model model) {
        log.debug("Открытие формы для создания нового пользователя");
        model.addAttribute("person", new Person());
        return "peopleNew";
    }

    @PostMapping
    public String create(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult) {
        log.debug("Создание нового пользователя: {}", person);
        personValidator.validate(person, bindingResult);

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации: {}", bindingResult.getAllErrors());
            return "peopleNew";
        }

        peopleService.save(person);
        log.info("Пользователь успешно создан: {}", person);
        return "redirect:/people";
    }

    @GetMapping("/{id}/edit")
    public String edit(Model model, @PathVariable("id") int id) {
        log.debug("Запрос на редактирование пользователя с id: {}", id);
        model.addAttribute("person", peopleService.findOne(id));
        return "peopleEdit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") int id, @ModelAttribute("person") @Valid Person person, BindingResult bindingResult) {
        log.debug("Обновление пользователя с id: {} и данными: {}", id, person);

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при обновлении: {}", bindingResult.getAllErrors());
            return "peopleEdit";
        }

        peopleService.update(id, person);
        log.info("Пользователь успешно обновлён с id: {}", id);
        return "redirect:/people";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id) {
        log.debug("Удаление пользователя с id: {}", id);
        peopleService.deletePerson((long) id);
        log.info("Пользователь успешно удалён с id: {}", id);
        return "redirect:/people";
    }
}
