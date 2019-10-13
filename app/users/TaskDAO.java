package users;
import ExternalAPIs.ExternalAPIDAO;
import JWT.JWTUtils;
import com.fasterxml.jackson.databind.JsonNode;



import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;

import database.Mongo;
import org.bson.Document;

import org.bson.types.ObjectId;
import play.libs.Json;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;

@Singleton
public class TaskDAO extends ExternalAPIDAO {
    private JWTUtils jwtUtils;
    private static String COLLECTION_NAME = "user";
    @Inject
    public TaskDAO(Mongo mongo, JWTUtils jwtUtils) {
        super(mongo);
        this.jwtUtils = jwtUtils;
    }
//create system user
    public JsonNode createSystemUser(JsonNode u){
      Document document = Document.parse(u.toString());
      String objectId = insertDoc(document,COLLECTION_NAME).toString();
      Map<String, Object> response = new HashMap<>();
      response.put("status","success");
      response.put("user_id",objectId);
      return (Json.toJson(response));
    }

    //get all user from system
    public JsonNode getAllUsers(){
        ArrayList<Document> documents = getAllDoc("user");
        ArrayList<JsonNode> users = new ArrayList<>();
        ObjectNode obj = Json.newObject();
        for(Document document: documents){

            obj.put("id",document.get("_id").toString());
            obj.put("username",document.get("username").toString());
            obj.put("email",document.get("email").toString());
            obj.put("contact",document.get("contact").toString());
            obj.put("role",document.get("role").toString());

            JsonNode jsonNode = Json.toJson(obj);
            users.add(jsonNode);
       }
        return Json.toJson(users);

    }
    //get find user from system
    public JsonNode findUser(JsonNode u) {
        JsonNode user = u;
        BasicDBObject SearchQuery = new BasicDBObject();
        SearchQuery.put("username", user.findPath("username").textValue());
        SearchQuery.put("password", user.findPath("password").textValue());
        Map<String, String> res = new HashMap<>();
        res.put("status","bad");
        ArrayList<Document> documents = getQueryDoc("user", SearchQuery);
        if(!documents.isEmpty()) {
            ArrayList<JsonNode> users = new ArrayList<>();
            ObjectNode obj = Json.newObject();
            for (Document document : documents) {

                obj.put("id", document.get("_id").toString());
                obj.put("username", document.get("username").toString());
                obj.put("email", document.get("email").toString());
                obj.put("contact", document.get("contact").toString());
                obj.put("role", document.get("role").toString());

                JsonNode jsonNode = Json.toJson(obj);
                users.add(jsonNode);
            }


            return Json.toJson(users);
        }else{
            return Json.toJson(res);
        }
    }



    public boolean findLoginUser(JsonNode u) {
        boolean check = false;
        BasicDBObject SearchQuery = new BasicDBObject();
        SearchQuery.put("username", u.findPath("username").textValue());
        SearchQuery.put("password", u.findPath("password").textValue());
        Map<String, String> res = new HashMap<>();
        res.put("status", "bad");
        ArrayList<Document> documents = getQueryDoc("user", SearchQuery);
        if (!documents.isEmpty()) {
            check = false;
        } else {
            check = true;
        }
        return check;
    }



        public JsonNode RemoveUser(String id){
            BasicDBObject query = new BasicDBObject();
            query.put("_id",new ObjectId(id));
            return (deleteDoc("user",query));

    }



    public JsonNode Update(String id){
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("username", "John");
        query.put("_id",new ObjectId(id));
        return(updateDoc("user",query,newDocument));

    }



    }
