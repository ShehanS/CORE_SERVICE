

import com.google.inject.AbstractModule;
import database.Mongo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import play.libs.akka.AkkaGuiceSupport;
import com.typesafe.config.Config;
import play.Environment;


public class Module extends AbstractModule implements AkkaGuiceSupport {

    private static final Logger log = LogManager.getLogger(Mongo.class);
    private final Config configuration;

    public Module(Environment environment, Config configuration) {
        log.info("======STARTING CORE SERVICE========");
        log.info("Version : " + configuration.getString("app.version \n\n"));
        this.configuration = configuration;
        Mongo.setConfiguration(this.configuration);
    }


    @Override
    protected void configure() {

    }
}
