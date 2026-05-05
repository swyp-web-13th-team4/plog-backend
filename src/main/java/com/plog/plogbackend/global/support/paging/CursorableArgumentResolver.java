package com.plog.plogbackend.global.support.paging;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CursorableArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String CURSOR = "cursor";
  private static final String LIMIT = "limit";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(Cursorable.class);
  }

  @Override
  public @Nullable Object resolveArgument(
      MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory)
      throws Exception {
    CursorDefault annotation = parameter.getParameterAnnotation(CursorDefault.class);

    String limitParam = webRequest.getParameter(LIMIT);
    int limit =
        (limitParam != null)
            ? Integer.parseInt(limitParam)
            : (annotation != null) ? annotation.defaultLimit() : 10;

    String cursorParam = webRequest.getParameter(CURSOR);
    Object cursor = null;

    if (cursorParam != null && binderFactory != null) {
      var binder = binderFactory.createBinder(webRequest, null, CURSOR);
      Class<?> targetType = ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve();
      cursor = binder.convertIfNecessary(cursorParam, targetType);
    }
    return new Cursorable<>(cursor, limit);
  }
}
