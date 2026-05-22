package com.plog.plogbackend.global.support.paging;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
  private final String nextCursor;

  public static <T> Slice<T> of(
      List<T> content, Cursorable<?> cursorable, Function<T, String> cursorExtractor) {
    boolean hasNext = content.size() > cursorable.getLimit();
    if (hasNext) {
      content.remove(content.size() - 1);
    }
    String nextCursor = null;
    if (hasNext && !content.isEmpty()) {
      String raw = cursorExtractor.apply(content.get(content.size() - 1));
      nextCursor =
          Base64.getUrlEncoder()
              .withoutPadding()
              .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
    return new Slice<>(content, cursorable, hasNext, nextCursor);
  }

  public <U> Slice<U> map(Function<T, U> converter) {
    return new Slice<>(content.stream().map(converter).toList(), cursorable, hasNext, nextCursor);
  }
}
