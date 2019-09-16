package com.lig.chatty.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lig.chatty.domain.core.GenericAbstractPersistentAuditingObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import net.jcip.annotations.NotThreadSafe;
import org.hibernate.annotations.Formula;

import javax.persistence.*;

@NotThreadSafe
@Entity
@Table(name = ChatRoomMessage.TABLE)
@Getter
@Setter
@ToString(callSuper = true)
@FieldNameConstants
public class ChatRoomMessage extends GenericAbstractPersistentAuditingObject<User> {
    public static final String TABLE = "CHAT_ROOM_MESSAGE";
    @Formula("NULL")
    private String q;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne()
    @JoinColumn(name = Columns.CHAT_ROOM_ID, nullable = false, updatable = false)
    private ChatRoom chatRoom;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne()
    @JoinColumn(name = Columns.AUTHOR_ID, nullable = false, updatable = false)
    private User author;

    @Column(name = Columns.MESSAGE)
    private String message;

    public static final class Columns {
        public static final String CHAT_ROOM_ID = "CHAT_ROOM_ID";
        public static final String AUTHOR_ID = "AUTHOR_ID";
        public static final String MESSAGE = "MESSAGE";

        private Columns() {
            throw new IllegalStateException("Utility class");
        }
    }
}
