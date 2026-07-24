package com.plog.plogbackend.domain.notice.service;

import com.plog.plogbackend.domain.notice.controller.dto.NewNoticeRequest;
import com.plog.plogbackend.domain.notice.controller.dto.NoticeRequest;
import com.plog.plogbackend.domain.notice.entity.Notice;
import com.plog.plogbackend.domain.notice.repository.NoticeRepository;
import com.plog.plogbackend.domain.notice.service.dto.NoticeResult;
import com.plog.plogbackend.domain.notice.service.dto.NoticesResult;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

  private final NoticeRepository noticeRepository;

  public List<NoticesResult> noticeResultList(NoticeRequest request) {
    Pageable pageable = PageRequest.of(request.page(), 10, Direction.DESC, "localDateTime");

    Page<Notice> notices = noticeRepository.OrderByIdDesc(pageable);

    List<NoticesResult> list = notices.getContent().stream().map(NoticesResult::from).toList();

    return list;
  }

  public NoticeResult findNotice(Long id) {

    Notice notice =
        noticeRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorType.NOTICE_NOT_FOUND));

    return NoticeResult.from(notice);
  }

  @Transactional
  public Long create(NewNoticeRequest request) {
    Notice notice = Notice.builder().title(request.title()).content(request.content()).build();
    noticeRepository.save(notice);

    return notice.getId();
  }

  public void delete(Long id) {
    Notice notice =
        noticeRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorType.NOTICE_NOT_FOUND));
    noticeRepository.deleteById(id);
  }

  @Transactional
  public void updateNotice(Long id, NewNoticeRequest request) {

    Notice notice =
        noticeRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorType.NOTICE_NOT_FOUND));

    notice.update(request.title(), request.content());
  }
}
