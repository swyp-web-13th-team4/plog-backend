package com.plog.plogbackend.domain.block.controller.dto;

import java.util.UUID;

public record BlockedUserResponse(UUID memberKey, String nickname, String profileImageUrl) {}
