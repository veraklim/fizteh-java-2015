package ru.fizteh.fivt.students.veraklim.CollectionQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;
import ru.fizteh.fivt.students.veraklim.CollectionQuery.CollectionQuery.Student;

import static org.junit.Assert.assertTrue;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.CollectionQuery.Student.student;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.OrderByConditions.asc;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.OrderByConditions.desc;

@RunWith(PowerMockRunner.class)
public class OrderByConditionsTest {
    Student student1, student2, student3;
    @Before
    public void setUp() {
        student("Petrov", LocalDate.parse("1986-08-06"), "494");
        student("Ivanova", LocalDate.parse("1986-08-06"), "495");
        student("Smolin", LocalDate.parse("1986-08-06"), "495");
    }
    @Test
    public void testAsc() {
        assertTrue(asc(Student::getGroup).compare(student1, student2) < 0);
        assertTrue(asc(Student::getGroup).compare(student2, student1) > 0);
        assertTrue(asc(Student::getGroup).compare(student2, student3) == 0);
    }
    @Test
    public void testDesc() {
        assertTrue(desc(Student::getGroup).compare(student1, student2) > 0);
        assertTrue(desc(Student::getGroup).compare(student2, student1) < 0);
        assertTrue(desc(Student::getGroup).compare(student2, student3) == 0);
    }
}
