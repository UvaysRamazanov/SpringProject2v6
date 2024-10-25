package library.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "person")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Person {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    @NotEmpty(message = "Имя пользователя не может быть пустым")
    private String name;

    @NotEmpty(message = "Год рождения пользователя не может быть пустым")
    @Min(value = 1900, message = "Год рождения не должен быть меньше 1900")
    @Max(value = 2010, message = "Год рождения не должен превышать 2010")
    // Условие - клиенты библиотеки должны быть старше 14 лет (2010 год рождения)
    @Column(name = "year_of_birth")
    private int yearOfBirth;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Book> books;

    public void releaseBooks() {
        if (books != null) {
            for (Book book : books) {
                book.setOwner(null); // Освобождаем книги
            }
        }
    }
}