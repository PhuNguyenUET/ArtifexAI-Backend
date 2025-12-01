package com.Artiom.ArtifexAI.Util;

import com.Artiom.ArtifexAI.User.Model.CustomUserDetails;
import com.Artiom.ArtifexAI.User.Model.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class AuthenticationUtils {
    public static User getCurrentUser() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customUserDetails.getUser();
    }
}
