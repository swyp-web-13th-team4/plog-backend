package com.plog.plogbackend.domain.block.service;

import com.plog.plogbackend.domain.block.controller.dto.BlockedUserResponse;
import com.plog.plogbackend.domain.block.entity.Block;
import com.plog.plogbackend.domain.block.repository.BlockRepository;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

  private final BlockRepository blockRepository;
  private final MemberRepository memberRepository;

  /**
   * 유저 차단.
   *
   * <p>자기 자신 차단 불가, 대상 유저 존재 확인, 이미 차단된 경우 예외 발생.
   *
   * @param blockerKey 차단하는 유저의 memberKey
   * @param targetKey 차단할 대상 유저의 memberKey
   */
  @Transactional
  public void blockUser(UUID blockerKey, UUID targetKey) {
    if (blockerKey.equals(targetKey)) {
      throw new AppException(ErrorType.CANNOT_BLOCK_SELF);
    }

    Member blocker = getMember(blockerKey);
    Member blocked = getMember(targetKey);

    if (blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blocked.getId())) {
      throw new AppException(ErrorType.ALREADY_BLOCKED);
    }

    blockRepository.save(new Block(blocker, blocked));
    log.info(
        "User Blocked - Blocker: {} ({}), Blocked: {} ({})",
        blocker.getNickname(),
        blocker.getMemberKey(),
        blocked.getNickname(),
        blocked.getMemberKey());
  }

  /**
   * 유저 차단 해제.
   *
   * <p>차단 관계가 없는 경우 예외 발생.
   *
   * @param blockerKey 차단을 해제하는 유저의 memberKey
   * @param targetKey 차단 해제할 대상 유저의 memberKey
   */
  @Transactional
  public void unblockUser(UUID blockerKey, UUID targetKey) {
    Member blocker = getMember(blockerKey);
    Member blocked = getMember(targetKey);

    Block block =
        blockRepository
            .findByBlockerIdAndBlockedId(blocker.getId(), blocked.getId())
            .orElseThrow(() -> new AppException(ErrorType.NOT_BLOCKED));

    blockRepository.delete(block);
    log.info(
        "User Unblocked - Blocker: {} ({}), Unblocked: {} ({})",
        blocker.getNickname(),
        blocker.getMemberKey(),
        blocked.getNickname(),
        blocked.getMemberKey());
  }

  /**
   * 내가 차단한 유저 목록 조회.
   *
   * @param blockerKey 로그인한 유저의 memberKey
   * @return 차단한 유저 정보 리스트
   */
  public List<BlockedUserResponse> getBlockedUsers(UUID blockerKey) {
    Member blocker = getMember(blockerKey);

    return blockRepository.findAllByBlockerId(blocker.getId()).stream()
        .map(
            block ->
                new BlockedUserResponse(
                    block.getBlocked().getMemberKey(),
                    block.getBlocked().getNickname(),
                    block.getBlocked().getProfileImage()))
        .toList();
  }

  /**
   * 이 유저가 내가 차단한 유저인지 확인합니다.
   *
   * @param blockerKey 내 memberKey
   * @param targetKey 차단 여부를 확인할 대상의 memberKey
   * @return 차단했으면 true, 아니면 false
   */
  public boolean isBlockedUser(UUID blockerKey, UUID targetKey) {
    if (blockerKey.equals(targetKey)) {
      return false;
    }
    Member blocker = getMember(blockerKey);
    Member blocked = getMember(targetKey);
    return blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blocked.getId());
  }

  private Member getMember(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
  }
}
