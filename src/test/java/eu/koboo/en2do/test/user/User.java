package eu.koboo.en2do.test.user;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;
import java.util.UUID;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public class User {

    @Id // en2do
    UUID uniqueId;

    String userName;
    String email;

    Date registrationDate;
    Date lastLoginDate;
    Date inactiveDate;

    public String someThing() {
        return "Awesome";
    }

    public String getCombined() {
        return userName + email;
    }
}
