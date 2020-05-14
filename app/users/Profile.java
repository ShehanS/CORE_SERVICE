package users;


import ExternalAPIs.ExternalAPIDAO;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import database.Mongo;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class Profile extends ExternalAPIDAO {


    protected static MongoDatabase db;


 @Inject
 private Profile(Mongo mongo) {
        super(mongo);
 }


    public void addOneDocument() {

        MongoCollection<Document> collection = db.getCollection("sampleCollection");
        System.out.println("Collection sampleCollection selected successfully");

        Document document = new Document("title", "MongoDB")
                .append("id", 1)
                .append("description", "database")
                .append("likes", 100)
                .append("url", "http://www.tutorialspoint.com/mongodb/")
                .append("by", "tutorials point");
        collection.insertOne(document);
    }


}
