package de.bentrm.datacat.graphql.fetcher;

import de.bentrm.datacat.auth.service.*;
import de.bentrm.datacat.auth.service.dto.ProfileDto;
import de.bentrm.datacat.graphql.dto.LoginInput;
import de.bentrm.datacat.graphql.dto.ProfileUpdateInput;
import de.bentrm.datacat.graphql.dto.SignupInput;
import de.bentrm.datacat.graphql.dto.SpecificationMapper;
import graphql.GraphQLError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    private final ProfileService profileService;

    private final SpecificationMapper specificationMapper;

    @ExceptionHandler({
            UsernameTakenException.class,
            EmailTakenException.class,
            EmailUnconfirmedException.class,
            BadCredentialsException.class
    })
    public GraphQLError handleException(Throwable e) {
        log.warn("AUTH_ERROR error={}", e.getMessage());
        return GraphQLError.newError().errorType(ErrorType.BAD_REQUEST).message(e.getMessage()).build();
    }

    @QueryMapping
    public ProfileDto profile() {
        return profileService.getProfile();
    }

    @MutationMapping
    public Boolean signup(@Argument SignupInput input) {
        authenticationService.signup(input);
        log.info("USER_REGISTERED username={}", input.getUsername());
        return true;
    }

    @MutationMapping
    public Boolean confirm(@Argument String token) {
        authenticationService.fulfillEmailConfirmationRequest(token);
        return true;
    }

    @MutationMapping
    public String login(@Argument LoginInput input) {
        String token = authenticationService.login(input.getUsername(), input.getPassword());
        log.info("USER_LOGIN username={}", input.getUsername());
         return token;
    }

    @MutationMapping
    public ProfileDto updateProfile(@Argument ProfileUpdateInput input) {
        return profileService.updateAccount(specificationMapper.toDto(input));
    }

}
