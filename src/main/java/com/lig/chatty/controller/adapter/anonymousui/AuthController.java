package com.lig.chatty.controller.adapter.anonymousui;

import com.lig.chatty.controller.adapter.anonymousui.dto.AuthResponseDto;
import com.lig.chatty.controller.adapter.anonymousui.dto.LoginRequestDto;
import com.lig.chatty.domain.Authority;
import com.lig.chatty.domain.ChatRoom;
import com.lig.chatty.domain.ChatRoomUser;
import com.lig.chatty.domain.User;
import com.lig.chatty.repository.AuthorityRepository;
import com.lig.chatty.repository.ChatRoomRepository;
import com.lig.chatty.repository.ChatRoomUserRepository;
import com.lig.chatty.repository.UserRepository;
import com.lig.chatty.security.config.AppPropertiesConfig;
import com.lig.chatty.security.jwt.TokenProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(AuthController.FORM_AUTH_REQUEST_MAPPING)
public class AuthController {
    public static final String FORM_AUTH_REQUEST_MAPPING = "/auth";
    public static final String PUBLIC_API_ANT_MATCHER = FORM_AUTH_REQUEST_MAPPING + "/**";

    private final AuthenticationManager authenticationManager;
    private final TokenProviderService tokenProviderService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatRoomRepository chatRoomRepository;


    @Autowired
    public AuthController(AuthenticationManager authenticationManager, TokenProviderService tokenProviderService, UserRepository userRepository, AuthorityRepository authorityRepository, PasswordEncoder passwordEncoder, ChatRoomUserRepository chatRoomUserRepository, ChatRoomRepository chatRoomRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProviderService = tokenProviderService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.chatRoomUserRepository = chatRoomUserRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {

        if (!userRepository.findByEmail(loginRequestDto.getEmail()).isPresent()) {
            subscribeUserToDefaultChatRoom(registerNewUser(loginRequestDto));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );


        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProviderService.createToken(authentication, false);
        return ResponseEntity.ok(new AuthResponseDto(token));
    }

    protected User registerNewUser(LoginRequestDto loginRequestDto) {
        User user = new User();

        user.setProvider(Authority.AuthProvider.local);
        user.setName(loginRequestDto.getEmail());
        user.setEmail(loginRequestDto.getEmail());
        user.setImageUrl(null);

        Authority authority = authorityRepository.getAuthorityByName(Authority.Roles.USER);
        if (authority != null) {
            user.getAuthorities().add(authority);
        }

        user.setPassword(passwordEncoder.encode(loginRequestDto.getPassword()));
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    protected void subscribeUserToDefaultChatRoom(User user) {
        ChatRoom defaultChatRoom = chatRoomRepository.getOne(AppPropertiesConfig.DEFAULT_ROOM_ID);
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setChatRoom(defaultChatRoom);
        chatRoomUser.setUser(user);
        chatRoomUserRepository.save(chatRoomUser);
    }

}
