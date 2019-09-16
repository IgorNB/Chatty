package com.lig.chatty.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lig.chatty.domain.*;
import com.lig.chatty.domain.core.AbstractPersistentObject;
import com.lig.chatty.domain.core.GenericAbstractPersistentAuditingObject;
import com.lig.chatty.repository.common.DataJpaAuditConfig;
import com.lig.chatty.repository.common.EntityFactory;

import lombok.NonNull;
import org.apache.commons.lang.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@TestPropertySource(properties = {"spring.batch.job.enabled=false"})
@ExtendWith(SpringExtension.class)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = {DataJpaAuditConfig.class}))
@ActiveProfiles({"springDataJpa", "ChatRoomMessageRepositoryTest"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRoomMessageRepositoryTest {


    public static final String OPERATION_NOT_SUPPORTED = "Operation not supported";
    public final ChatRoomMessageRepository repository;
    private final TestEntityManager em;
    private final EntityManager entityManager;
    private final EntityFactory<ChatRoomMessage> entityFactoryChatRoomMessage;

    @Autowired
    public ChatRoomMessageRepositoryTest(@NonNull ChatRoomMessageRepository repository, @NonNull TestEntityManager em, @NonNull EntityFactory<ChatRoomMessage> entityFactoryChatRoomMessage, @NonNull EntityManager entityManager) {
        this.repository = repository;
        this.em = em;
        this.entityFactoryChatRoomMessage = entityFactoryChatRoomMessage;
        this.entityManager = entityManager;
    }

    @Test
    public void testRepositoryInterfaceImplementationAutowiring() {
        assertThat(repository instanceof JpaRepository
                || AopUtils.getTargetClass(repository).equals(JpaRepository.class)
        ).isTrue();
    }

    @Test
    @Transactional
    public void saveAndQueryTest() {
        final ChatRoomMessage entity = entityFactoryChatRoomMessage.getNewEntityInstance();
        String id = entity.getId();
        Integer version = entity.getVersion();

        final ChatRoomMessage deepEntityCopy = (ChatRoomMessage) SerializationUtils.clone(entity);

        final ChatRoomMessage entitySaved = repository.saveAndFlush(entity);
        final ChatRoomMessage entityQueried = repository.findById(id).orElse(null);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        deepEntityCopy.setVersion(0);

        deepEntityCopy.setCreatedBy(entitySaved.getCreatedBy());
        deepEntityCopy.setCreatedDate(entitySaved.getCreatedDate());

        deepEntityCopy.setUpdatedDate(entitySaved.getUpdatedDate());
        deepEntityCopy.setLastUpdBy(entitySaved.getLastUpdBy());
        assertAll(
                () -> assertThat(deepEntityCopy).isEqualToComparingFieldByFieldRecursively(entitySaved),
                () -> assertThat(deepEntityCopy).isEqualToComparingFieldByFieldRecursively(entityQueried)/*,
                () -> assertThat(gson.toJson(deepEntityCopy).toString()).isEqualTo(gson.toJson(entitySaved).toString()),
                () -> assertThat(gson.toJson(deepEntityCopy).toString()).isEqualTo(gson.toJson(entityQueried).toString())*/
        );
    }

    @Test
    @Transactional
    public void updateAndQueryTest() {
        final ChatRoomMessage entitySaved = repository.saveAndFlush(entityFactoryChatRoomMessage.getNewEntityInstance());
        final ChatRoomMessage deepEntitySavedCopy = (ChatRoomMessage) SerializationUtils.clone(entitySaved);
        entitySaved.setMessage("updated message " +  UUID.randomUUID().toString().replaceAll("-", ""));

        final ChatRoomMessage entityUpdated = repository.saveAndFlush(entitySaved);


        deepEntitySavedCopy.setVersion(deepEntitySavedCopy.getVersion() + 1);

        deepEntitySavedCopy.setCreatedBy(deepEntitySavedCopy.getCreatedBy());
        deepEntitySavedCopy.setCreatedDate(deepEntitySavedCopy.getCreatedDate());

        deepEntitySavedCopy.setUpdatedDate(entityUpdated.getUpdatedDate());
        deepEntitySavedCopy.setLastUpdBy(entityUpdated.getLastUpdBy());

        deepEntitySavedCopy.setMessage(entityUpdated.getMessage());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        assertAll(
                () -> assertThat(deepEntitySavedCopy).isEqualToComparingFieldByField(entityUpdated),
                () -> assertThat(gson.toJson(deepEntitySavedCopy).toString()).isEqualTo(gson.toJson(entityUpdated).toString())
        );
    }

    @Test
    @Transactional
    public void findAllByChatRoomTest() {
        final ChatRoomMessage entitySaved1 = repository.saveAndFlush(entityFactoryChatRoomMessage.getNewEntityInstance());

        ChatRoomMessage chatRoomMessage2 = entityFactoryChatRoomMessage.getNewEntityInstance();

        final ChatRoomMessage entitySaved2 = repository.saveAndFlush(chatRoomMessage2);

        ChatRoomMessage chatRoomMessage3 = entityFactoryChatRoomMessage.getNewEntityInstance();

        chatRoomMessage3.setChatRoom(entitySaved2.getChatRoom());
        final ChatRoomMessage entitySaved3 = repository.saveAndFlush(chatRoomMessage3);

        Sort sort = Sort.by(Sort.Direction.ASC, GenericAbstractPersistentAuditingObject.Fields.createdDate)
                .and(Sort.by(Sort.Direction.DESC, AbstractPersistentObject.Fields.id));

        Iterable<ChatRoomMessage> page1 = repository.findAllByChatRoom(entitySaved2.getChatRoom(), PageRequest.of(0, 1, sort));
        Iterable<ChatRoomMessage> page2 = repository.findAllByChatRoom(entitySaved3.getChatRoom(), PageRequest.of(1, 1, sort));

        assertAll(
                () -> assertThat(page1).containsOnly(entitySaved2),
                () -> assertThat(page2).containsOnly(entitySaved3)
        );
    }

    @Test
    @Transactional
    public void countByChatRoomTest() {
        final ChatRoomMessage entitySaved1 = repository.saveAndFlush(entityFactoryChatRoomMessage.getNewEntityInstance());

        ChatRoomMessage chatRoomMessage2 = entityFactoryChatRoomMessage.getNewEntityInstance();

        final ChatRoomMessage entitySaved2 = repository.saveAndFlush(chatRoomMessage2);

        ChatRoomMessage chatRoomMessage3 = entityFactoryChatRoomMessage.getNewEntityInstance();

        chatRoomMessage3.setChatRoom(entitySaved2.getChatRoom());
        final ChatRoomMessage entitySaved3 = repository.saveAndFlush(chatRoomMessage3);


        @NonNull Integer countRoom1 = repository.countChatRoomMessagesByChatRoom(entitySaved1.getChatRoom());
        @NonNull Integer countRoom2 = repository.countChatRoomMessagesByChatRoom(entitySaved2.getChatRoom());

        assertAll(
                () -> assertThat(countRoom1).isEqualTo(1),
                () -> assertThat(countRoom2).isEqualTo(2)
        );
    }

    @Profile("ChatRoomMessageRepositoryTest")
    @TestConfiguration
    public static class IntegrationTestConfiguration {


        @Bean
        public EntityFactory<ChatRoomMessage> getNewEntityInstance(TestEntityManager em) {
            return new EntityFactory<ChatRoomMessage>() {
                @Override
                @Transactional(propagation = Propagation.REQUIRES_NEW)
                public ChatRoomMessage getNewEntityInstance() {
                    String userName = "test-user-name" + UUID.randomUUID().toString().replaceAll("-", "");
                    User userNew = new User();
                    userNew.setName(userName);
                    userNew.setEmail(userName + "@chatty.com");
                    userNew.setProvider(Authority.AuthProvider.local);
                    final User userSaved = em.persistFlushFind(userNew);

                    String chatRoomName = "test-chatRoom-name" + UUID.randomUUID().toString().replaceAll("-", "");
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setName(chatRoomName);
                    final ChatRoom chatRoomSaved = em.persistFlushFind(chatRoom);

                    String chatRoomMessageName2 = "test-chatRoomMessage-name" + UUID.randomUUID().toString().replaceAll("-", "");
                    ChatRoomMessage chatRoomMessageNew2 = new ChatRoomMessage();
                    //chatRoomMessageNew2.setLastUpdBy(userSaved);
                    chatRoomMessageNew2.setChatRoom(chatRoomSaved);
                    chatRoomMessageNew2.setAuthor(userSaved);
                    chatRoomMessageNew2.setMessage("message " +  UUID.randomUUID().toString().replaceAll("-", ""));
                    return chatRoomMessageNew2;
                }
            };
        }
    }
}