package com.plog.plogbackend.domain.inquiry.controller.dto.admin;

import com.plog.plogbackend.domain.inquiry.contents.Category;

public record AnswerInquiryRequest(String answerTitle, String answerContent, Category category) {}
