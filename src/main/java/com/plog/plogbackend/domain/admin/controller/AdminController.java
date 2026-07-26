package com.plog.plogbackend.domain.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

  @GetMapping("/login")
  public String loginPage(
      @RequestParam(value = "error", required = false) String error, Model model) {
    if ("forbidden".equals(error)) {
      model.addAttribute("errorMessage", "관리자 권한이 없는 계정입니다.");
    }
    return "admin/login";
  }

  @GetMapping("/dashboard")
  public String dashboard() {
    return "admin/dashboard";
  }

  @GetMapping("/notice")
  public String noticePage() {
    return "admin/notice";
  }

  @GetMapping("/inquiry")
  public String inquiryPage() {
    return "admin/inquiry";
  }
}
