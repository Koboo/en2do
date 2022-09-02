package eu.koboo.en2do;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) {
        MongoManager manager = new MongoManager();

        MongoCollection<Document> coll = manager.getDatabase().getCollection("pojo_test");

        Pattern pattern = Pattern.compile(".*SomeOtherName.*", Pattern.CASE_INSENSITIVE);
        FindIterable<Document> name = coll.find(Filters.regex("name", pattern));
        System.out.println(name.first());
    }
}