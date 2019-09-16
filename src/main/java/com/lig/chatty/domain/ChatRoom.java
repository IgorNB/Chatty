package com.lig.chatty.domain;

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
@Table(name = ChatRoom.TABLE)
@Getter
@Setter
@ToString(callSuper = true)
@FieldNameConstants
public class ChatRoom extends GenericAbstractPersistentAuditingObject<User> {
    public static final String TABLE = "CHAT_ROOM";
    @Formula("NULL")
    private String q;

    @Column(name = Columns.NAME, nullable = true)
    private String name;

    public static final class Columns {
        public static final String NAME = "NAME";
        private Columns() {
            throw new IllegalStateException("Utility class");
        }
    }
}
