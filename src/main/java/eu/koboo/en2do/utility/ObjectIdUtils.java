package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;
import org.bson.types.ObjectId;

@UtilityClass
public class ObjectIdUtils {

    public ObjectId of(Object object) {
        if(object instanceof ObjectId objectId) {
            return objectId;
        }
        if(object instanceof String string) {
            return new ObjectId(string);
        }
        return new ObjectId(String.valueOf(object));
    }
}
