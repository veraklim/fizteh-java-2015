package ru.fizteh.fivt.students.veraklim.CollectionQuery;

import ru.fizteh.fivt.students.veraklim.CollectionQuery.impl.Tuple;
import ru.fizteh.fivt.students.veraklim.CollectionQuery.impl.CollectionException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.List;

import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Aggregates.avg;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Aggregates.count;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.CollectionQuery.Student.student;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Conditions.rlike;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.OrderByConditions.asc;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.OrderByConditions.desc;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.Sources.list;
import static ru.fizteh.fivt.students.veraklim.CollectionQuery.impl.FromStmt.from;


/**
 * @author akormushin
 */
public class CollectionQuery {

    /**
     * Make this code work!
     *
     * @param args
     */
    public static void main(String[] args) {

        List<Student> students = list(
                student("Petrov", LocalDate.parse("1986-08-06"), "494"),
                student("Ivanova", LocalDate.parse("1986-08-06"), "495"),
                student("Smolin", LocalDate.parse("1986-08-06"), "495"),
                student("Sidorenko", LocalDate.parse("1986-08-06"), "495"),
                student("Denisova", LocalDate.parse("1985-04-13"), "497"),
                student("Petrenko", LocalDate.parse("1989-06-18"), "497"),
                student("Utesov", LocalDate.parse("2006-08-06"), "494"));
        List<Group> groups = list(new Group("494", "Mr.Petrov"),
                new Group("495", "Mr.Sidorenko"),
                new Group("497", "Ms.Denisova"));

        Iterable<Statistics> statistics =
                from(list(
                        student("Petrov", LocalDate.parse("1986-08-06"), "494"),
                        student("Ivanova", LocalDate.parse("1986-08-06"), "495"),
                        student("Sidorenko", LocalDate.parse("1986-08-06"), "495"),
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
        System.out.println(statistics);

        Iterable<Tuple<String, String>> mentorsByStudent =
                from(list(student("ivanov", LocalDate.parse("1985-08-06"), "494")))
                        .join(list(new Group("494", "mr.sidorov")))
                        .on((s, g) -> Objects.equals(s.getGroup(), g.getGroup()))
                        .select(sg -> sg.getFirst().getName(), sg -> sg.getSecond().getMentor())
                        .execute();
        System.out.println(mentorsByStudent);
    }


    public static class Student {
        private final String name;

        private final LocalDate dateOfBith;

        private final String group;

        public String getName() {
            return name;
        }

        public Student(String name, LocalDate dateOfBith, String group) {
            this.name = name;
            this.dateOfBith = dateOfBith;
            this.group = group;
        }

        public LocalDate getDateOfBith() {
            return dateOfBith;
        }

        public String getGroup() {
            return group;
        }

        public long age() {
            return ChronoUnit.YEARS.between(getDateOfBith(), LocalDateTime.now());
        }

        public static Student student(String name, LocalDate dateOfBith, String group) {
            return new Student(name, dateOfBith, group);
        }
    }

    public static class Group {
        private final String group;
        private final String mentor;

        public Group(String group, String mentor) {
            this.group = group;
            this.mentor = mentor;
        }

        public String getGroup() {
            return group;
        }

        public String getMentor() {
            return mentor;
        }
    }


    public static class Statistics {

        private final String group;
        private final Long count;
        private final Double age;

        public String getGroup() {
            return group;
        }

        public Long getCount() {
            return count;
        }

        public Double getAge() {
            return age;
        }

        public Statistics(String group, Long count, Double age) {
            this.group = group;
            this.count = count;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Statistics{"
                    + "group='" + group + '\''
                    + ", count=" + count
                    + ", age=" + age
                    + '}';
        }
    }

}