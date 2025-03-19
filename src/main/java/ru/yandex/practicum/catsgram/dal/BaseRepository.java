package ru.yandex.practicum.catsgram.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.catsgram.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected boolean delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }

    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    protected long insert(String query, Object... params) {
        // Создаем объект GeneratedKeyHolder, который будет хранить сгенерированный ключ
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        // Выполняем SQL-запрос с параметрами
        jdbc.update(connection -> {
            // Подготавливаем SQL-запрос с указанием, что мы хотим получить сгенерированный ключ
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            // Устанавливаем параметры запроса
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            // Возвращаем подготовленный запрос
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ключ как объект Long
        Long id = keyHolder.getKeyAs(Long.class);

        // Если ключ не равен null, возвращаем его
        if (id != null) {
            return id;
        } else {
            // Если ключ равен null, выбрасываем исключение
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }
}