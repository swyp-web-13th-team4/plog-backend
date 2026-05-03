package com.plog.plogbackend.global.support.paging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Cursorable<T> {

    private final T cursor;
    private  final int limit;
}
