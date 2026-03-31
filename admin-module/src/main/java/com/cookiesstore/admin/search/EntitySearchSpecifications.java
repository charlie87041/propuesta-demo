package com.cookiesstore.admin.search;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class EntitySearchSpecifications {

    private EntitySearchSpecifications() {
    }

    public static <T> Specification<T> globalSearch(String searchQuery, List<String> fields) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(searchQuery) || fields == null || fields.isEmpty()) {
                return cb.conjunction();
            }

            String rawQuery = searchQuery.trim();
            String loweredQuery = rawQuery.toLowerCase();
            List<Predicate> predicates = new ArrayList<>();

            for (String field : fields) {
                if (!StringUtils.hasText(field)) {
                    continue;
                }

                Predicate predicate;
                try {
                    predicate = buildPredicate(root.get(field), rawQuery, loweredQuery, cb);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.or(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate buildPredicate(
        Path<?> fieldPath,
        String rawQuery,
        String loweredQuery,
        jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        Class<?> fieldType = fieldPath.getJavaType();

        if (String.class.equals(fieldType)) {
            return cb.like(cb.lower(fieldPath.as(String.class)), "%" + loweredQuery + "%");
        }

        if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
            Integer numericQuery = parseInteger(rawQuery);
            return numericQuery == null ? null : cb.equal(fieldPath.as(Integer.class), numericQuery);
        }

        if (Long.class.equals(fieldType) || long.class.equals(fieldType)) {
            Long numericQuery = parseLong(rawQuery);
            return numericQuery == null ? null : cb.equal(fieldPath.as(Long.class), numericQuery);
        }

        if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
            Boolean booleanQuery = parseBoolean(rawQuery);
            return booleanQuery == null ? null : cb.equal(fieldPath.as(Boolean.class), booleanQuery);
        }

        if (Instant.class.equals(fieldType)) {
            LocalDate localDateQuery = parseDate(rawQuery);
            if (localDateQuery == null) {
                return null;
            }
            Instant start = localDateQuery.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = localDateQuery.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            return cb.between(fieldPath.as(Instant.class), start, end);
        }

        if (LocalDate.class.equals(fieldType)) {
            LocalDate localDateQuery = parseDate(rawQuery);
            return localDateQuery == null ? null : cb.equal(fieldPath.as(LocalDate.class), localDateQuery);
        }

        return null;
    }

    private static Integer parseInteger(String query) {
        try {
            return Integer.valueOf(query);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Long parseLong(String query) {
        try {
            return Long.valueOf(query);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Boolean parseBoolean(String query) {
        if ("true".equalsIgnoreCase(query)) {
            return true;
        }
        if ("false".equalsIgnoreCase(query)) {
            return false;
        }
        return null;
    }

    private static LocalDate parseDate(String query) {
        try {
            return LocalDate.parse(query);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
