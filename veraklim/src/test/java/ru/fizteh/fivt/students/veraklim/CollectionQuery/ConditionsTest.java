package ru.fizteh.fivt.students.veraklim.CollectionQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;

import ru.fizteh.fivt.students.veraklim.CollectionQuery.CollectionQuery.Student;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.CollectionQuery.Student.student;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Conditions.like;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Conditions.rlike;

@RunWith(PowerMockRunner.class)
public class ConditionsTest {
    @Test
    public void testLike() {
        assertTrue(like(Student::getName, "%_ov").test(student("Petrov", LocalDate.parse("1986-08-06"), "494")));
        assertFalse(like(Student::getName, "%_ov").test(student("Sidorenko", LocalDate.parse("1986-08-06"), "495")));
    }

    @Test
    public void testRlike() {
        assertTrue(rlike(Student::getName, ".*ov").test(student("Petrov", LocalDate.parse("1986-08-06"), "494")));
        assertFalse(rlike(Student::getName, ".*ov").test(student("Sidorenko", LocalDate.parse("1986-08-06"), "495")));
    }
}

