-- Создание базы данных
CREATE DATABASE library;

-- Улучшенное обработка потенциальных ошибок
DROP TABLE IF EXISTS book_loans CASCADE;
DROP TABLE IF EXISTS book CASCADE;
DROP TABLE IF EXISTS person CASCADE;

-- Создание таблицы person
CREATE TABLE person
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    year_of_birth INT          NOT NULL CHECK (year_of_birth >= 1900 AND year_of_birth <= 2010)
);

-- Создание таблицы book
CREATE TABLE book
(
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(30)  NOT NULL,
    year_of_release INT          NOT NULL CHECK (year_of_release >= 1000),
    author          VARCHAR(100) NOT NULL,
    person_id       BIGINT,
    taken_at        TIMESTAMP,
    FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE SET NULL
);

-- Создание таблицы book_loans
CREATE TABLE book_loans
(
    id          BIGSERIAL PRIMARY KEY,
    book_id     BIGINT NOT NULL,
    person_id   BIGINT NOT NULL,
    loan_date   DATE   NOT NULL,
    return_date DATE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE,
    FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE CASCADE
);

-- Индексы для улучшения производительности
CREATE INDEX idx_person_name ON person (name);
CREATE INDEX idx_book_title ON book (title);
CREATE INDEX idx_book_author ON book (author);
