package com.chicchoc.sivillage.global.auth.application;

import com.chicchoc.sivillage.domain.member.domain.Member;
import com.chicchoc.sivillage.domain.member.infrastructure.MemberRepository;
import com.chicchoc.sivillage.global.auth.domain.EmailVerification;
import com.chicchoc.sivillage.global.auth.dto.in.CheckEmailRequestDto;
import com.chicchoc.sivillage.global.auth.dto.in.EmailVerificationRequestDto;
import com.chicchoc.sivillage.global.auth.dto.in.FindEmailRequestDto;
import com.chicchoc.sivillage.global.auth.dto.in.SignInRequestDto;
import com.chicchoc.sivillage.global.auth.dto.in.SignUpRequestDto;
import com.chicchoc.sivillage.global.auth.dto.out.FindEmailResponseDto;
import com.chicchoc.sivillage.global.auth.dto.out.SignInResponseDto;
import com.chicchoc.sivillage.global.auth.infrastructure.EmailVerificationRepository;
import com.chicchoc.sivillage.global.auth.provider.EmailProvider;
import com.chicchoc.sivillage.global.common.entity.BaseResponseStatus;
import com.chicchoc.sivillage.global.common.generator.NanoIdGenerator;
import com.chicchoc.sivillage.global.common.generator.VerificationCode;
import com.chicchoc.sivillage.global.error.exception.BaseException;
import com.chicchoc.sivillage.global.jwt.application.JwtTokenProvider;
import com.chicchoc.sivillage.global.jwt.application.RefreshTokenService;
import com.chicchoc.sivillage.global.jwt.config.JwtProperties;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final EmailProvider emailProvider;
    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void signUp(SignUpRequestDto signUpRequestDto) {

        String uuid = new NanoIdGenerator().generateNanoId();

        // 중복된 이름과 전화번호가 존재할 경우 예외 처리
        if (memberRepository.existsByNameAndPhone(signUpRequestDto.getName(), signUpRequestDto.getPhone())) {
            throw new BaseException(BaseResponseStatus.DUPLICATED_NAME_AND_PHONE);
        }

        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());
        Member member = signUpRequestDto.toEntity(uuid, encodedPassword);

        memberRepository.save(member);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SignInResponseDto signIn(SignInRequestDto signInRequestDto) {

        //아이디 검증
        Member member = memberRepository.findByEmail(signInRequestDto.getEmail())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAILED_TO_LOGIN));

        //인증 객체 생성
        Authentication authentication = authenticateMember(member, signInRequestDto.getPassword());

        //토큰 생성
        String accessToken = jwtTokenProvider.generateToken(authentication, jwtProperties.getAccessExpireTime());
        String refreshToken = jwtTokenProvider.generateToken(authentication, jwtProperties.getRefreshExpireTime());
        refreshTokenService.saveOrUpdateRefreshToken(member.getUuid(), refreshToken);

        return new SignInResponseDto(accessToken, refreshToken, member.getUuid());
    }

    @Transactional(readOnly = true)
    @Override
    public void checkEmail(CheckEmailRequestDto checkEmailRequestDto) {

        if (memberRepository.existsByEmail(checkEmailRequestDto.getEmail())) {
            throw new BaseException(BaseResponseStatus.DUPLICATED_EMAIL);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public FindEmailResponseDto findEmail(FindEmailRequestDto dto) {

        String email = memberRepository.findEmailByNameAndPhoneNumber(dto.getName(), dto.getPhone())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_USER));

        return new FindEmailResponseDto(maskEmail(email));

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void verifyEmail(EmailVerificationRequestDto requestDto) {

        String userEmail = requestDto.getEmail();
        String verificationCode = VerificationCode.generateCode();

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(userEmail)) {
            log.error("이미 사용중인 이메일입니다.");
            throw new BaseException(BaseResponseStatus.DUPLICATED_EMAIL);
        }

        // 전송, 실패시 예외처리
        boolean isSent = emailProvider.sendVerificationEmail(userEmail, verificationCode);
        if (!isSent) {
            throw new BaseException(BaseResponseStatus.FAILED_TO_SEND_EMAIL);
        }

        // 이메일 인증 정보 DB 저장
        EmailVerification emailVerification = EmailVerification.builder()
                .email(userEmail)
                .verificationCode(verificationCode)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        emailVerificationRepository.save(emailVerification);

    }

    private Authentication authenticateMember(Member member, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        member.getUuid(), password
                )
        );
    }

    private String maskEmail(String email) {
        String[] splitEmail = email.split("@");
        String local = splitEmail[0];
        String domain = splitEmail[1];
        int localLength = local.length();

        // 로컬 길이에 따라 마스킹 처리
        String maskedLocalPart;
        if (localLength > 3) {
            maskedLocalPart = local.substring(0, 3) + "*".repeat(localLength - 3);
        } else {
            maskedLocalPart = local.charAt(0) + "*".repeat(localLength - 1);
        }

        return maskedLocalPart + "@" + domain;
    }
}
