package ExternalAPIs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import database.Mongo;
import model.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.libs.Json;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class ExternalAPIDAO {

    protected static MongoDatabase db;
    private static final Logger log = LogManager.getLogger(Mongo.class);
    private static String TAG = "ExternalAPIDAO.class";
    private MongoCollection collection = null;

    public ExternalAPIDAO(Mongo mongo) {
        db = mongo.db;
    }

    //Insert document
    protected ObjectId insertDoc(Document doc, String colName) {
        ObjectMapper errorMessage = new ObjectMapper();
        ObjectNode message = errorMessage.createObjectNode();
        message.put("status", "error");
        try {
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            db = db.withCodecRegistry(pojoCodecRegistry);
            collection = db.getCollection(colName);
            collection = collection.withCodecRegistry(pojoCodecRegistry);
            collection.insertOne(doc);
            ObjectId id = doc.getObjectId("_id");
            log.info("{{Class-" + TAG + " - insertDoc - method}}-Inser document-" + id);
            return id;
        } catch (MongoWriteException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    //get document as array list
    protected ArrayList<Document> getAllDoc(String col) {
        ArrayList<Document> ArrayList = new ArrayList<>();
        collection = db.getCollection(col);
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                ArrayList.add(document);
            }
        } catch (MongoException e) {
            log.error(e.getMessage());
        } finally {
            cursor.close();
        }
        return ArrayList;
    }


    //get document as array list
    protected long getDocCount(String col, BasicDBObject query) {
        log.info("{{Class-" + TAG + " - getDocCount - method}}-Inser document-" + query);
        collection = db.getCollection(col);
        long docCount = collection.count(query);
        return docCount;
    }

    //get document using query as array list
    protected ArrayList<Document> getQueryDoc(String col, BasicDBObject query) {
        SearchResult result;
        log.info("{{Class-" + TAG + " - getQueryDoc - method}}-Inser document-" + query);
        ArrayList<Document> ArrayList = new ArrayList<>();
        collection = db.getCollection(col);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                ArrayList.add(document);
            }
        } catch (MongoException e) {
            log.error(e.getMessage());
        } finally {
            cursor.close();
        }
        return ArrayList;
    }

    //get singel doc as json
    protected Document getQueryForSingelDoc(String col, BasicDBObject query) {
        Document result = new Document();
        collection = db.getCollection(col);
        log.info("{{Class-" + TAG + " : method-getQueryForSingelDoc}}-Find document: query-" + query);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        try {
            while (cursor.hasNext()) {
                result = cursor.next();
            }
            log.info("RESULT: " + result);
        } catch (MongoCommandException e) {
            log.error(e.getErrorMessage());
        } finally {
            cursor.close();
        }
        return (result);
    }

    //get singel doc as json
    protected ArrayList<Document> getQueryForUser(String col, String username, String nic, String email) {
        try {
            MongoCollection<Document> users = db.getCollection(col);
            ArrayList<Document> result = new ArrayList<>();
            log.info("{{Class-" + TAG + " : method-getQueryForUser}}");
            users.find(Filters.or(
                    Filters.or(Filters.eq("username", username), Filters.eq("email", email), Filters.eq("nic_or_passport", nic))
            )).into(result);
            log.info("RESULT : " + result);
            return result;
        } catch (MongoException e) {
            log.error(e.getMessage());
        }
        return null;
    }


    protected JsonNode deleteDoc(String col, BasicDBObject query) {
        DeleteResult result;
        collection = db.getCollection(col);
        log.info("{{Class-" + TAG + " : method-deleteDoc}}-Delete document: query-" + query);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        try {
            result = collection.deleteOne(query);
            return Json.toJson(result);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            cursor.close();
        }
        return null;
    }


    protected JsonNode updateDoc(String col, BasicDBObject query, BasicDBObject newDocument) {
        UpdateResult result = null;
        collection = db.getCollection(col);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        log.info("{{Class-" + TAG + " : method-updateDoc}}-Update document: query-" + query);
        try {
            BasicDBObject updateObject = new BasicDBObject();
            updateObject.put("$set", newDocument);
            result = collection.updateMany(query, updateObject);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            cursor.close();
        }
        return Json.toJson(result);
    }


    protected JsonNode updateDocArray(String col, BasicDBObject query, BasicDBObject newDocument) {

        UpdateResult result = null;
        collection = db.getCollection(col);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        log.info("{{Class-" + TAG + " : method-updateDoc}}-Update document: query-" + query);
        try {
            BasicDBObject updateObject = new BasicDBObject();
            updateObject.put("$set", newDocument);
            result = collection.updateMany(query, updateObject);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            cursor.close();
        }
        return Json.toJson(result);
    }



    protected Document getQueryOneDoc(String col, BasicDBObject query) {
        log.info("{{Class-" + TAG + " : method-getQueryOneDoc}}-Get document: query-" + query);
        collection = db.getCollection(col);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        Document document = null;
        try {
            while (cursor.hasNext()) {
                document = cursor.next();

            }
            log.info("RESULT :" + document);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return (document);
    }


    protected Document getSingelDocuent(String col, BasicDBObject query) {

        log.info("{{Class-" + TAG + " : method-getSingelDocuent}}-Get document: query-" + query);
        collection = db.getCollection(col);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        Document document = null;
        try {

            while (cursor.hasNext()) {
                document = cursor.next();
            }
            log.info("RESULT :" + document);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        System.out.println(document);

        return (document);
    }


    protected ArrayList<Response> getAllResponseByQuery(String col, BasicDBObject query, int order) {
        collection = db.getCollection(col);
        log.info("{{Class-" + TAG + " : method-getAllResponseByQuery}}-Get document: query-" + query);

        System.out.println("Order " + order);


        ArrayList<Response> responsesList = new ArrayList<>();
        Document document = null;
        try {

            MongoCursor<Document> cursor = collection.find(query).sort(new BasicDBObject("date_time", -1)).iterator();
            //MongoCursor<Document> cursor = collection.find(query).iterator();

            while (cursor.hasNext()) {
                document = cursor.next();
                Response response = new Response();
                response.set_id(document.get("_id").toString());
                response.setDate_time(document.getLong("date_time"));
                response.setUser_id(document.getString("user_id"));
                response.setCurrent_city(document.getString("current_city"));
                response.setContact(document.getString("contact"));
                response.setRequest_date(document.getString("request_date"));
                response.setClient_request(document.getString("client_request"));
                response.setCourier_id(document.getString("courier_id"));
                response.setLast_name(document.getString("last_name"));
                response.setRequest_id(document.getString("request_id"));
                response.setFirst_name(document.getString("first_name"));
                response.setStatus(document.getString("status"));
                responsesList.add(response);
            }


            log.info(("RESULT :" + Json.toJson(responsesList)));


            //cursor.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            return responsesList;
        }


    }


    protected Document aggregateQuery(String from, String localField, String foreignField, String as, String matchField, String matchValue, String source) {
        try {
            Document result = null;
            log.info("{{Class-" + TAG + " : method-aggregateQuery}}");
            Bson project = new Document("$project", new Document("client_obj_request", new Document("$toObjectId", "$client_request"))
                    .append("date_time", "$date_time")
                    .append("user_id", "$user_id")
                    .append("first_name", "$first_name")
                    .append("last_name", "$last_name")
                    .append("current_city", "$current_city")
                    .append("contact", "$contact")
                    .append("request_date", "$request_date")
                    .append("client_request", "$client_request")
                    .append("courier_id", "$courier_id")
                    .append("status", "$status")


            );


            Bson lookup = new Document("$lookup",
                    new Document("from", from)
                            .append("localField", "client_obj_request")
                            .append("foreignField", foreignField)
                            .append("as", as));


            Bson matchFliter = new Document("$match", new Document(matchField, new ObjectId(matchValue)));
            List<Bson> filters = new ArrayList<>();
            filters.add(project);
            filters.add(lookup);
            filters.add(matchFliter);

            AggregateIterable<Document> it = db.getCollection(source).aggregate(filters);
            for (Document doc : it) {
                result = doc;
            }

            return result;
        } catch (MongoException e) {
            log.error(e.getMessage());
        }

        return null;
    }


    protected ArrayList<Document> aggregateQueryDefault(String from, String localField, String foreignField, String as, String matchField, String matchValue, String source) {
        ArrayList<Document> result = new ArrayList<>();
        try {

            Document docs = null;
            log.info("{{Class-" + TAG + " : method-aggregateQueryDefault}}");

            Bson lookup = new Document("$lookup",
                    new Document("from", from)
                            .append("localField", localField)
                            .append("foreignField", foreignField)
                            .append("as", as));


            Bson matchFliter = new Document("$match", new Document(matchField, new ObjectId(matchValue)));
            List<Bson> filters = new ArrayList<>();
            filters.add(lookup);
            filters.add(matchFliter);

            AggregateIterable<Document> it = db.getCollection(source).aggregate(filters);
            for (Document document : it) {
                docs = document;
                result.add(docs);
            }

            return result;
        } catch (MongoException e) {
            log.error(e.getMessage());
        }

        return null;
    }


    protected ArrayList<Document> aggregateQuery2(String from, String localField, String foreignField, String as, String matchField, String matchValue, String source) {
        ArrayList<Document> result = new ArrayList<>();
        try {

            Document docs = null;
            log.info("{{Class-" + TAG + " : method-aggregateQuery2}}");

            Bson lookup = new Document("$lookup",
                    new Document("from", from)
                            .append("localField", localField)
                            .append("foreignField", foreignField)
                            .append("as", as));


            Bson matchFliter = new Document("$match", new Document(matchField, matchValue));
            Bson courierStatus1 = new Document("$match", new Document("status", "working"));
            Bson courierStatus2 = new Document("$match", new Document("courier_status", "pending"));
            List<Bson> filters = new ArrayList<>();

            filters.add(lookup);
            filters.add(matchFliter);
            filters.add(courierStatus1);
            filters.add(courierStatus2);

            AggregateIterable<Document> it = db.getCollection(source).aggregate(filters);
            for (Document document : it) {
                docs = document;
                result.add(docs);
            }

            System.out.println(Json.toJson(result));
            return result;
        } catch (MongoException e) {
            log.error(e.getMessage());
        }

        return null;
    }


    protected Document aggregateQuery3(String from, String localField, String foreignField, String as, String matchField, String matchValue, String source) {
        try {
            Document result = null;
            log.info("{{Class-" + TAG + " : method-aggregateQuery3}}");
            Bson project = new Document("$project", new Document("client_obj_request", new Document("$toObjectId", "$client_request"))
                    .append("date_time", "$date_time")
                    .append("user_id", "$user_id")
                    .append("first_name", "$first_name")
                    .append("last_name", "$last_name")
                    .append("current_city", "$current_city")
                    .append("contact", "$contact")
                    .append("request_date", "$request_date")
                    .append("client_request", "$client_request")
                    .append("courier_id", "$courier_id")
                    .append("status", "$status")


            );


            Bson lookup = new Document("$lookup",
                    new Document("from", from)
                            .append("localField", "client_obj_request")
                            .append("foreignField", foreignField)
                            .append("as", as));


            Bson matchFliter1 = new Document("$match", new Document(matchField, new ObjectId(matchValue)));
            Bson matchFliter2 = new Document("$match", new Document("status", new Document("$ne", "complete")));
            List<Bson> filters = new ArrayList<>();
            filters.add(project);
            filters.add(lookup);
            filters.add(matchFliter1);
            filters.add(matchFliter2);

            AggregateIterable<Document> it = db.getCollection(source).aggregate(filters);
            for (Document doc : it) {
                result = doc;
            }

            return result;
        } catch (MongoException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    protected Document aggregate4(String col, String qrID, String courierID) {
        Document result = null;
        try {
            Bson rule = new Document("$match", new Document("qr_id", qrID).append("couriers", new Document("$elemMatch", new Document("courier_id", courierID))));
            List<Bson> filters = new ArrayList<>();
            filters.add(rule);
            AggregateIterable<Document> it = db.getCollection(col).aggregate(filters);

            for (Document doc : it) {
                result = doc;
            }
            return result;
        } catch (MongoException e) {
            log.error(e.getMessage());
        }

        return null;
    }


}