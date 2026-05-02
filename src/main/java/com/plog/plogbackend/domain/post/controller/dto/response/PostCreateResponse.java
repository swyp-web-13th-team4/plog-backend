package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import java.util.List;

public record PostCreateResponse(PostResponse texts, List<ImageUrlResponse> images) {}
