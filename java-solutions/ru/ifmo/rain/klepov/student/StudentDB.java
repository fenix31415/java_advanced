package ru.ifmo.rain.klepov.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedStudentGroupQuery {
    private static Comparator<Student> IDCMP = Comparator.comparingInt(Student::getId);
    private static Comparator<Student> CMP = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName).thenComparing(Student::getId);

    @Override
    public List<Group> getGroupsByName(Collection<Student> collection) {
        return getGroups(collection, CMP);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> collection) {
        return getGroups(collection, IDCMP);
    }

    @Override
    public String getLargestGroup(Collection<Student> collection) {
        return getLargestGroup(collection, List::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> collection) {
        return getLargestGroup(collection, students -> getDistinctFirstNames(students).size());
    }

    @Override
    public List<String> getFirstNames(List<Student> list) {
        return getInfo(list, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> list) {
        return getInfo(list, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> list) {
        return getInfo(list, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> list) {
        return getInfo(list, StudentDB::fullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> list) {
        return getDistinct(list, Student::getFirstName);
    }

    @Override
    public String getMinStudentFirstName(List<Student> list) {
        return list.stream()
                .min(IDCMP)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> collection) {
        return sortAndCollect(collection.stream(), IDCMP, ArrayList::new);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> collection) {
        return sortAndCollect(collection.stream(), CMP, ArrayList::new);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> collection, String s) {
        return filterAndSort(collection.stream(), x -> x.getFirstName().equals(s));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> collection, String s) {
        return filterAndSort(collection.stream(), x -> x.getLastName().equals(s));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> collection, String s) {
        return filterAndSort(collection.stream(), x -> x.getGroup().equals(s));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> collection, String s) {
        return collection.stream()
                .filter(x -> x.getGroup().equals(s))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)
                ));
    }

    private static String fullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    private <C extends Collection<Student>> C sortAndCollect(Stream<Student> students,
                                                             Comparator<Student> comp, Supplier<C> supplier) {
        return students.sorted(comp)
                .collect(Collectors.toCollection(supplier));
    }

    private List<Student> filterAndSort(Stream<Student> students, Predicate<Student> predicate) {
        return sortAndCollect(students.filter(predicate), CMP, ArrayList::new);
    }

    private String getMaxEntry(Stream<Map.Entry<String, List<Student>>> stream,
                               Function<List<Student>, Integer> filter,
                               Comparator<String> keycmp) {
        return stream
                .map(x -> Map.entry(x.getKey(), filter.apply(x.getValue())))
                .max(Comparator.<Map.Entry<String, Integer>> comparingInt(Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey, keycmp))
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private String getLargestGroup(Collection<Student> students, Function<List<Student>, Integer> filter) {
        return getMaxEntry(getGroupsStream(students, Student::getGroup), filter, Comparator.reverseOrder());
    }

    private Stream<Map.Entry<String, List<Student>>> getGroupsStream(Collection<Student> students,
                                                                     Function<Student, String> extractor) {
        return students.stream()
                .collect(Collectors.groupingBy(extractor, TreeMap::new, Collectors.toList()))
                .entrySet()
                .stream();
    }

    private static Set<String> getDistinct(List<Student> students, Function<Student, String> getVal) {
        return getInfo(students, getVal, TreeSet::new);
    }

    private static <T, C extends Collection<T>> C getInfo(List<Student> students,
                                                          Function<Student, T> function, Supplier<C> collection) {
        return students.stream()
                .map(function)
                .collect(Collectors.toCollection(collection));
    }

    private List<String> getInfo(List<Student> students, Function<Student, String> function) {
        return getInfo(students, function, ArrayList::new);
    }

    private static List<Student> sortByGivenComparator(Stream<Student> stream, Comparator<Student> comparator) {
        return stream.sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Group> getGroups(Collection<Student> collection, Comparator<Student> order) {
        return getGroupsStream(collection, Student::getGroup)
                .map(x -> new Group(x.getKey(), sortByGivenComparator(x.getValue().stream(), order)))
                .collect(Collectors.toList());
    }

    private static Set<String> getDistinctGroups(List<Student> students) {
        return getDistinct(students, Student::getGroup);
    }

    @Override
    public String getMostPopularName(Collection<Student> collection) {
        return getMaxEntry(getGroupsStream(collection, StudentDB::fullName),
                x -> getDistinctGroups(x).size(), String::compareTo);
    }

    private List<String> getStudentById(List<Student> students, int[] indexes, Function<Student, String> f) {
        return Arrays.stream(indexes)
                .mapToObj(students::get)
                .map(f)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(Collection<Student> collection, int[] inds) {
        return getStudentById(new ArrayList<>(collection), inds, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> collection, int[] inds) {
        return getStudentById(new ArrayList<>(collection), inds, Student::getLastName);
    }

    @Override
    public List<String> getGroups(Collection<Student> collection, int[] inds) {
        return getStudentById(new ArrayList<>(collection), inds, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> collection, int[] inds) {
        return getStudentById(new ArrayList<>(collection), inds, StudentDB::fullName);
    }
}
