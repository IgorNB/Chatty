package com.lig.chatty.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppPropertiesConfig {
    //this cannot be moved to application.yml due to changing it leads to ChatRoomMembers and so on migration
    public static final String DEFAULT_ROOM_ID = "0-1";

    private final Relay relay = new Relay();
    private final Auth auth = new Auth();
    private final RoomMessage roomMessage = new RoomMessage();
    private final RoomMember roomMember = new RoomMember();

    @Setter
    @Getter
    public static class RoomMessage {
        private Integer pageSize;
    }

    @Setter
    @Getter
    public static class RoomMember {
        private Integer pageSize;
    }

    @Setter
    @Getter
    public static class Relay {
        private String host;
        private Integer port;
    }

    @Setter
    @Getter
    public static class Auth {
        private String tokenSecret;
        private long tokenExpirationMsec;
        private long tokenExpirationMsecForRememberMe;

    }
}
