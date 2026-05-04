package org.tritonkor.controlabackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tritonkor.controlabackend.common.entity.AuditableEntity;

import java.sql.Types;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends AuditableEntity implements UserDetails {

    @Column(unique = true, nullable = false)
    private String email;

    private String hashPassword;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean isApproved = false;
    private Boolean isActive = true;

    @JdbcTypeCode(Types.BINARY)
    @Column(name = "avatar", columnDefinition = "BYTEA")
    private byte[] avatar;

    public User(String email, String hashPassword, Role role) {
        this.email = email;
        this.hashPassword = hashPassword;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return hashPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
}
