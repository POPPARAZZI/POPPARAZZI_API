package com.spoons.popparazzi.jwt.security;

import com.spoons.popparazzi.member.entity.Member;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final String memberCode;
    private final String memberUuid;
    private final String memberId;
    private final String password;
    private final SnsType snsType;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Member member) {
        this.memberCode = member.getMemberCode();
        this.memberUuid = member.getMemberUuid();
        this.memberId = member.getMemberId();
        this.password = member.getSnsType() == SnsType.E
                ? member.getMemberPwd()
                : "";
        this.snsType = member.getSnsType();
        this.authorities = List.of(new SimpleGrantedAuthority(member.getRole().name()));
    }

    @Override public String getUsername() { return memberId; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
