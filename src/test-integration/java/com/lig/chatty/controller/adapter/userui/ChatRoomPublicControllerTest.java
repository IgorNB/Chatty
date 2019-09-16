package com.lig.chatty.controller.adapter.userui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lig.chatty.Main;

import com.lig.chatty.controller.adapter.userui.dto.ChatRoomMessagePublicDto;

import com.lig.chatty.controller.adapter.userui.dto.UserPublicDto;
import com.lig.chatty.core.TestUtil;
import com.lig.chatty.core.TestUtil.TestArgs;
import com.lig.chatty.domain.*;
import com.lig.chatty.domain.core.PersistentObject;
import com.lig.chatty.repository.*;
import com.lig.chatty.security.config.AppPropertiesConfig;
import org.apache.commons.lang.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(properties = {"spring.batch.job.enabled=false"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Main.class})
public class ChatRoomPublicControllerTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomMessageRepository chatRoomMessageRepository;

    @Autowired
    private ChatRoomUserRepository chatRoomUserRepository;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private javax.servlet.Filter springSecurityFilterChain;


    @BeforeEach
    public void setup() {
        TestUtil.setAuthenticationForCurrentThreadLocal(authenticationManager, "admin@localhost", "admin");
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }



    @Transactional
    @TestFactory
    Stream<DynamicTest> dynamicFindAllMessagesTest() {
        List<TestArgs<User, String, String, List<ChatRoomMessage>, String, String, String>> inputList;
        {
            User admin = userRepository.findByEmail("admin@localhost").orElse(null);
            User user1 = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
            User user2 = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);

            ChatRoom newChatRoom = TestUtil.createAndSaveChatRoom(chatRoomRepository);
            ChatRoomUser newChatRoomAdmin = TestUtil.createAndSaveChatRoomUser(admin, newChatRoom, chatRoomUserRepository);
            ChatRoomUser newChatRoomUser1 = TestUtil.createAndSaveChatRoomUser(user1, newChatRoom, chatRoomUserRepository);
            ChatRoomUser newChatRoomUser2 = TestUtil.createAndSaveChatRoomUser(user2, newChatRoom, chatRoomUserRepository);
            Deque<ChatRoomUser> newChatRoomUsers = new LinkedList<>(Arrays.asList(newChatRoomAdmin, newChatRoomUser1, newChatRoomUser2));

            List<ChatRoomMessage> newChatRoomMessages = new ArrayList<>();


            final String defaultChatRoomId = AppPropertiesConfig.DEFAULT_ROOM_ID;
            ChatRoom defaultChatRoom = chatRoomRepository.getOne(defaultChatRoomId);
            ChatRoomUser defaultChatRoomUser1 = TestUtil.createAndSaveChatRoomUser(user1, defaultChatRoom, chatRoomUserRepository);
            ChatRoomUser defaultChatRoomUser2 = TestUtil.createAndSaveChatRoomUser(user2, defaultChatRoom, chatRoomUserRepository);
            List<ChatRoomMessage> defaultChatRoomMessages = new ArrayList<>();


            final Integer pageSize = 20;

            IntStream
                    .range(1, pageSize*2 - 1)
                    .boxed()
                    .forEach(i -> {
                        ChatRoomUser currentUser = newChatRoomUsers.peekFirst();
                        newChatRoomMessages.add(TestUtil.createAndSaveChatRoomMessage(currentUser.getUser(), newChatRoom, chatRoomMessageRepository));
                        defaultChatRoomMessages.add(TestUtil.createAndSaveChatRoomMessage(currentUser.getUser(), defaultChatRoom, chatRoomMessageRepository));
                        newChatRoomUsers.addLast(currentUser);
                    });


            inputList = Arrays.asList(
                    //2*page-1 different messages inserted by user1, user2, admin one by one (in newChat and in default chat) .

                    // For all users api must show newChat "lastPage" (no "pageNumber" declared in query params), e.g. from page + 1 to 2*pageSize -1 elements.
                    // Messages from same user but another chat (defaultChatRoomMessages) are not seen.
                    new TestArgs<>(user1, "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/",
                            newChatRoomMessages.subList(pageSize, newChatRoomMessages.size()), null, null, null),
                    new TestArgs<>(user2, "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/",
                            newChatRoomMessages.subList(pageSize, newChatRoomMessages.size()), null, null, null),
                    new TestArgs<>(admin, "admin", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/",
                            newChatRoomMessages.subList(pageSize, newChatRoomMessages.size()), null, null, null),

                    // when no "pageNumber" declared results must be queal to explicitly set pageNumber = lastPageNumber.
                    // Messages from same user but another chat (defaultChatRoomMessages) are not seen.
                    new TestArgs<>(user1, "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/?pageNumber=1",
                            newChatRoomMessages.subList(pageSize, newChatRoomMessages.size()), null, null, null),
                    new TestArgs<>(user2, "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/?pageNumber=1",
                            newChatRoomMessages.subList(pageSize, newChatRoomMessages.size()), null, null, null),
                    new TestArgs<>(admin, "admin", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/?pageNumber=1",
                            newChatRoomMessages.subList(pageSize, newChatRoomMessages.size()), null, null, null),

                    // For all users api must show full first page (due to "pageNumber" = 1), e.g. from 1 to pageSize elements.
                    // Messages from same user but another chat (defaultChatRoomMessages) are not seen.
                    new TestArgs<>(user1, "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/?pageNumber=0",
                            newChatRoomMessages.subList(0, pageSize), null, null, null),
                    new TestArgs<>(user2, "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/?pageNumber=0",
                            newChatRoomMessages.subList(0, pageSize), null, null, null),
                    new TestArgs<>(admin, "admin", "/api/v1/chatRooms/"+newChatRoom.getId()+"/messages/?pageNumber=0",
                            newChatRoomMessages.subList(0, pageSize), null, null, null)
            );
        }

        return inputList.stream()
                .map(test -> DynamicTest.dynamicTest(
                        " When call url: " + test.getArgC() +
                                " by user: " + test.getArgA().getEmail() +
                                " loggined with password: " + test.getArgB() +
                                " then see only comments: " + test.getArgD().stream().map(PersistentObject::getId).collect(Collectors.joining(",")),
                        () -> {
                            String responsePageJson = TestUtil.getDtoPageByQueryParamsByUser(test.getArgA(), test.getArgB(), test.getArgC(), mockMvc);
                            TestUtil.HelperPage<ChatRoomMessage> responsePage = new ObjectMapper().readerFor(new TypeReference<TestUtil.HelperPage<ChatRoomMessage>>() {
                            }).readValue(responsePageJson);
                            assertAll(
                                    () -> assertThat(responsePage.stream().map(PersistentObject::getId).collect(Collectors.toList())).
                                            containsExactlyInAnyOrderElementsOf(test.getArgD().stream().map(PersistentObject::getId).collect(Collectors.toList()))
                            );
                        }
                        )
                );
    }


    @Test
    @Transactional
    void createMessageInNewChatRoomTest() throws Exception {
        User user = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
        ChatRoom chatRoom = TestUtil.createAndSaveChatRoom(chatRoomRepository);
        ChatRoomUser chatRoomUser = TestUtil.createAndSaveChatRoomUser(user, chatRoom, chatRoomUserRepository);

        ChatRoomMessagePublicDto createDto = new ChatRoomMessagePublicDto();
        createDto.setId(UUID.randomUUID().toString().replaceAll("-", "")); //id MUST be generated as UUID by client!
        createDto.setMessage("test message " + UUID.randomUUID().toString().replaceAll("-", ""));

        ChatRoomMessagePublicDto responseDto = TestUtil.postDtoByUser(user, "test", createDto, "/api/v1/chatRooms/"+chatRoom.getId()+"/messages/", mockMvc, ChatRoomMessagePublicDto.class);

        //copy request and enrich with fields that backend should set itself
        ChatRoomMessagePublicDto expectedResponseDTO = (ChatRoomMessagePublicDto) SerializationUtils.clone(createDto);

        expectedResponseDTO.setVersion(0);

        UserPublicDto userPublicDto = new UserPublicDto();
        userPublicDto.setId(user.getId());

        expectedResponseDTO.setAuthor(userPublicDto);

        //copy responce fields we cannot predict
        expectedResponseDTO.setId(responseDto.getId());
        //expectedResponseDTO.setCreatedDate(responseDto.getCreatedDate());
        //expectedResponseDTO.setUpdatedDate(responseDto.getUpdatedDate());

        assertAll(
                () -> assertThat(responseDto).isEqualToComparingFieldByField(expectedResponseDTO)
        );

    }


    @Test
    @Transactional
    void createMessageInDefaultChatRoomTest() throws Exception {
        final String defaultChatRoomId = AppPropertiesConfig.DEFAULT_ROOM_ID;

        User user = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);

        TestUtil.createAndSaveChatRoomUser(user, chatRoomRepository.getOne(defaultChatRoomId), chatRoomUserRepository);

        ChatRoomMessagePublicDto createDto = new ChatRoomMessagePublicDto();
        createDto.setId(UUID.randomUUID().toString().replaceAll("-", "")); //id MUST be generated as UUID by client!
        createDto.setMessage("test message " + UUID.randomUUID().toString().replaceAll("-", ""));

        ChatRoomMessagePublicDto responseDto = TestUtil.postDtoByUser(user, "test", createDto, "/api/v1/chatRooms/"+ defaultChatRoomId +"/messages/", mockMvc, ChatRoomMessagePublicDto.class);

        //copy request and enrich with fields that backend should set itself
        ChatRoomMessagePublicDto expectedResponseDTO = (ChatRoomMessagePublicDto) SerializationUtils.clone(createDto);

        expectedResponseDTO.setVersion(0);

        UserPublicDto userPublicDto = new UserPublicDto();
        userPublicDto.setId(user.getId());

        expectedResponseDTO.setAuthor(userPublicDto);

        //copy responce fields we cannot predict
        expectedResponseDTO.setId(responseDto.getId());
        //expectedResponseDTO.setCreatedDate(responseDto.getCreatedDate());
        //expectedResponseDTO.setUpdatedDate(responseDto.getUpdatedDate());

        assertAll(
                () -> assertThat(responseDto).isEqualToComparingFieldByField(expectedResponseDTO)
        );

    }

    @Transactional
    @TestFactory
    Stream<DynamicTest> dynamicFindAllChatRoomUsersTest() {
        List<TestArgs<User, String, String, List<ChatRoomUser>, String, String, String>> inputList;
        {
            User admin = userRepository.findByEmail("admin@localhost").orElse(null);
            User user1 = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
            User user2 = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
            ChatRoom newChatRoom = TestUtil.createAndSaveChatRoom(chatRoomRepository);

            final String defaultChatRoomId = AppPropertiesConfig.DEFAULT_ROOM_ID;
            ChatRoom defaultChatRoom = chatRoomRepository.getOne(defaultChatRoomId);
            List<ChatRoomUser> defaultChatRoomUsers = new ArrayList<>();
            List<ChatRoomUser> newChatRoomUsers = new ArrayList<>();


            final Integer pageSize = 20;

            IntStream
                    .range(1, pageSize*2 - 1)
                    .boxed()
                    .forEach(i -> {
                        User user = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
                        newChatRoomUsers.add(TestUtil.createAndSaveChatRoomUser(user, newChatRoom, chatRoomUserRepository));
                        defaultChatRoomUsers.add(TestUtil.createAndSaveChatRoomUser(user, defaultChatRoom, chatRoomUserRepository));
                    });

            inputList = Arrays.asList(
                    //2*page-1 different messages inserted by user1, user2, admin one by one (in newChat and in default chat) .

                    // For all users api must show newChat "first Page" (no "pageNumber" declared in query params), e.g. from 0 to pageSize -1 elements.
                    // Messages from same user but another chat (defaultChatRoomMessages) are not seen.
                    new TestArgs<>(newChatRoomUsers.get(0).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/",
                            newChatRoomUsers.subList(0, pageSize), null, null, null),
                    new TestArgs<>(newChatRoomUsers.get(1).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/",
                            newChatRoomUsers.subList(0, pageSize), null, null, null),
                    new TestArgs<>(newChatRoomUsers.get(2).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/",
                            newChatRoomUsers.subList(0, pageSize), null, null, null),

                    // when no "pageNumber" declared results must be queal to explicitly set pageNumber = 0.
                    // Messages from same user but another chat (defaultChatRoomMessages) are not seen.
                    new TestArgs<>(newChatRoomUsers.get(0).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/?pageNumber=0",
                            newChatRoomUsers.subList(0, pageSize), null, null, null),
                    new TestArgs<>(newChatRoomUsers.get(1).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/?pageNumber=0",
                            newChatRoomUsers.subList(0, pageSize), null, null, null),
                    new TestArgs<>(newChatRoomUsers.get(2).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/?pageNumber=0",
                            newChatRoomUsers.subList(0, pageSize), null, null, null),

                    // For all users api must show full first page (due to "pageNumber" = 1), e.g. from pageSize to 2*pageSize -1 elements.
                    // Messages from same user but another chat (defaultChatRoomMessages) are not seen.
                    new TestArgs<>(newChatRoomUsers.get(0).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/?pageNumber=1",
                            newChatRoomUsers.subList(pageSize, newChatRoomUsers.size()), null, null, null),
                    new TestArgs<>(newChatRoomUsers.get(1).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/?pageNumber=1",
                            newChatRoomUsers.subList(pageSize, newChatRoomUsers.size()), null, null, null),
                    new TestArgs<>(newChatRoomUsers.get(2).getUser(), "test", "/api/v1/chatRooms/"+newChatRoom.getId()+"/members/?pageNumber=1",
                            newChatRoomUsers.subList(pageSize, newChatRoomUsers.size()), null, null, null)
            );
        }

        return inputList.stream()
                .map(test -> DynamicTest.dynamicTest(
                        " When call url: " + test.getArgC() +
                                " by user: " + test.getArgA().getEmail() +
                                " loggined with password: " + test.getArgB() +
                                " then see only comments: " + test.getArgD().stream().map(PersistentObject::getId).collect(Collectors.joining(",")),
                        () -> {
                            String responsePageJson = TestUtil.getDtoPageByQueryParamsByUser(test.getArgA(), test.getArgB(), test.getArgC(), mockMvc);
                            TestUtil.HelperPage<ChatRoomUser> responsePage = new ObjectMapper().readerFor(new TypeReference<TestUtil.HelperPage<ChatRoomUser>>() {
                            }).readValue(responsePageJson);
                            assertAll(
                                    () -> assertThat(responsePage.stream().map(PersistentObject::getId).collect(Collectors.toList())).
                                            containsExactlyInAnyOrderElementsOf(test.getArgD().stream().map(PersistentObject::getId).collect(Collectors.toList()))
                            );
                        }
                        )
                );
    }
}
