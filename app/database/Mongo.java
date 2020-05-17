package database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Mongo {
    private static final Logger log = LogManager.getLogger(Mongo.class);
    public MongoClient mongoClient = null;
    public MongoDatabase db = null;
    private static Mongo mongo = null;
    private static Config configuration;

    /*
    Database connection manager. all the configuration are getting from
    application.conf

     */
    @Inject
    private Mongo() {
        try {
            Builder builder = MongoClientOptions.builder().connectTimeout(3000);
            log.info("Trying to connect with : [Mongo] DB... {{connection-localhost:27017}}");
            mongoClient = new MongoClient(new ServerAddress(configuration.getString("app.mongo.server"), configuration.getInt("app.mongo.port")), builder.build());

            db = mongoClient.getDatabase(configuration.getString("app.mongo.db"));
            log.info("DB : [Mongo] DB... {{databases: " + db.getName() + "}})");
            log.info("DB : [Mongo] DB... {{connection successfully}}");

        } catch (MongoException e) {
            log.error(e.getMessage());
        } finally {

        }
    }
    //Load configuration from Module Class
    public static void setConfiguration(Config configuration) {
        Mongo.configuration = configuration;
    }

}
