package com.chicchoc.sivillage.global.auth.dto.in;

import com.chicchoc.sivillage.domain.member.domain.Member;
import com.chicchoc.sivillage.domain.oauth.domain.OauthMember;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignInRequestDto {

    @Email
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    private boolean isAutoSignIn;

    private String oauthId;

    private String oauthProvider;

    private String oauthEmail;


    @Builder
    public SignInRequestDto(String email, String password, boolean isAutoSignIn) {
        this.email = email;
        this.password = password;
        this.isAutoSignIn = isAutoSignIn;
    }

    // Dto -> OauthEntity
    public OauthMember toOauthEntity(Member member) {
        return OauthMember.builder()
                .oauthId(oauthId)
                .oauthProvider(oauthProvider)
                .oauthEmail(oauthEmail)
                .member(member)
                .build();
    }
}
