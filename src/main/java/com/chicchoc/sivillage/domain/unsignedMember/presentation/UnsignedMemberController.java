package com.chicchoc.sivillage.domain.unsignedMember.presentation;

import com.chicchoc.sivillage.domain.unsignedMember.application.UnsignedMemberService;
import com.chicchoc.sivillage.domain.unsignedMember.dto.out.UnsignedMemberResponseDto;
import com.chicchoc.sivillage.domain.unsignedMember.vo.out.UnsignedMemberResponseVo;
import com.chicchoc.sivillage.global.common.entity.BaseResponse;
import jakarta.servlet.http.Cookie;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/unsignedMember")
@Slf4j
public class UnsignedMemberController {

    private final UnsignedMemberService unsignedMemberService;

    @Operation(summary = "getUnsignedMember API", description = "비회원 조회", tags = {"비회원"})
    @GetMapping
    public ResponseEntity<UnsignedMemberResponseVo> getUnsignedMember(HttpServletResponse response) {

        UnsignedMemberResponseDto unsignedMember = unsignedMemberService.createUnsignedMember();

        log.info("uuid : {}", unsignedMember.getUserUuid());

        ResponseCookie cookie = ResponseCookie.from("X-Unsigned-User-UUID", unsignedMember.getUserUuid())
                .path("/")
                .httpOnly(true)
                .maxAge(60 * 60 * 24 * 30)
                .secure(false)
                .sameSite("None")
                .domain("localhost")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(unsignedMember.toVo());
    }

    @PutMapping
    public void updateLastConnectedAt(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String unsignedUserUuid = null;

        for (Cookie cookie : cookies) {
            if ("X-Unsigned-User-UUID".equals(cookie.getName())) {
                unsignedUserUuid = cookie.getValue();

                Cookie updatedCookie = new Cookie("X-Unsigned-User-UUID", unsignedUserUuid);
                updatedCookie.setHttpOnly(true);
                updatedCookie.setPath("/");
                updatedCookie.setMaxAge(60 * 60 * 24 * 30);
                response.addCookie(updatedCookie);
                break;
            }
        }

        unsignedMemberService.updateUnsignedMember(unsignedUserUuid);
    }
}
