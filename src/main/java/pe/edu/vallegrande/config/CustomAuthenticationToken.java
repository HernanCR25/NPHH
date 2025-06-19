package pe.edu.vallegrande.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {
    private final Jwt jwt;

    public CustomAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public Object getPrincipal() {
        return jwt.getSubject();
    }

    public Jwt getJwt() {
        return jwt;
    }
}
