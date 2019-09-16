package com.lig.chatty.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lig.chatty.controller.adapter.anonymousui.AuthControllerTest;
import com.lig.chatty.controller.adapter.anonymousui.dto.LoginRequestDto;
import com.lig.chatty.domain.*;
import com.lig.chatty.domain.core.PersistentObject;
import com.lig.chatty.repository.*;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestUtil {
    private TestUtil() {
    }

    @NotNull
    public static ChatRoom createAndSaveChatRoom(ChatRoomRepository chatRoomRepository) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName("test-chat-room-name" + UUID.randomUUID().toString().replaceAll("-", ""));
        chatRoom = chatRoomRepository.saveAndFlush(chatRoom);
        return chatRoom;
    }

    @NotNull
    public static ChatRoomMessage createAndSaveChatRoomMessage(User user, ChatRoom chatRoom, ChatRoomMessageRepository chatRoomMessageRepository) {
        ChatRoomMessage chatRoomMessage = new ChatRoomMessage();
        chatRoomMessage.setAuthor(user);
        chatRoomMessage.setChatRoom(chatRoom);
        chatRoomMessage = chatRoomMessageRepository.saveAndFlush(chatRoomMessage);
        return chatRoomMessage;
    }

    @NotNull
    public static ChatRoomUser createAndSaveChatRoomUser(User user, ChatRoom chatRoom, ChatRoomUserRepository chatRoomUserRepository) {
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setUser(user);
        chatRoomUser.setChatRoom(chatRoom);
        chatRoomUser = chatRoomUserRepository.saveAndFlush(chatRoomUser);
        return chatRoomUser;
    }

    @NotNull
    public static User createAndSaveUserWithUserRole(PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository, UserRepository userRepository) {
        User user = new User();
        user.setName("user-jwt-controller" + UUID.randomUUID().toString().replaceAll("-", ""));
        user.setEmail("user-jwt-controller" + UUID.randomUUID().toString().replaceAll("-", "") + "@example.com");
        user.setEmailVerified(true);
        user.setProvider(Authority.AuthProvider.local);
        user.setPassword(passwordEncoder.encode("test"));

        Authority authority = authorityRepository.getAuthorityByName(Authority.Roles.USER);
        if (authority != null) {
            user.getAuthorities().add(authority);
        }

        return  userRepository.saveAndFlush(user);
    }

    @NotNull
    public static User createAndSaveUserWithAdminRole(PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository, UserRepository userRepository) {
        User user = new User();
        user.setName("user-jwt-controller" + UUID.randomUUID().toString().replaceAll("-", ""));
        user.setEmail("user-jwt-controller" + UUID.randomUUID().toString().replaceAll("-", "") + "@example.com");
        user.setEmailVerified(true);
        user.setProvider(Authority.AuthProvider.local);
        user.setPassword(passwordEncoder.encode("test"));
        Authority authority = authorityRepository.getAuthorityByName(Authority.Roles.USER);
        if (authority != null) {
            user.getAuthorities().add(authority);
        }

        return userRepository.saveAndFlush(user);
    }

    public static void setAuthenticationForCurrentThreadLocal(AuthenticationManager authenticationManager, String login, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static <D extends PersistentObject, E extends PersistentObject> D getDtoByIdByUser(User user, String password, E entity, String url, MockMvc mockMvc, Class<D> clazz) throws Exception {
        D createdTaskDto;
        AtomicReference<String> responceBodyRef = new AtomicReference<>();
        String token = AuthControllerTest.authUser(mockMvc, LoginRequestDto.builder().email(user.getEmail()).password(password).build());
        mockMvc.perform(MockMvcRequestBuilders.get(url + entity.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(mvcResult -> responceBodyRef.set(mvcResult.getResponse().getContentAsString()));

        createdTaskDto = new ObjectMapper().readValue(responceBodyRef.get(), clazz);
        return createdTaskDto;
    }

    public static String getDtoPageByQueryParamsByUser(User user, String password, String urlWithQueryParams, MockMvc mockMvc) throws Exception {

        AtomicReference<String> responceBodyRef = new AtomicReference<>();
        String token = AuthControllerTest.authUser(mockMvc, LoginRequestDto.builder().email(user.getEmail()).password(password).build());
        mockMvc.perform(MockMvcRequestBuilders.get(Optional.ofNullable(urlWithQueryParams).orElse(""))
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(mvcResult -> responceBodyRef.set(mvcResult.getResponse().getContentAsString()));

        return responceBodyRef.get();

    }

    public static <D extends PersistentObject> D putDtoByUser(User user, String password, D entityDto, String url, MockMvc mockMvc, Class<D> clazz) throws Exception {
        AtomicReference<String> responceBodyRef = new AtomicReference<>();

        String token = AuthControllerTest.authUser(mockMvc, LoginRequestDto.builder().email(user.getEmail()).password(password).build());
        mockMvc.perform(MockMvcRequestBuilders.put(url + entityDto.getId())
                .header("Authorization", "Bearer " + token)
                //.accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(new ObjectMapper().writeValueAsString(entityDto))
                .characterEncoding("utf-8")
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(mvcResult -> responceBodyRef.set(mvcResult.getResponse().getContentAsString()));

        return new ObjectMapper().readValue(responceBodyRef.get(), clazz);
    }

    public static <D extends PersistentObject> D postDtoByUser(User user, String password, D entityDto, String url, MockMvc mockMvc, Class<D> clazz) throws Exception {
        AtomicReference<String> responceBodyRef = new AtomicReference<>();

        String token = AuthControllerTest.authUser(mockMvc, LoginRequestDto.builder().email(user.getEmail()).password(password).build());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .header("Authorization", "Bearer " + token)
                //.accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(new ObjectMapper().writeValueAsString(entityDto))
                .characterEncoding("utf-8")
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(mvcResult -> responceBodyRef.set(mvcResult.getResponse().getContentAsString()));

        return new ObjectMapper().readValue(responceBodyRef.get(), clazz);
    }


    @JsonIgnoreProperties(value = {"pageable"}, allowGetters = true, ignoreUnknown = true)
    public static class HelperPage<T> extends PageImpl<T> {

        @JsonCreator
        // Note: I don't need a sort, so I'm not including one here.
        // It shouldn't be too hard to add it in tho.
        public HelperPage(@JsonProperty("content") List<T> content,
                          @JsonProperty("number") int number,
                          @JsonProperty("size") int size,
                          @JsonProperty("totalElements") Long totalElements
        ) {
            super(content, PageRequest.of(number, size), totalElements);
            //System.out.println(content);
        }
    }

    @Getter
    @Setter
    @Builder
    @RequiredArgsConstructor
    public static class TestArgs<A, B, C, D, E, F, G> {
        private final A argA;
        private final B argB;
        private final C argC;
        private final D argD;
        private final E argE;
        private final F argF;
        private final G argG;
    }
}
