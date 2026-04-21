package ru.devanalyzer.gateway_service.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void userPrincipal_Getters_ReturnCorrectValues() {

        Long userId = 1L;
        String email = "test@example.com";
        String role = "HR";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_HR"));

        UserPrincipal principal = new UserPrincipal(userId, email, role, authorities);

        assertThat(principal.getUserId()).isEqualTo(userId);
        assertThat(principal.getEmail()).isEqualTo(email);
        assertThat(principal.getRole()).isEqualTo(role);
        assertThat(principal.getUsername()).isEqualTo(email);
        assertThat(principal.getPassword()).isNull();
        assertThat(principal.getAuthorities()).hasSize(1);
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void userPrincipal_EqualsAndHashCode() {

        UserPrincipal principal1 = new UserPrincipal(1L, "test@example.com", "HR", List.of());
        UserPrincipal principal2 = new UserPrincipal(1L, "test@example.com", "HR", List.of());
        UserPrincipal principal3 = new UserPrincipal(2L, "other@example.com", "ADMIN", List.of());

        assertThat(principal1).isEqualTo(principal2);
        assertThat(principal1).isNotEqualTo(principal3);
        assertThat(principal1.hashCode()).isEqualTo(principal2.hashCode());
    }

    @Test
    void userPrincipal_ToString_ContainsFields() {

        UserPrincipal principal = new UserPrincipal(1L, "test@example.com", "HR", List.of());

        String toString = principal.toString();

        assertThat(toString).contains("userId=1");
        assertThat(toString).contains("email=test@example.com");
        assertThat(toString).contains("role=HR");
    }
}
