package ru.fizteh.fivt.students.veraklim.CollectionQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ru.fizteh.fivt.students.veraklim.CollectionQuery.CollectionQuery.*;
import ru.fizteh.fivt.students.veraklim.CollectionQuery.impl.CollectionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Aggregates.*;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.CollectionQuery.Student.student;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Conditions.rlike;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.OrderByConditions.asc;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.OrderByConditions.desc;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Sources.list;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.impl.FromStmt.from;

@RunWith(PowerMockRunner.class)
public class ImplTest {
    List<Student> students;
    List<String> answer = new ArrayList<>();

    @Before
    public void setUp() {
        students = list(
                student("Petrov", LocalDate.parse("1986-08-06"), "494"),
                student("Ivanova", LocalDate.parse("1986-08-06"), "495"),
                student("Smolin", LocalDate.parse("1986-08-06"), "495"),
                student("Utesov", LocalDate.parse("2006-08-06"), "494"),
                student("Utesov", LocalDate.parse("2006-08-06"), null));
    }

    @Test
    public void testQuery() throws CollectionException {
        Iterable<Statistics> statistics =
                from(list(
                        student("Petrov", LocalDate.parse("1986-08-06"), "494"),
                        student("Ivanova", LocalDate.parse("1986-08-06"), "495"),
                        student("Smolin", LocalDate.parse("1986-08-06"), "495"),
                        student("Utesov", LocalDate.parse("2006-08-06"), "494")))
                        .select(Statistics.class, Student::getGroup, count(Student::getGroup), avg(Student::age))
                        .where(rlike(Student::getName, ".*ov").and(s -> s.age() > 20))
                        .groupBy(Student::getGroup)
                        .having(s -> s.getCount() > 0)
                        .orderBy(asc(Statistics::getGroup), desc(Statistics::getCount))
                        .limit(100)
                        .union()
                        .from(list(student("Petrov", LocalDate.parse("1985-08-06"), "494")))
                        .selectDistinct(Statistics.class, s -> "all", count(s -> 1), avg(Student::age))
                        .execute();
        statistics.forEach(t -> answer.add(t.toString()));
        assertTrue(answer.size() == 3);
        assertTrue(answer.contains(new Statistics("494", 1L, 29.0).toString()));
        assertTrue(answer.contains(new Statistics("495", 1L, 29.0).toString()));
        assertTrue(answer.contains(new Statistics("all", 1L, 30.0).toString()));
    }

    @Test
    public void testSelect() throws Exception {
        Iterable<String> result = from(list(
                student("Petrov", LocalDate.parse("1986-08-06"), "494"),
                student("Ivanova", LocalDate.parse("1986-08-06"), "495"),
                student("Smolin", LocalDate.parse("1986-08-06"), "495"),
                student("Sidorenko", LocalDate.parse("1986-08-06"), "495"),
                student("Denisova", LocalDate.parse("1985-04-13"), "497"),
                student("Petrenko", LocalDate.parse("1989-06-18"), "497"))
                ).select(String.class, Student::getName).execute();
        int len = 0;
        for (String str : result) {
            len++;
        }
        assertEquals(len, 6);
        result = from(list(
                student("Petrov", LocalDate.parse("1986-08-06"), "494"),
                student("Ivanova", LocalDate.parse("1986-08-06"), "495"),
                student("Smolin", LocalDate.parse("1986-08-06"), "495"),
                student("Sidorenko", LocalDate.parse("1986-08-06"), "495"))
                ).selectDistinct(String.class, Student::getName).execute();
        len = 0;
        for (String str : result) {
            len++;
        }
        assertEquals(len, 4);
    }

    @Test
    public void testFrom() throws Exception {
        Iterable<String> result1 = from(list(
                student("ivanov", LocalDate.parse("1986-08-06"), "491"))
        ).select(String.class,Student::getName).execute();
        Iterable<String> result2 = from(list(
                student("ivanov", LocalDate.parse("1986-08-06"), "491")).stream()
        ).select(String.class, Student::getName).execute();
        assertEquals(result1, result2);
    }

    @Test
    public void testUnion() throws Exception {
        Iterable<String> result = from(list(
                student("Ivanova", LocalDate.parse("1986-08-06"), "495")))
                .select(String.class,Student::getName)
                .union()
                .from(list(
                        student("Utesov", LocalDate.parse("1986-08-06"), "494")))
                .select(String.class, Student::getName)
                .execute();
        int len = 0;
        for (String str : result) {
            len++;
        }
        assertEquals(len, 2);
        Iterable<?> result2 = from(list(
                student("ivanov", LocalDate.parse("1986-08-06"), "491")))
                .select(String.class, Student::getName)
                .union()
                .from(list(
                        student("bykov", LocalDate.parse("1986-08-06"), "491")))
                .select(Statistics.class, s -> "all", s -> 1, s-> 4.0)
                .execute();
    }

    @Test(expected = CollectionException.class)
    public void testException() throws CollectionException {
        Iterable<Statistics> statistics =
                from(list(
                        student("Petrov", LocalDate.parse("1986-08-06"), "494"),
                        student("Ivanova", LocalDate.parse("1986-08-06"), "495"),
                        student("Smolin", LocalDate.parse("1986-08-06"), "495"),
                        student("Utesov", LocalDate.parse("2006-08-06"), "494")))
                        .select(Statistics.class, Student::getGroup, Student::getGroup, avg(Student::age))
                        .where(rlike(Student::getName, ".*ov").and(s -> s.age() > 20))
                        .groupBy(Student::getGroup)
                        .having(s -> s.getCount() > 0)
                        .orderBy(asc(Statistics::getGroup), desc(Statistics::getCount))
                        .limit(100)
                        .union()
                        .from(list(student("Petrov", LocalDate.parse("1985-08-06"), "494")))
                        .selectDistinct(Statistics.class, s -> "all", count(s -> 1), avg(Student::age))
                        .execute();
    }
}
