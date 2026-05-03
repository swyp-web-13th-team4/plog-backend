package com.plog.plogbackend.global.support.paging;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Slice<T> {
    private final List<T> content;
    private final Cursorable<?> cursorable;
    private final boolean hasNext;

    public <U> Slice<U> map(Function<T, U> converter){
        return new Slice<>(content.stream().map(converter).toList(),cursorable,hasNext);
    }
}
