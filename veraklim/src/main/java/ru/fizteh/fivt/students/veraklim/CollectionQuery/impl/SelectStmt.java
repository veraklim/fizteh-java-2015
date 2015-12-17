package ru.fizteh.fivt.students.veraklim.CollectionQuery.impl;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SelectStmt<T, R> implements Query<R> {
    private List<R> lastResult;
    private List<T> data;
    private Function<T, ?>[] functions;
    private Class rClass;
    private List<Function<T, ?>> groupByExpressions;
    private Predicate<R> havingCondition;
    private Comparator<R>[] orderByComparators;
    private Integer resultMaxSize;
    private Boolean isDistinct;

    public SelectStmt(Iterable<T> data, Class<R> rClass, Function<T, ?>[] functions, Boolean isDistinct, Iterable<R> lastResult) {
        this.data = new LinkedList<>();
        data.forEach(this.data::add);
        this.lastResult = new LinkedList<>();
        lastResult.forEach(this.lastResult::add);
        this.functions = functions;
        this.rClass = rClass;
        this.isDistinct = isDistinct;
        groupByExpressions = new LinkedList<>();
        havingCondition = element -> true;
        orderByComparators = new Comparator[0];
        resultMaxSize = Integer.MAX_VALUE;
    }

    public SelectStmt<T, R> where(Predicate<T> predicate) {
        List<T> newData = new LinkedList<>();
        data.forEach(element -> {
            if (predicate.test(element)) {
                newData.add(element);
            }
        });
        data = newData;
        return this;
    }
    @SafeVarargs
    public final SelectStmt<T, R> groupBy(Function<T, ?>... expressions) {
        groupByExpressions = Arrays.asList(expressions);
        return this;
    }
    @SafeVarargs
    public final SelectStmt<T, R> orderBy(Comparator<R>... comparators) {
        orderByComparators = comparators;
        return this;
    }
    public SelectStmt<T, R> having(Predicate<R> condition) {
        havingCondition = condition;
        return this;
    }

    public SelectStmt<T, R> limit(int amount) {
        resultMaxSize = amount;
        return this;
    }
    public UnionStmt<R> union() {
        return new UnionStmt(execute());
    }
    @Override
    public Iterable<R> execute() {
        List<R> result = new LinkedList<>();
        Set<R> distinctResult = new HashSet<>();
        List<List<T>> groupedData = new LinkedList<>();
        if (groupByExpressions.size() == 0) {
            data.forEach(element -> groupedData.add(Arrays.asList(element)));
        } else {
            Map<String, List<T>> groups = new HashMap<>();
            data.forEach(element -> {
                StringBuilder groupName = new StringBuilder();
                groupByExpressions.forEach(expression -> groupName.append(expression.apply(element).toString()));
                if (!groups.containsKey(groupName.toString())) {
                    groups.put(groupName.toString(), new LinkedList<>());
                }
                groups.get(groupName.toString()).add(element);
            });
            groups.forEach((key, value) -> groupedData.add(value));
        }
        for (List<T> elements : groupedData) {
            Object[] args = new Object[functions.length];
            Class[] argClasses = new Class[functions.length];
            for (int i = 0; i < functions.length; i++) {
                if (functions[i] instanceof Aggregator) {
                    args[i] = ((Aggregator) functions[i]).apply(elements);
                } else {
                    args[i] = functions[i].apply(elements.get(0));
                }
                argClasses[i] = args[i].getClass();
            }
            R newElement;
            try {
                newElement = (R) rClass.getConstructor(argClasses).newInstance(args);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                throw new RuntimeException();
            }
            if (!isDistinct) {
                if (result.size() < resultMaxSize && havingCondition.test(newElement)) {
                    result.add(newElement);
                }
            } else {
                if (!distinctResult.contains(newElement) && havingCondition.test(newElement)
                        && distinctResult.size() < resultMaxSize) {
                    result.add(newElement);
                    distinctResult.add(newElement);
                }
            }
        }
        for (int i = orderByComparators.length - 1; i >= 0; i--) {
            Collections.sort(result, orderByComparators[i]);
        }
        List<R> newResult = lastResult;
        newResult.addAll(result);
        return newResult;
    }

    @Override
    public Stream<R> stream() {
        List<R> result = new LinkedList<>();
        execute().forEach(result::add);
        return result.stream();
    }
}
