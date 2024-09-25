package com.chicchoc.sivillage.domain.review.application;

import com.chicchoc.sivillage.domain.review.domain.Review;
import com.chicchoc.sivillage.domain.review.dto.in.ReviewRequestDto;
import com.chicchoc.sivillage.domain.review.dto.out.ReviewResponseDto;
import com.chicchoc.sivillage.global.common.utils.CursorPage;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ReviewService {

    void addReview(String userUuid, ReviewRequestDto reviewRequestDto, List<MultipartFile> fileList);

    List<ReviewResponseDto> getReviewByProductUuid(String productUuid);

    List<ReviewResponseDto> getReviewByUserUuid(String userUuid);

    void deleteReview(String reviewUuid);

    CursorPage<String> getAllReviews(String productUuid, String userUuid, Long lastId, Integer pageSize,
            Integer page);
}
