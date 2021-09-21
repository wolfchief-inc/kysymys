package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.EmailAddress;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import net.unit8.kysymys.user.domain.UserName;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserDetailsService userDetailsService;
    private final SaveUserPort saveUserPort;
    private final TransactionTemplate tx;

    public CustomOAuth2UserService(UserDetailsService userDetailsService, SaveUserPort saveUserPort, TransactionTemplate tx) {
        this.userDetailsService = userDetailsService;
        this.saveUserPort = saveUserPort;
        this.tx = tx;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        try {
            return processOAuth2User(request, oAuth2User);
        } catch(Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest request, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        try {
            return (OAuth2User) userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            return tx.execute(status -> {
                User user = User.fromOAuth2(new UserId(),
                        EmailAddress.of(email),
                        UserName.of(oAuth2User.getAttribute("name"))
                );
                saveUserPort.save(user);
                return user;
            });
        }
    }
}
