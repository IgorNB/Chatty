package com.lig.chatty.controller.adapter.anonymousui;

import com.google.gson.Gson;
import com.lig.chatty.Main;
import com.lig.chatty.controller.adapter.anonymousui.dto.LoginRequestDto;
import com.lig.chatty.domain.*;
import com.lig.chatty.repository.AuthorityRepository;
import com.lig.chatty.repository.ChatRoomRepository;
import com.lig.chatty.repository.ChatRoomUserRepository;
import com.lig.chatty.repository.UserRepository;
import com.lig.chatty.security.config.AppPropertiesConfig;
import com.lig.chatty.security.jwt.TokenProviderService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {"spring.batch.job.enabled=false"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Main.class})
public class AuthControllerTest {


    @Autowired
    private TokenProviderService tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public ChatRoomUserRepository chatRoomUserRepository;

    @Autowired
    public ChatRoomRepository chatRoomRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    public static String authUser(MockMvc mockMvc, LoginRequestDto login) throws Exception {
        Gson gson = new Gson();
        AtomicReference<String> responceBodyRef = new AtomicReference<>();
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(login)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andDo(mvcResult -> responceBodyRef.set(mvcResult.getResponse().getContentAsString()));

        JSONObject responceBodyJsonObject = new JSONObject(responceBodyRef.get());
        return (String) responceBodyJsonObject.get("accessToken");
    }

    @BeforeEach
    public void setup() {
        AuthController userAuthController = new AuthController(authenticationManager, tokenProvider, userRepository, authorityRepository, passwordEncoder, chatRoomUserRepository, chatRoomRepository);
        //threadlocal reset authenticated User after previous tests
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("admin@localhost", "admin"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userAuthController).build();
    }

    @Test
    @Transactional
    public void testExistingUserAuthorize() throws Exception {
        User user = new User();
        user.setName("user-jwt-controller");
        user.setEmail("user-jwt-controller@example.com");
        user.setEmailVerified(true);
        user.setProvider(Authority.AuthProvider.local);
        user.setPassword(passwordEncoder.encode("test"));
        Authority authority = authorityRepository.getAuthorityByName(Authority.Roles.USER);
        if (authority != null) {
            user.getAuthorities().add(authority);
        }

        userRepository.saveAndFlush(user);

        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("user-jwt-controller@example.com");
        login.setPassword("test");
        String jws = authUser(mockMvc, login);

        String jwsWithoutSignatureString = jws.substring(0, jws.lastIndexOf('.') + 1);
        Jwt<Header, Claims> jwsWithoutSignature = Jwts.parser().parseClaimsJwt(jwsWithoutSignatureString);

        assertAll(
                () -> assertThat(jwsWithoutSignature.getHeader().get("alg")).isEqualTo("HS512"),
                () -> assertThat(jwsWithoutSignature.getBody().get("auth")).isEqualTo(Authority.Roles.USER),
                () -> assertThat(jwsWithoutSignature.getBody().get("sub")).isEqualTo(user.getId())
        );

    }

    @Test
    @Transactional
    public void testNewUserAuthorize() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test-user-" + UUID.randomUUID().toString().replaceAll("-", "") + "@example.com");
        loginDto.setPassword("test");
        String jws = authUser(mockMvc, loginDto);

        User user = userRepository.findByEmail(loginDto.getEmail()).orElse(null);
        ChatRoom defaultChatRoom = chatRoomRepository.getOne(AppPropertiesConfig.DEFAULT_ROOM_ID);
        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(defaultChatRoom,user).orElse(null);

        String jwsWithoutSignatureString = jws.substring(0, jws.lastIndexOf('.') + 1);
        Jwt<Header, Claims> jwsWithoutSignature = Jwts.parser().parseClaimsJwt(jwsWithoutSignatureString);

        assertAll(
                () -> assertThat(user).isNotNull(),
                () -> assertThat(chatRoomUser).isNotNull(),
                () -> assertThat(chatRoomUser.getChatRoom().getId()).isEqualToIgnoringCase(defaultChatRoom.getId()),
                () -> assertThat(jwsWithoutSignature.getHeader().get("alg")).isEqualTo("HS512"),
                () -> assertThat(jwsWithoutSignature.getBody().get("auth")).isEqualTo(Authority.Roles.USER),
                () -> assertThat(jwsWithoutSignature.getBody().get("sub")).isEqualTo(user.getId())
        );

    }

}
