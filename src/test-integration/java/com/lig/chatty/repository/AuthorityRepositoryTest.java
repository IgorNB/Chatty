package com.lig.chatty.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lig.chatty.domain.Authority;

import com.lig.chatty.domain.User;
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
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@TestPropertySource(properties = {"spring.batch.job.enabled=false"})
@ExtendWith(SpringExtension.class)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = {DataJpaAuditConfig.class}))
@ActiveProfiles({"springDataJpa", "AuthorityRepositoryTest"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthorityRepositoryTest {


    public static final String OPERATION_NOT_SUPPORTED = "Operation not supported";
    public final AuthorityRepository repository;
    private final TestEntityManager em;
    private final EntityManager entityManager;
    private final EntityFactory<Authority> entityFactoryAuthority;

    @Autowired
    public AuthorityRepositoryTest(@NonNull AuthorityRepository repository, @NonNull TestEntityManager em, @NonNull EntityFactory<Authority> entityFactoryAuthority, @NonNull EntityManager entityManager) {
        this.repository = repository;
        this.em = em;
        this.entityFactoryAuthority = entityFactoryAuthority;
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
        final Authority entity = entityFactoryAuthority.getNewEntityInstance();
        String id = entity.getId();
        Integer version = entity.getVersion();

        final Authority deepEntityCopy = (Authority) SerializationUtils.clone(entity);

        final Authority entitySaved = repository.saveAndFlush(entity);
        final Authority entityQueried = repository.findById(id).orElse(null);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        deepEntityCopy.setVersion(0);

        deepEntityCopy.setCreatedBy(entitySaved.getCreatedBy());
        deepEntityCopy.setCreatedDate(entitySaved.getCreatedDate());

        deepEntityCopy.setUpdatedDate(entitySaved.getUpdatedDate());
        deepEntityCopy.setLastUpdBy(entitySaved.getLastUpdBy());

        assertAll(
                () -> assertThat(deepEntityCopy).isEqualToComparingFieldByFieldRecursively(entitySaved),
                () -> assertThat(deepEntityCopy).isEqualToComparingFieldByFieldRecursively(entityQueried),
                () -> assertThat(gson.toJson(deepEntityCopy).toString()).isEqualTo(gson.toJson(entitySaved).toString()),
                () -> assertThat(gson.toJson(deepEntityCopy).toString()).isEqualTo(gson.toJson(entityQueried).toString())
        );
    }

    @Test
    @Transactional
    public void updateAndQueryTest() {
        final Authority entitySaved = repository.saveAndFlush(entityFactoryAuthority.getNewEntityInstance());
        final Authority deepEntitySavedCopy = (Authority) SerializationUtils.clone(entitySaved);
        entitySaved.setLastUpdBy(entityFactoryAuthority.getNewEntityInstance().getLastUpdBy());

        final Authority entityUpdated = repository.saveAndFlush(entitySaved);


        deepEntitySavedCopy.setVersion(deepEntitySavedCopy.getVersion() + 1);

        deepEntitySavedCopy.setCreatedBy(deepEntitySavedCopy.getCreatedBy());
        deepEntitySavedCopy.setCreatedDate(deepEntitySavedCopy.getCreatedDate());

        deepEntitySavedCopy.setUpdatedDate(entityUpdated.getUpdatedDate());
        deepEntitySavedCopy.setLastUpdBy(entityUpdated.getLastUpdBy());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        assertAll(
                () -> assertThat(deepEntitySavedCopy).isEqualToComparingFieldByField(entityUpdated),
                () -> assertThat(gson.toJson(deepEntitySavedCopy).toString()).isEqualTo(gson.toJson(entityUpdated).toString())
        );
    }

    @Test
    void getAuthorityByName() {

        final Authority entitySaved1 = repository.saveAndFlush(entityFactoryAuthority.getNewEntityInstance());

        Authority authority2 = entityFactoryAuthority.getNewEntityInstance();

        final Authority entitySaved2 = repository.saveAndFlush(authority2);

        Authority authorityQueried = repository.getAuthorityByName(entitySaved2.getName());

        assertAll(
                () -> assertThat(authorityQueried).isEqualTo(entitySaved2)
        );

    }

    @Profile("AuthorityRepositoryTest")
    @TestConfiguration
    public static class IntegrationTestConfiguration {

        @Bean
        @Primary
        public AuditorAware<User> auditorProvider(@Autowired EntityManager entityManager) {
            return () -> {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                return Optional.ofNullable(securityContext.getAuthentication())
                        .map(authentication -> {
                            /*if (authentication.getPrincipal() instanceof UserDetails) {
                                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                                return entityManager.getReference(User.class, userDetails.getUsername());
                            }*/
                            return null; //in dataJPA tests we do not log createdBy and updatedBy
                        });
            };
        }

        @Bean
        public EntityFactory<Authority> getNewEntityInstance(TestEntityManager em) {
            return new EntityFactory<Authority>() {
                @Override
                @Transactional(propagation = Propagation.REQUIRES_NEW)
                public Authority getNewEntityInstance() {
                    String userName = "test-user-name" + UUID.randomUUID().toString().replaceAll("-", "");
                    User userNew = new User();
                    userNew.setName(userName);
                    userNew.setEmail(userName + "@chatty.com");
                    userNew.setProvider(Authority.AuthProvider.local);
                    userNew.setPassword("test-encrypted-password" + userName);
                    final User user = em.persistFlushFind(userNew);

                    String authorityName2 = "test-authority-name" + UUID.randomUUID().toString().replaceAll("-", "");
                    Authority authorityNew2 = new Authority();
                    authorityNew2.setLastUpdBy(user);
                    authorityNew2.setName(authorityName2);
                    return authorityNew2;
                }
            };
        }
    }
}