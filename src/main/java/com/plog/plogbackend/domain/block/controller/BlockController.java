package com.plog.plogbackend.domain.block.controller;

import com.plog.plogbackend.domain.block.service.BlockService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "차단", description = "유저 차단 관련 API")
@RestController
@RequestMapping("/api/members/block")
@RequiredArgsConstructor
public class BlockController {

  private final BlockService blockService;

  @Operation(summary = "유저 차단", description = "특정 유저를 차단합니다. 차단된 유저의 게시글은 피드에서 노출되지 않습니다.")
  @PostMapping("/{targetMemberKey}")
  public ResponseEntity<ApiResponse<Void>> blockUser(
      @PathVariable UUID targetMemberKey,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    blockService.blockUser(memberKey, targetMemberKey);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(summary = "유저 차단 해제", description = "차단한 유저의 차단을 해제합니다.")
  @DeleteMapping("/{targetMemberKey}")
  public ResponseEntity<ApiResponse<Void>> unblockUser(
      @PathVariable UUID targetMemberKey,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    blockService.unblockUser(memberKey, targetMemberKey);
    return ResponseEntity.ok(ApiResponse.success());
  }
}
