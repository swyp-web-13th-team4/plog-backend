package com.plog.plogbackend.domain.admin.controller;

import com.plog.plogbackend.domain.notice.entity.Notice;
import com.plog.plogbackend.domain.notice.repository.NoticeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** 어드민 페이지 컨트롤러 (Thymeleaf SSR) */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final NoticeRepository noticeRepository;

  /** /admin → /admin/notice 리다이렉트 */
  @GetMapping
  public String adminRoot() {
    return "redirect:/admin/notice";
  }

  /** 권한 에러 페이지 */
  @GetMapping("/error")
  public String errorPage() {
    return "admin/error";
  }

  /** 공지사항 관리 페이지 */
  @GetMapping("/notice")
  public String noticePage(Model model) {
    List<Notice> notices =
        noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "localDateTime"));
    model.addAttribute("notices", notices);
    model.addAttribute("currentMenu", "notice");
    return "admin/notice";
  }

  // =========================================
  // 회원 관리
  // =========================================

  private final com.plog.plogbackend.domain.member.repository.MemberRepository memberRepository;

  /** 내부 DTO: 회원 목록 요청 (다른 도메인과 통일된 page 변수 사용) */
  public record MemberRequest(Integer page, String keyword) {}

  /** 내부 DTO: 회원 통계 */
  public record MemberStatsDto(
      long totalMembers, long adminMembers, long newThisMonth, long deletedMembers) {}

  /** 회원 목록 관리 페이지 */
  @GetMapping("/members")
  public String membersPage(MemberRequest request, Model model) {
    int currentPage = request.page() != null ? request.page() : 0;
    String keyword = request.keyword() != null ? request.keyword().trim() : "";

    // 1. 통계 데이터 로드
    java.time.LocalDateTime startOfMonth =
        java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

    long total = memberRepository.count();
    long admins = memberRepository.countByRole(com.plog.plogbackend.domain.member.enums.Role.ROLE_ADMIN);
    long news = memberRepository.countByCreatedAtAfter(startOfMonth);
    long deleted = memberRepository.countByStatus(com.plog.plogbackend.global.common.Enum.EntityStatus.DELETED);

    MemberStatsDto stats = new MemberStatsDto(total, admins, news, deleted);

    // 2. 검색 및 페이징 로드 (한 페이지 10개)
    org.springframework.data.domain.Pageable pageable =
        org.springframework.data.domain.PageRequest.of(currentPage, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

    org.springframework.data.domain.Page<com.plog.plogbackend.domain.member.Member> memberPage;
    if (keyword.isEmpty()) {
      memberPage = memberRepository.findAll(pageable);
    } else {
      memberPage = memberRepository.findByNicknameContaining(keyword, pageable);
    }

    // 3. 모델에 담기
    model.addAttribute("stats", stats);
    model.addAttribute("members", memberPage);
    model.addAttribute("keyword", keyword);
    model.addAttribute("currentMenu", "members");

    return "admin/members";
  }
}
