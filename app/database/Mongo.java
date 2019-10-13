package database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.slf4j.LoggerFactory;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Mongo {
    public MongoClient mongoClient = null;
    public MongoDatabase db = null;
    private static Mongo mongo = null;


    @Inject
    private Mongo() {
        Logger.info("DB : [Mongo] - Starting...");
       mongoClient = new MongoClient("localhost",27017);
        db = mongoClient.getDatabase("myDb");
        Logger.info("DB : [Mongo] Name: " + db.getName());
       }


    public static Mongo getInstance() {
        if (mongo == null) {
            synchronized (Mongo.class) {
                if (mongo == null) {
                    mongo = new Mongo();
                }
            }
        }
        return mongo;
    }


}
