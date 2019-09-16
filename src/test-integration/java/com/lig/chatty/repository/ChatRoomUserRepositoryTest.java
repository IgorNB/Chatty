package com.lig.chatty.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lig.chatty.domain.Authority;
import com.lig.chatty.domain.ChatRoom;
import com.lig.chatty.domain.ChatRoomUser;
import com.lig.chatty.domain.User;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
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
@ActiveProfiles({"springDataJpa", "ChatRoomUserRepositoryTest"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRoomUserRepositoryTest {


    public static final String OPERATION_NOT_SUPPORTED = "Operation not supported";
    public final ChatRoomUserRepository repository;
    private final TestEntityManager em;
    private final EntityManager entityManager;
    private final EntityFactory<ChatRoomUser> entityFactoryChatRoomUser;

    @Autowired
    public ChatRoomUserRepositoryTest(@NonNull ChatRoomUserRepository repository, @NonNull TestEntityManager em, @NonNull EntityFactory<ChatRoomUser> entityFactoryChatRoomUser, @NonNull EntityManager entityManager) {
        this.repository = repository;
        this.em = em;
        this.entityFactoryChatRoomUser = entityFactoryChatRoomUser;
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
        final ChatRoomUser entity = entityFactoryChatRoomUser.getNewEntityInstance();
        String id = entity.getId();
        Integer version = entity.getVersion();

        final ChatRoomUser deepEntityCopy = (ChatRoomUser) SerializationUtils.clone(entity);

        final ChatRoomUser entitySaved = repository.saveAndFlush(entity);
        final ChatRoomUser entityQueried = repository.findById(id).orElse(null);

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
    public void findAllByChatRoomTest() {
        final ChatRoomUser entitySaved1 = repository.saveAndFlush(entityFactoryChatRoomUser.getNewEntityInstance());

        ChatRoomUser chatRoomUser2 = entityFactoryChatRoomUser.getNewEntityInstance();

        final ChatRoomUser entitySaved2 = repository.saveAndFlush(chatRoomUser2);

        ChatRoomUser chatRoomUser3 = entityFactoryChatRoomUser.getNewEntityInstance();

        chatRoomUser3.setChatRoom(entitySaved2.getChatRoom());
        final ChatRoomUser entitySaved3 = repository.saveAndFlush(chatRoomUser3);

        Sort sort = Sort.by(Sort.Direction.ASC, GenericAbstractPersistentAuditingObject.Fields.createdDate)
                .and(Sort.by(Sort.Direction.DESC, AbstractPersistentObject.Fields.id));

        Iterable<ChatRoomUser> page1 = repository.findAllByChatRoom(entitySaved2.getChatRoom(), PageRequest.of(0, 1, sort));
        Iterable<ChatRoomUser> page2 = repository.findAllByChatRoom(entitySaved3.getChatRoom(), PageRequest.of(1, 1, sort));

        assertAll(
                () -> assertThat(page1).containsOnly(entitySaved2),
                () -> assertThat(page2).containsOnly(entitySaved3)
        );
    }



    @Profile("ChatRoomUserRepositoryTest")
    @TestConfiguration
    public static class IntegrationTestConfiguration {


        @Bean
        public EntityFactory<ChatRoomUser> getNewEntityInstance(TestEntityManager em) {
            return new EntityFactory<ChatRoomUser>() {
                @Override
                @Transactional(propagation = Propagation.REQUIRES_NEW)
                public ChatRoomUser getNewEntityInstance() {
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

                    String chatRoomUserName2 = "test-chatRoomUser-name" + UUID.randomUUID().toString().replaceAll("-", "");
                    ChatRoomUser chatRoomUserNew2 = new ChatRoomUser();
                    //chatRoomUserNew2.setLastUpdBy(userSaved);
                    chatRoomUserNew2.setChatRoom(chatRoomSaved);
                    chatRoomUserNew2.setUser(userSaved);
                    return chatRoomUserNew2;
                }
            };
        }
    }
}