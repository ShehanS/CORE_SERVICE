package analytic;

import ExternalAPIs.ExternalAPIDAO;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import database.Mongo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import play.libs.Json;
import service.Clock;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analytic extends ExternalAPIDAO {

    private static final Logger log = LogManager.getLogger(Mongo.class); //define log manager
    private static String TAG = "Analytic.class"; //Class name
    private static String TRANSACTION = "transaction";

    @Inject
    public Analytic(Mongo mongo) { // Pass mongo class
        super(mongo);

    }

    //Get inventory count
    public JsonNode getInventoryDetails() {
        Map<String, Object> details = new HashMap<>();
        Clock clock = new Clock();
        int systemIN = 0;
        int systemOUT = 0;
        BasicDBObject query = new BasicDBObject();
        query.put("couriers", new Document("$elemMatch", new Document("app_type", "BA-APP-INVENTORY")));
        //query.put("date_time", new BasicDBObject("$gt", clock.getMillPriviousDate()).append("$lte", clock.getMillCurrentEpoch()));

        System.out.println(query);
        ArrayList<Document> result = getQueryDoc(TRANSACTION, query);
        for (Document doc : result) {
            List<Document> txHistory = (List<Document>) doc.get("couriers");
            for (Document c : txHistory) {
                if (c.getString("tx_type").equals("in")) {
                    systemIN = +1;
                }
                if (c.getString("tx_type").equals("out")) {
                    systemIN = +1;
                }

            }

        }


        details.put("inventory_in", systemIN);
        details.put("inventory_out", systemOUT);
        details.put("total_que", result.size());
        details.put("result", result);

        //details.put()


        return Json.toJson(details);
    }


}
