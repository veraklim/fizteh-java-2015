package ru.fizteh.fivt.students.veraklim.CollectionQuery.impl;

import java.util.function.Function;

public interface Aggregator<T, R> extends Function<T, R> {
    R apply(Iterable<T> iterable);
}