package com.plog.plogbackend.domain.map.implement;

import com.plog.plogbackend.domain.map.model.RecordSortType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SortTypeConverter implements Converter<String, RecordSortType> {

  @Override
  public RecordSortType convert(String source) {
    return RecordSortType.from(source);
  }
}
