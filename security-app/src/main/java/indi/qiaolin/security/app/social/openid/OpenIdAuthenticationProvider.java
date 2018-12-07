package indi.qiaolin.security.app.social.openid;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;

import java.util.HashSet;
import java.util.Set;

/**
 * @author qiaolin
 * @version 2018/12/7
 **/

@Getter
@Setter
public class OpenIdAuthenticationProvider implements AuthenticationProvider {

    private SocialUserDetailsService socialUserDetailsService;

    private UsersConnectionRepository usersConnectionRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        OpenIdAuthenticationToken token = (OpenIdAuthenticationToken) authentication;
        Set<String> providerUserIds = new HashSet<>();
        providerUserIds.add((String) token.getPrincipal());

        Set<String> userIds = usersConnectionRepository.findUserIdsConnectedTo(token.getProviderId(), providerUserIds);

        if(CollectionUtils.isEmpty(userIds) || userIds.size() != 1){
            throw new InternalAuthenticationServiceException("无法获取用户信息！");
        }

        String userId = userIds.iterator().next();

        SocialUserDetails user = socialUserDetailsService.loadUserByUserId(userId);

        if(user == null){
            throw new InternalAuthenticationServiceException("无法获取用户信息！");
        }

        OpenIdAuthenticationToken authenticationToken = new OpenIdAuthenticationToken(user, user.getAuthorities());
        authenticationToken.setDetails(token.getDetails());

        return authenticationToken;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return OpenIdAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
