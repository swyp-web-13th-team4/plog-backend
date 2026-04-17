package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.post.repository.PostImageRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.util.GcsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostImageService {

  private static final String POST_DIR = "posts";
  private static final int POST_IMAGE_MAX = 5;

  private final GcsService gcsService;
  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;

  /**
   * 게시글 이미지를 GCS에 업로드합니다. 기존 이미지 포함 최대 5개를 초과하면 예외를 발생시킵니다.
   *
   * @param postId 게시글 ID
   * @param files 업로드할 이미지 파일 목록 (1~5개)
   * @return 업로드된 이미지 URL 목록
   */
  @Transactional
  public List<ImageUrlResponse> uploadPostImages(Long postId, List<MultipartFile> files) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));

    if (files == null || files.isEmpty()) {
      throw new AppException(ErrorType.FILE_EMPTY);
    }

    int currentCount = postImageRepository.countByPostId(postId);
    if (currentCount + files.size() > POST_IMAGE_MAX) {
      throw new AppException(ErrorType.POST_IMAGE_LIMIT_EXCEEDED);
    }

    List<ImageUrlResponse> result =
        files.stream()
            .map(
                file -> {
                  String url = gcsService.upload(file, POST_DIR);
                  postImageRepository.save(PostImage.of(url, post));
                  return new ImageUrlResponse(url);
                })
            .toList();

    log.debug("게시글 이미지 업로드 완료 - postId: {}, 업로드 수: {}", postId, files.size());
    return result;
  }

  /**
   * 특정 게시글 이미지 1개를 GCS에서 삭제하고 DB에서도 제거합니다.
   *
   * @param postId 게시글 ID
   * @param imageId 삭제할 이미지 ID
   */
  @Transactional
  public void deletePostImage(Long postId, Long imageId) {
    PostImage postImage =
        postImageRepository
            .findById(imageId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND));

    // 요청한 postId와 실제 이미지의 postId가 일치하는지 검증
    if (!postImage.getPost().getId().equals(postId)) {
      throw new AppException(ErrorType.INVALID_ACCESS_PATH);
    }

    gcsService.delete(postImage.getImageUrl());
    postImageRepository.delete(postImage);
    log.debug("게시글 이미지 삭제 완료 - postId: {}, imageId: {}", postId, imageId);
  }

  /**
   * 특정 게시글의 모든 이미지를 GCS에서 삭제하고 DB에서도 제거합니다.
   *
   * @param postId 게시글 ID
   */
  @Transactional
  public void deleteAllPostImages(Long postId) {
    List<PostImage> images = postImageRepository.findAllByPostId(postId);
    images.forEach(img -> gcsService.delete(img.getImageUrl()));
    postImageRepository.deleteAllByPostId(postId);
    log.debug("게시글 전체 이미지 삭제 완료 - postId: {}, 삭제 수: {}", postId, images.size());
  }

  /**
   * 게시글의 모든 이미지 URL 목록을 조회합니다.
   *
   * @param postId 게시글 ID
   * @return 이미지 URL 목록
   */
  @Transactional(readOnly = true)
  public List<ImageUrlResponse> getPostImages(Long postId) {
    if (!postRepository.existsById(postId)) {
      throw new AppException(ErrorType.POST_NOT_FOUND);
    }
    return postImageRepository.findAllByPostId(postId).stream()
        .map(img -> new ImageUrlResponse(img.getImageUrl()))
        .toList();
  }
}
