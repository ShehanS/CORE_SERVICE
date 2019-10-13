
import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import play.Environment;
import play.libs.akka.AkkaGuiceSupport;

public class Module extends AbstractModule implements AkkaGuiceSupport {


    private final Config configuration;

    public Module(Environment environment, Config configuration) {
        System.out.println("START");
        this.configuration = configuration;
       // Mongo.setConfiguration(this.configuration);



    }


    @Override
    protected void configure() {



    }
}
