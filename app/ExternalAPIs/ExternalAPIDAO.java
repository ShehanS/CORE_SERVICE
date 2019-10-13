package ExternalAPIs;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import database.Mongo;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import play.libs.Json;

import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class ExternalAPIDAO {

    protected static MongoDatabase db;
    private MongoCollection collection =null;


    public ExternalAPIDAO(Mongo mongo){
            db = mongo.db;
        }


    protected ObjectId insertDoc(Document doc, String colName){
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        db = db.withCodecRegistry(pojoCodecRegistry);
        collection = db.getCollection(colName);
        collection = collection.withCodecRegistry(pojoCodecRegistry);
        collection.insertOne(doc);
        ObjectId id = doc.getObjectId("_id");
        return id;
    }

    protected ArrayList<Document> getAllDoc(String colName){
        ArrayList<Document> DocArrayList = new ArrayList<>();
        collection = db.getCollection(colName);
        MongoCursor<Document> cursor= collection.find().iterator();
        try{
            while(cursor.hasNext()){
                Document document = cursor.next();
                DocArrayList.add(document);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
          return DocArrayList;
    }


    protected ArrayList<Document> getQueryDoc(String col, BasicDBObject query){
        SearchResult result = null;
        ArrayList<Document> DocArrayList = new ArrayList<>();
        collection = db.getCollection(col);
        MongoCursor<Document> cursor= collection.find(query).iterator();

        try{


            while(cursor.hasNext()){
                Document document = cursor.next();
                DocArrayList.add(document);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return DocArrayList;
    }


    protected JsonNode deleteDoc(String col, BasicDBObject query){
        DeleteResult result = null;
        collection = db.getCollection(col);
        MongoCursor<Document> cursor= collection.find(query).iterator();
        try{
            result= collection.deleteOne(query);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return Json.toJson(result);
    }


    protected JsonNode updateDoc(String col, BasicDBObject query,BasicDBObject newDocument){
        UpdateResult result = null;
        collection = db.getCollection(col);
        MongoCursor<Document> cursor= collection.find(query).iterator();
        try{
            BasicDBObject updateObject = new BasicDBObject();
            updateObject.put("$set", newDocument);
            result= collection.updateOne(query, updateObject);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return Json.toJson(result);
    }
}
