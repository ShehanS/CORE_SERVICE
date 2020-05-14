package users;

import AESEncryptionDecryption.ASE;
import ExternalAPIs.ExternalAPIDAO;
import JWT.JWTUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import database.Mongo;
import model.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import play.libs.Json;
import service.Clock;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Singleton
public class TaskDAO extends ExternalAPIDAO {
    private static final Logger log = LogManager.getLogger(TaskDAO.class);
    private static String USER_COLLECTION = "user";
    private JWTUtils jwtUtils;
    private static String CLIENT_REQEST_COLLECTION = "client_request";
    private static String EMPLOYEE_ATTENDANCE = "employee_attendance";
    private static String FACTORY_CONFIG = "config_data";
    private static String COURIER_LOCATION = "courier_location";
    private static String RESPONSE = "response";
    private static String CURRENT_DATE;
    private static String DAY_BEFORE_DATE;
    private static String TRANSACTION = "transaction";


    final String secretKey = "citypack";
    private ASE ase;
    private Clock clock;

    @Inject
    public TaskDAO(Mongo mongo, JWTUtils jwtUtils, ASE ase, Clock clock) {
        super(mongo);
        this.jwtUtils = jwtUtils;
        this.ase = ase;
        clock = clock;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();
        CURRENT_DATE = (dtf.format(now));
        LocalDateTime dayBefore = now.minusDays(1);
        DAY_BEFORE_DATE = (dtf.format(dayBefore));

        log.info("Date - range1 " + DAY_BEFORE_DATE + " to " + CURRENT_DATE);
        log.info("Date - range2 " + clock.getDayBeforeDate() + " to " + clock.getCurrentDate());
    }

    /**
     * All the user can create here
     *
     * @param u is json body
     **/
    public JsonNode createUser(JsonNode u) {
        ase.setKey(secretKey);
        Document document = new Document();
        document.append("first_name", u.findPath("first_name").asText());
        document.append("last_name", u.findPath("last_name").asText());
        document.append("gender", u.findPath("gender").asText());
        document.append("age", (u.findPath("age").asInt()));
        document.append("address", u.findPath("address").asText());
        document.append("city", u.findPath("city").asText());
        document.append("state", u.findPath("state").asText());
        document.append("postal_code", u.findPath("postal_code").asText());
        document.append("contact", u.findPath("contact").asText());
        document.append("email", u.findPath("email").asText());
        document.append("nic_or_passport", u.findPath("nic_or_passport").asText());
        document.append("user_profile", u.findPath("user_profile").asText());
        document.append("role", u.findPath("role").asText());
        document.append("username", u.findPath("username").asText());
        document.append("password", ase.encrypt(u.findPath("password").asText(), secretKey));
        document.append("timestamp", u.findPath("timestamp").asDouble());
        document.append("device", u.findPath("device").asText());
        document.append("account_status", "deactive");
        document.append("update", true);
        document.append("duration", 10000);

        if (checkExistingUser(u.findPath("username").textValue(), u.findPath("nic_or_passport").textValue(), u.findPath("email").textValue()) == false) {
            String objectId = insertDoc(document, USER_COLLECTION).toString();
            Map<String, Object> response = new HashMap<>();
            response.put("args1", "success");
            response.put("args2", objectId);
            response.put("args3", u.findPath("username").asText());
            response.put("args4", u.findPath("password").asText());
            response.put("args5", "Account is created!!");
            return (Json.toJson(response));
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("args1", "failed");
            response.put("args5", "Existing user found. Please check NIC/passport number, email and username");
            return (Json.toJson(response));

        }
    }

    //get all user from db
    public JsonNode getAllUsers() {
        ArrayList<Document> documents = getAllDoc(USER_COLLECTION);
        ArrayList<JsonNode> users = new ArrayList<>();
        ObjectNode obj = Json.newObject();
        try {
            for (Document document : documents) {
                obj.put("id", document.get("_id").toString());
                obj.put("username", document.get("username").toString());
                obj.put("first_name", document.get("first_name").toString());
                obj.put("last_name", document.get("last_name").toString());
                obj.put("gender", document.get("gender").toString());
                obj.put("age", document.get("age").toString());
                obj.put("address", document.get("address").toString());
                obj.put("city", document.get("city").toString());
                obj.put("state", document.get("state").toString());
                obj.put("postalCode", document.get("postal_code").toString());
                obj.put("contact", document.get("contact").toString());
                obj.put("email", document.get("email").toString());
                obj.put("nic_or_passport", document.get("nic_or_passport").toString());
                obj.put("user_profile", document.get("user_profile").toString());
                obj.put("role", document.get("role").toString());
                obj.put("username", document.get("username").toString());
                obj.put("password", document.get("password").toString());
                obj.put("added_time", document.get("timestamp").toString());
                obj.put("device", document.get("device").toString());
                obj.put("account_status", document.get("account_status").toString());
                JsonNode user = Json.toJson(obj);
                users.add(user);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Json.toJson(users);

    }

    //get find user from system
    public JsonNode findUser(JsonNode u) {
        JsonNode user = u;
        BasicDBObject SearchQuery = new BasicDBObject();
        SearchQuery.put("username", user.findPath("username").textValue());

        Map<String, String> res = new HashMap<>();
        res.put("status", "bad");
        ArrayList<Document> documents = getQueryDoc("user", SearchQuery);
        if (!documents.isEmpty()) {
            ArrayList<JsonNode> users = new ArrayList<>();
            ObjectNode obj = Json.newObject();
            for (Document document : documents) {

                obj.put("id", document.get("_id").toString());
                obj.put("username", document.get("username").toString());
                obj.put("email", document.get("email").toString());
                obj.put("contact", document.get("contact").toString());
                obj.put("password", document.get("password").toString());


                JsonNode jsonNode = Json.toJson(obj);
                users.add(jsonNode);
            }


            return Json.toJson(users);
        } else {
            return Json.toJson(res);
        }
    }


    //get find user from system
    public JsonNode findUserById(String id) {
        BasicDBObject SearchQuery = new BasicDBObject();
        SearchQuery.put("_id", new ObjectId(id));

        Map<String, String> res = new HashMap<>();
        res.put("status", "bad");
        ArrayList<Document> documents = getQueryDoc(USER_COLLECTION, SearchQuery);
        if (!documents.isEmpty()) {
            ObjectNode obj = Json.newObject();
            for (Document document : documents) {
                obj.put("id", document.get("_id").toString());
                obj.put("username", document.get("username").toString());
                obj.put("first_name", document.get("first_name").toString());
                obj.put("last_name", document.get("last_name").toString());
                obj.put("gender", document.get("gender").toString());
                obj.put("age", document.get("age").toString());
                obj.put("address", document.get("address").toString());
                obj.put("city", document.get("city").toString());
                obj.put("state", document.get("state").toString());
                obj.put("postal_code", document.get("postal_code").toString());
                obj.put("contact", document.get("contact").toString());
                obj.put("email", document.get("email").toString());
                obj.put("nic_or_passport", document.get("nic_or_passport").toString());
                obj.put("user_profile", document.get("user_profile").toString());
                obj.put("role", document.get("role").toString());
                obj.put("username", document.get("username").toString());
                String password = ase.decrypt(document.get("password").toString(), secretKey);
                obj.put("password", password);
                obj.put("added_time", document.get("timestamp").toString());
                obj.put("device", document.get("device").toString());
                obj.put("account_status", document.get("account_status").toString());


            }


            return Json.toJson(obj);
        } else {
            return Json.toJson(res);
        }
    }

    /**
     * find user and user details from user collection, getQueryForSingelDoc method return value as document.
     *
     * @Params u username and password,
     * Create Access-Token using jwt token using
     * @Params ase.decrypt(
     */
    public JsonNode findLoginUser(JsonNode u) {
        Map<String, Object> auth_user = new HashMap<>();
        BasicDBObject SearchQuery = new BasicDBObject();
        SearchQuery.put("username", u.findPath("username").textValue());
        Map<String, String> checkPassword = new HashMap<>();
        Map<String, String> error = new HashMap<>();
        checkPassword.put("error", "wrong_password"); //define errors
        checkPassword.put("code", "err002");
        Map<String, String> checkUser = new HashMap<>();
        checkUser.put("error", "no_user");
        checkUser.put("code", "err001");


        try {
            Document user = getQueryForSingelDoc(USER_COLLECTION, SearchQuery);
            String password;
            password = ase.decrypt(user.get("password").toString(), secretKey);

            if (user.get("username").toString() == null) {
                return Json.toJson(checkUser);
            }
            if (u.findPath("password").textValue().equals(password)) {
                String JWTToken = jwtUtils.genarateJWT(u);
                auth_user.put("username", user.get("username").toString());
                auth_user.put("first_name", user.get("first_name").toString());
                auth_user.put("last_name", user.get("last_name").toString());
                auth_user.put("role", user.get("role").toString());
                auth_user.put("user_id", user.get("_id").toString());
                auth_user.put("account_status", user.get("account_status").toString());
                auth_user.put("contact", user.get("contact").toString());
                auth_user.put("access_token", JWTToken);
                auth_user.put("update", user.getBoolean("update"));


                return Json.toJson(auth_user);
            } else {
                return Json.toJson(checkPassword);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Json.toJson(checkUser);
    }


    //remove document
    public JsonNode RemoveUser(String id) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));
        try {
            return (deleteDoc(USER_COLLECTION, query));
        } catch (MongoException e) {
            log.error(e.getMessage());
        }
        return null;
    }


    public JsonNode Update(String id, JsonNode u) {
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("first_name", u.findPath("first_name").textValue());
        newDocument.put("last_name", u.findPath("last_name").textValue());
        newDocument.put("gender", u.findPath("gender").textValue());
        newDocument.put("gender", u.findPath("gender").textValue());
        newDocument.put("age", u.findPath("age").intValue());
        newDocument.put("address", u.findPath("address").textValue());
        newDocument.put("city", u.findPath("city").textValue());
        newDocument.put("state", u.findPath("state").textValue());
        newDocument.put("postal_code", u.findPath("postal_code").textValue());
        newDocument.put("contact", u.findPath("contact").textValue());
        newDocument.put("email", u.findPath("email").textValue());
        newDocument.put("nic_or_passport", u.findPath("nic_or_passport").textValue());
        newDocument.put("role", u.findPath("role").textValue());
        newDocument.put("username", u.findPath("username").textValue());
        newDocument.put("password", ase.encrypt(u.findPath("password").asText(), secretKey));
        if ((u.findPath("device").textValue() == "") || (u.findPath("device").textValue() == null)) {
            newDocument.put("device", "WEB-APP");
        } else {
            newDocument.put("device", u.findPath("device").textValue());
        }
        query.put("_id", new ObjectId(id));
        return (updateDoc(USER_COLLECTION, query, newDocument));


    }


    public JsonNode updateProfileStatus(String id, JsonNode u) {
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("update", u.findPath("status").asBoolean());
        query.put("_id", new ObjectId(id));
        return (updateDoc(USER_COLLECTION, query, newDocument));

    }


    public JsonNode UpdateActivation(String id, JsonNode u) {
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("account_status", u.findPath("activation").textValue());
        query.put("_id", new ObjectId(id));
        return (updateDoc(USER_COLLECTION, query, newDocument));

    }


    public JsonNode clientRequest(JsonNode r) {
        Document document = Document.parse(r.toString());
        JsonNode user = findUserById(r.findPath("user_id").asText());
        document.append("contact", user.findPath("contact").textValue());
        String objectId = insertDoc(document, CLIENT_REQEST_COLLECTION).toString();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("id", objectId);
        return (Json.toJson(response));
    }


    //mark attendance
    public JsonNode addAttendance(JsonNode a) {
        Document document = Document.parse(a.toString());
        String objectId = insertDoc(document, EMPLOYEE_ATTENDANCE).toString();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("user_id", objectId);
        return (Json.toJson(response));
    }


    //response request
    public JsonNode addResponse(JsonNode a) {
        Document document = Document.parse(a.toString());
        String objectId = insertDoc(document, RESPONSE).toString();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("id", objectId);
        return (Json.toJson(response));
    }


    //get attendance
    public JsonNode getEmployeeAttendance(JsonNode request) {
        JsonNode requestRange = request;
        Long startDate = Long.parseLong(requestRange.findPath("start_date").asText());
        Long endDate = Long.parseLong(requestRange.findPath("end_date").asText());
        BasicDBObject query = new BasicDBObject("date_time", new BasicDBObject("$gte", startDate).append("$lte", endDate));
        System.out.println("formed query: " + query);
        Map<String, String> res = new HashMap<>();
        res.put("status", "bad");
        ArrayList<Document> documents = getQueryDoc(EMPLOYEE_ATTENDANCE, query);
        if (!documents.isEmpty()) {
            ArrayList<JsonNode> empAttendaneces = new ArrayList<>();
            ObjectNode obj = Json.newObject();
            for (Document document : documents) {
                obj.put("id", document.get("_id").toString());
                List<Document> location = (List<Document>) document.get("location");
                obj.put("location", Json.toJson(location));
                obj.put("address", document.get("address").toString());
                obj.put("date_time", document.get("date_time").toString());
                JsonNode userDetails = findUserById(document.get("user_id").toString());
                obj.put("first_name", userDetails.findPath("first_name").asText());
                obj.put("last_name", userDetails.findPath("last_name").asText());
                obj.put("contact", userDetails.findPath("contact").asText());
                obj.put("role", userDetails.findPath("role").asText());
                System.out.println(userDetails);
                JsonNode jsonNode = Json.toJson(obj);
                empAttendaneces.add(jsonNode);
            }

            return Json.toJson(empAttendaneces);
        } else {
            return Json.toJson(res);
        }
    }


//get all client request

    public JsonNode getClientAllRequests(JsonNode request) {
        JsonNode requestRange = request;
        String userID = requestRange.findPath("user_id").textValue();
        Long startDate = Long.parseLong(requestRange.findPath("start_date").asText());
        Long endDate = Long.parseLong(requestRange.findPath("end_date").asText());
        BasicDBObject query = new BasicDBObject("request_time", new BasicDBObject("$gte", startDate).append("$lte", endDate));
        System.out.println("formed query: " + query);
        Map<String, String> res = new HashMap<>();
        res.put("status", "bad");
        ArrayList<Document> documents = getQueryDoc(CLIENT_REQEST_COLLECTION, query);
        if (!documents.isEmpty()) {
            ArrayList<JsonNode> requestPickup = new ArrayList<>();

            ObjectNode obj = Json.newObject();
            for (Document document : documents) {
                obj.put("id", document.get("_id").toString());
                obj.put("user_id", document.get("user_id").toString());
                obj.put("first_name", document.get("first_name").toString());
                obj.put("last_name", document.get("last_name").toString());
                List<Document> location = (List<Document>) document.get("location");
                obj.put("location", Json.toJson(location));
                obj.put("address", document.get("address").toString());
                obj.put("request_time", document.get("request_time").toString());
                obj.put("device_id", document.get("address").toString());
                List<Document> item = (List<Document>) document.get("item_info");
                obj.put("item", Json.toJson(item));
                List<Document> deviceInfo = (List<Document>) document.get("delevery_info");
                obj.put("item", Json.toJson(deviceInfo));
                JsonNode jsonNode = Json.toJson(obj);
                requestPickup.add(jsonNode);
            }
            return Json.toJson(requestPickup);
        } else {
            return Json.toJson(res);
        }
    }


//    //get find user from system
//    public boolean checkExistingUser(String username, String nic, String email) {
//        BasicDBObject SearchQuery = new BasicDBObject();
//
//        SearchQuery.put("username", username);
//        SearchQuery.put("nic_or_passport", nic);
//        SearchQuery.put("email", email);
//          //Document documents = getQueryForSingelDoc1(USER_COLLECTION, username, email);
//
//        getQueryForSingelDoc1(USER_COLLECTION, username, email);
////        if(!documents.isEmpty()) {
////            return true;
////        }else{
////            return false;
////        }
//
//        return false;
//    }


    //get find user from system
    public boolean checkExistingUser(String username, String nic, String email) {
        ArrayList<Document> result = getQueryForUser(USER_COLLECTION, username, nic, email);
        if (result.size() > 0) {
            return true;
        }
        return false;
    }


    //get app config
    public JsonNode getFactoryConfig(String token, String confType, String listName) {
        BasicDBObject searchQuery = new BasicDBObject();
        Map<String, Object> factoryConf = new HashMap<>();
        searchQuery.put("token", token);
        try {

            searchQuery.put("conf_type", confType);
            searchQuery.put("list_name", listName);
            log.info("GET factory config\n");
            Document configDoc = getQueryOneDoc(FACTORY_CONFIG, searchQuery);
            if (!configDoc.isEmpty()) {
                factoryConf.put("_id", configDoc.get("_id").toString());
                factoryConf.put("item_list", configDoc.get("item_list"));
                return Json.toJson(factoryConf);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;

    }


    //find courier
    public ArrayList<JsonNode> findLocationBaseCourier(String city) {
        BasicDBObject searchQuery = new BasicDBObject();
        ArrayList<JsonNode> allCouriers = new ArrayList<>();
        searchQuery.put("current_city", city);
        //searchQuery.put("request_date",  clock.getCurrentDate());
        ArrayList<Document> courierDoc = getQueryDoc(COURIER_LOCATION, searchQuery);



        try {
            if (!courierDoc.isEmpty()) {
                for (Document document : courierDoc) {
                    ObjectNode couriers = Json.newObject();
                    couriers.put("id", document.get("_id").toString());
                    couriers.put("courier_id", document.get("courier_id").toString());
                    couriers.put("first_name", document.get("first_name").toString());
                    couriers.put("last_name", document.get("last_name").toString());
                    couriers.put("current_city", document.get("current_city").toString());
                    couriers.put("contact", document.get("contact").toString());
                    couriers.put("status", document.get("status").toString());
                    couriers.put("job_running", document.getBoolean("job_running"));
                    allCouriers.add(couriers);
                }


                return allCouriers;


            }
        } catch (MongoException e) {
            log.error(e.getMessage());
        }
        return null;
    }


    //assign courier by id
    public JsonNode getCourierById(String id) {
        Map<String, Object> courier = new HashMap<>();
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("courier_id", id);
        //searchQuery.put("request_date",  clock.getCurrentDate());


        try {
            Document document = getSingelDocuent(COURIER_LOCATION, searchQuery);

            courier.put("id", document.get("_id").toString());
            courier.put("courier_id", document.get("courier_id").toString());
            courier.put("first_name", document.get("first_name").toString());
            courier.put("last_name", document.get("last_name").toString());
            courier.put("current_city", document.get("current_city").toString());
            courier.put("contact", document.get("contact").toString());
            courier.put("status", document.get("status").toString());
            courier.put("job_running", document.getBoolean("job_running"));

            return Json.toJson(courier);
        } catch (MongoException e) {
            log.error(e.getMessage());
        }
        return null;
    }


    public JsonNode assignCourierById(String courierID, String responseID) {
        JsonNode courier = this.getCourierById(courierID);
        JsonNode status = this.updateResponseByResponseID(responseID, courier);
        return status;

    }


    public JsonNode checkCourierStatus(String id) {
        Boolean status = false;
        Map<String, Object> res = new HashMap<>();
        BasicDBObject query = new BasicDBObject();
        query.put("courier_id", id);
        Document document = getSingelDocuent(COURIER_LOCATION, query);

        if ((document == null) || (document.equals("null"))) {
            status = false;
            res.put("status", status);
            res.put("message", "courier not found");

        } else {
            status = true;
            res.put("status", status);
            res.put("message", "courier is working");
        }
        return Json.toJson(res);
    }


    //getAllCouriers
    public JsonNode getAllCouriers() {
        BasicDBObject searchQuery = new BasicDBObject();
        ArrayList<JsonNode> allCouriers = new ArrayList<>();
        searchQuery.put("role", "Courier");
        ArrayList<Document> courierDoc = getQueryDoc(USER_COLLECTION, searchQuery);

        try {
            if (!courierDoc.isEmpty()) {
                for (Document document : courierDoc) {
                    ObjectNode couriers = Json.newObject();
                    // couriers.put("id", document.get("_id").toString());
                    couriers.put("id", document.getObjectId("_id").toString());
                    couriers.put("first_name", document.get("first_name").toString());
                    couriers.put("last_name", document.get("last_name").toString());
                    // couriers.put("current_city", document.get("current_city").toString());
                    couriers.put("contact", document.get("contact").toString());
                    //couriers.put("status", document.get("status").toString());
                    // couriers.put("job_running", document.getBoolean("job_running"));
                    allCouriers.add(couriers);
                }
                return Json.toJson(allCouriers);
            }
        } catch (MongoException e) {
            log.error(e.getMessage());
        }
        return null;
    }









    //find courier
    public long jobCountCourierWise(String id) {
        Clock clock = new Clock();
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date_time", new BasicDBObject("$gt", clock.getMillPriviousDate()).append("$lte", clock.getMillCurrentEpoch()));
        long jobCount = getDocCount(RESPONSE, searchQuery);
        return jobCount;
    }


    //get request status using different signature(by id)
    public JsonNode getRequestUpdate(String id) {
        Map<String, Object> response = new HashMap<>();
        BasicDBObject SearchQuery = new BasicDBObject();
        try {
            SearchQuery.put("_id", new ObjectId(id));
            Document responseStatus = getSingelDocuent(RESPONSE, SearchQuery);
            response.put("contact", responseStatus.get("contact"));
            response.put("courier_id", responseStatus.get("courier_id"));
            response.put("first_name", responseStatus.get("first_name"));
            response.put("last_name", responseStatus.get("last_name"));
            response.put("request_id", responseStatus.get("request_id"));
            response.put("save_id", responseStatus.get("_id").toString());
            response.put("status", responseStatus.get("status"));
            response.put("date", responseStatus.get("request_date"));

        } catch (MongoException e) {
            e.printStackTrace();
        } finally {
            return Json.toJson(response);
        }

    }

    public ArrayList<Response> getCurrentRequestQue() {
        Clock clock = new Clock();
        log.info("Date - in query " + clock.getDayBeforeDate() + " to " + clock.getCurrentDate());
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date_time", new BasicDBObject("$gt", clock.getMillPriviousDate()).append("$lte", clock.getMillCurrentEpoch()));
        ArrayList<Response> res = new ArrayList<>();
        res = getAllResponseByQuery(RESPONSE, searchQuery, -1);
        System.out.println(Json.toJson(res));
        return res;
    }


    public ArrayList<Response> getCurrentRequestQueDateWise(JsonNode request) {
        log.info("Date - in query " + request.findPath("start_date").asLong() + " to " + request.findPath("end_date").asLong());
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date_time", new BasicDBObject("$gt", request.findPath("start_date").asLong()).append("$lte", request.findPath("end_date").asLong()));
        ArrayList<Response> res = new ArrayList<>();
        res = getAllResponseByQuery(RESPONSE, searchQuery, 1); //// issuese
        System.out.println(Json.toJson(res));
        return res;
    }


    public ArrayList<Response> getCurrentPendingRequestQueDateWise(JsonNode request) {
        log.info("Date - in query " + request.findPath("start_date").asLong() + " to " + request.findPath("end_date").asLong());
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("status", "pending");
        searchQuery.put("date_time", new BasicDBObject("$gt", request.findPath("start_date").asLong()).append("$lte", request.findPath("end_date").asLong()));
        ArrayList<Response> res = new ArrayList<>();
        res = getAllResponseByQuery(RESPONSE, searchQuery, 1); //// issuese
        System.out.println(Json.toJson(res));
        return res;
    }


    //update response status
    public JsonNode updateResponse(String q, JsonNode update) {
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("contact", update.findPath("contact").textValue());
        newDocument.put("courier_id", update.findPath("courier_id").textValue());
        newDocument.put("last_name", update.findPath("last_name").textValue());
        newDocument.put("first_name", update.findPath("first_name").textValue());
        newDocument.put("status", update.findPath("status").textValue());
        query.put("current_city", q);
        return (updateDoc(RESPONSE, query, newDocument));

    }


    //update response status
    public JsonNode updateResponseByResponseID(String id, JsonNode update) {
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("contact", update.findPath("contact").textValue());
        newDocument.put("courier_id", update.findPath("courier_id").textValue());
        newDocument.put("last_name", update.findPath("last_name").textValue());
        newDocument.put("first_name", update.findPath("first_name").textValue());
        newDocument.put("status", update.findPath("status").textValue());
        query.put("_id", new ObjectId(id));
        return (updateDoc(RESPONSE, query, newDocument));

    }


    public JsonNode getUserAndQueryDetails(String id) {
        Document result = aggregateQuery("client_request", "client_request", "_id", "request_details", "_id", id, "response");
        return Json.toJson(result);

    }


    public JsonNode clientLocation(JsonNode updates) {
        Map<String, String> response = new HashMap<>();
        Document location = new Document();
        location.put("courier_id", updates.findPath("courier_id").textValue());
        location.put("current_city", updates.findPath("current_city").textValue());
        location.put("first_name", updates.findPath("first_name").textValue());
        location.put("last_name", updates.findPath("last_name").textValue());
        location.put("contact", updates.findPath("contact").textValue());
        location.put("status", updates.findPath("status").textValue());
        location.put("job_running", updates.findPath("job_running").asBoolean());
        location.put("date_time", updates.findPath("date_time").asLong());
        location.put("location_date", updates.findPath("location_date").textValue());
        String id = insertDoc(location, COURIER_LOCATION).toString();

        response.put("id", id);
        response.put("message", "update");

        return Json.toJson(response);


    }


    public JsonNode getCourierJobs(String id) {
        Clock clock = new Clock();
        ArrayList<Document> arrayList = new ArrayList<>();
        ArrayList<JsonNode> jobList = new ArrayList<>();
        Map<String, Object> jobs = new HashMap<>();
        BasicDBObject query = new BasicDBObject();
        query.put("courier_id", id);
        query.put("date_time", new BasicDBObject("$gt", clock.getMillPriviousDate()).append("$lte", clock.getMillCurrentEpoch()));
        arrayList = getQueryDoc(CLIENT_REQEST_COLLECTION, query);

        for (Document doc : arrayList) {
            jobs.put("job_id", doc.get("_id").toString());
            jobs.put("client_first_name", doc.getString("first_name"));
            jobs.put("client_last_name", doc.getString("last_name"));
            List<Document> location = (List<Document>) doc.get("current_location");
            jobs.put("client_location", location);
            jobs.put("client_address", doc.getString("current_address"));
            jobs.put("current_city", doc.getString("current_city"));
            List<Document> items = (List<Document>) doc.get("delevery_items");
            jobs.put("delevery_items", items);
            List<Document> receiver = (List<Document>) doc.get("receiver_details");
            jobs.put("receiver_details", receiver);
            jobs.put("request_date", doc.getString("request_date"));
            jobs.put("date_time", doc.getLong("date_time"));

            jobList.add(Json.toJson(jobs));

        }


        return Json.toJson(jobList);


    }


    public JsonNode getAllRequestDetails(String id) {
        ArrayList<Object> jobs = new ArrayList<>();

        ArrayList<Document> arrayResult = aggregateQuery2("client_request", "user_id", "user_id", "client_details", "courier_id", id, "response");

        for (Document document : arrayResult) {
            Map<String, Object> response = new HashMap<>();


            response.put("response_id", document.get("_id").toString());
            response.put("date_time", document.getLong("date_time"));
            response.put("user_id", document.getString("user_id"));
            response.put("current_city", document.getString("current_city"));
            response.put("contact", document.getString("contact"));
            response.put("request_date", document.getString("request_date"));
            response.put("client_request", document.getString("client_request"));
            response.put("courier_id", document.getString("courier_id"));
            response.put("last_name", document.getString("last_name"));
            response.put("request_id", document.getString("request_id"));
            response.put("first_name", document.getString("first_name"));
            response.put("status", document.getString("status"));
            response.put("courier_status", document.getString("courier_status"));
            response.put("comment", document.getString("comment"));
            List<Document> clientDetails = (List<Document>) document.get("client_details");
            for (Document doc : clientDetails) {
                Map<String, Object> requestDetails = new HashMap<>();
                requestDetails.put("client_request_id", doc.get("_id").toString());
                requestDetails.put("user_id", doc.getString("user_id"));
                requestDetails.put("first_name", doc.getString("first_name"));
                requestDetails.put("last_name", doc.getString("last_name"));
                List<Document> location = (List<Document>) doc.get("current_location");
                requestDetails.put("current_location", location);
                requestDetails.put("current_address", doc.getString("current_address"));
                requestDetails.put("current_city", doc.getString("current_city"));
                List<Document> items = (List<Document>) doc.get("delevery_items");
                requestDetails.put("delevery_items", items);
                List<Document> receiver = (List<Document>) doc.get("receiver_details");
                requestDetails.put("receiver_details", receiver);
                requestDetails.put("request_date", doc.getString("request_date"));
                requestDetails.put("date_time", doc.getLong("date_time"));
                requestDetails.put("contact", doc.getString("contact"));
                response.put("client_details", requestDetails);
            }


            jobs.add(response);
        }


        return Json.toJson(Json.toJson(jobs));

    }


    public JsonNode getPackgeDetail(String id) {
        Map<String, Object> response = new HashMap<>();
        ArrayList<Document> arrayResult = aggregateQueryDefault("client_request", "user_id", "user_id", "client_details", "_id", id, "response");
        for (Document document : arrayResult) {


            response.put("response_id", document.get("_id").toString());
            response.put("date_time", document.getLong("date_time"));
            response.put("user_id", document.getString("user_id"));
            response.put("current_city", document.getString("current_city"));
            response.put("contact", document.getString("contact"));
            response.put("request_date", document.getString("request_date"));
            response.put("client_request", document.getString("client_request"));
            response.put("courier_id", document.getString("courier_id"));
            response.put("last_name", document.getString("last_name"));
            response.put("request_id", document.getString("request_id"));
            response.put("first_name", document.getString("first_name"));
            response.put("status", document.getString("status"));
            response.put("courier_status", document.getString("courier_status"));
            response.put("comment", document.getString("comment"));
            List<Document> clientDetails = (List<Document>) document.get("client_details");
            for (Document doc : clientDetails) {
                Map<String, Object> requestDetails = new HashMap<>();
                requestDetails.put("client_request_id", doc.get("_id").toString());
                requestDetails.put("user_id", doc.getString("user_id"));
                requestDetails.put("first_name", doc.getString("first_name"));
                requestDetails.put("last_name", doc.getString("last_name"));
                List<Document> location = (List<Document>) doc.get("current_location");
                requestDetails.put("current_location", location);
                requestDetails.put("current_address", doc.getString("current_address"));
                requestDetails.put("current_city", doc.getString("current_city"));
                List<Document> items = (List<Document>) doc.get("delevery_items");
                requestDetails.put("delevery_items", items);
                List<Document> receiver = (List<Document>) doc.get("receiver_details");
                requestDetails.put("receiver_details", receiver);
                requestDetails.put("request_date", doc.getString("request_date"));
                requestDetails.put("date_time", doc.getLong("date_time"));
                requestDetails.put("contact", doc.getString("contact"));
                response.put("client_details", requestDetails);
            }


        }


        return (Json.toJson(response));


    }


    public JsonNode courierUpdateJob(String id, JsonNode update) {
        Clock clock = new Clock();
        Document transaction = new Document();
        Map<String, Object> courier = new HashMap<>();
        ArrayList<Object> history = new ArrayList<>();
        BasicDBObject query = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("courier_status", update.findPath("courier_status").textValue());
        newDocument.put("comment", update.findPath("comment").textValue());
        newDocument.put("status", "complete");
        query.put("_id", new ObjectId(id));
        Document jobDetails = aggregateQuery3("client_request", "client_request", "_id", "request_details", "_id", id, "response");
        // courier.put("user_id",jobDetails.getString("user_id"));
        courier.put("first_name", jobDetails.getString("first_name"));
        courier.put("last_name", jobDetails.getString("last_name"));
        courier.put("current_city", jobDetails.getString("current_city"));
        courier.put("contact", jobDetails.getString("contact"));
        //courier.put("request_date",jobDetails.getString("request_date"));
        //courier.put("client_request",jobDetails.getString("client_request"));
        courier.put("courier_id", jobDetails.getString("courier_id"));
        courier.put("status", jobDetails.getString("status"));
        courier.put("app_type", update.findPath("app_type").textValue());
        //courier.put("job_id", id);
        courier.put("tx_type", update.findPath("tx_type").textValue());
        courier.put("date_time", new Date().getTime());
        courier.put("date", clock.getCurrentDate());
        history.add(courier);
        transaction.put("client_request", jobDetails.get("request_details"));
        transaction.put("qr_id", id);
        transaction.put("couriers", history);

        insertDoc(transaction, TRANSACTION);
        return (updateDoc(RESPONSE, query, newDocument));
    }


    public JsonNode jobTransfer(String id, JsonNode courier) {
        BasicDBObject query = new BasicDBObject();
        Clock clock = new Clock();
        Document newCourier = new Document();
        Document previousCourier = new Document();
        Document updatedJob = new Document();
        Document updatedPreviousJob = new Document();
        String qrID;
        query.put("qr_id", id);

        Document job = getQueryOneDoc(TRANSACTION, query);
        List<Document> clientRequest = (List<Document>) job.get("client_request");
        List<Document> courierList = (List<Document>) job.get("couriers");
        //update previous courier
        qrID = job.getString("qr_id");
        for (Document courierDoc : courierList) {

            previousCourier.put("first_name", courierDoc.getString("first_name"));
            previousCourier.put("last_name", courierDoc.getString("last_name"));
            previousCourier.put("current_city", courierDoc.getString("current_city"));
            previousCourier.put("contact", courierDoc.getString("contact"));
            previousCourier.put("courier_id", courierDoc.getString("courier_id"));
            previousCourier.put("status", "transfer");
            previousCourier.put("app_type", courierDoc.getString("app_type"));
            previousCourier.put("tx_type", "out");
            previousCourier.put("date_time", new Date().getTime());
            previousCourier.put("date", clock.getCurrentDate());

        }
        courierList.add(previousCourier);
        updatedPreviousJob.put("couriers", courierList);
        updatedPreviousJob.put("qr_id", qrID);
        updatedPreviousJob.put("client_request", clientRequest);
        insertDoc(updatedPreviousJob, TRANSACTION);

        //new courier
        qrID = job.getString("qr_id");
        newCourier.put("first_name", courier.findPath("first_name").textValue());
        newCourier.put("last_name", courier.findPath("last_name").textValue());
        newCourier.put("current_city", courier.findPath("current_city").textValue());
        newCourier.put("contact", courier.findPath("contact").textValue());
        newCourier.put("courier_id", courier.findPath("courier_id").textValue());
        newCourier.put("status", courier.findPath("status").textValue());
        newCourier.put("app_type", courier.findPath("app_type").textValue());
        newCourier.put("tx_type", courier.findPath("tx_type").textValue());
        newCourier.put("date_time", courier.findPath("date_time").asLong());
        newCourier.put("date", courier.findPath("date").textValue());
        courierList.add(newCourier);
        updatedJob.put("client_request", clientRequest);
        updatedJob.put("qr_id", qrID);
        updatedJob.put("couriers", courierList);

        insertDoc(updatedJob, TRANSACTION);
        return Json.toJson(updatedJob);
    }


    public JsonNode getJobHistoryByQrId(String id) {
        Document jobHistory;
        Map<String, String> nullObject = new HashMap<>();
        nullObject.put("error_code", "err01");
        nullObject.put("message", "Couldn't find any record regarding this id");
        BasicDBObject query = new BasicDBObject();
        query.put("qr_id", id);
        jobHistory = getSingelDocuent(TRANSACTION, query);
        if (jobHistory == null) {
            return Json.toJson(nullObject);
        } else {
            return Json.toJson(jobHistory);
        }
    }

    /*
    @aggregate4
     db.getCollection("transaction").update(
    {"qr_id":"5ea6da941c2e9c1e11875551", "couriers.courier_id" : "5e9ff50ae0fd8c12e76377d9"},
         { "$set":
    {
      "couriers.$.status": "delete"
    }
     },
  {
       multi: true
   }
)

      */
    public JsonNode undoJobTranser(String qrID, JsonNode request) {
        String courierID = request.findPath("courier_id").textValue();
        BasicDBObject query = new BasicDBObject();
        Document doc = aggregate4(TRANSACTION, qrID, courierID);
        List<Document> couriers = (List<Document>) doc.get("couriers");
        query.put("qr_id", qrID);
        query.append("couriers.courier_id", courierID);

        BasicDBObject newDocument = new BasicDBObject();
        for (Document courier : couriers) {
            List<Document> fliteredCouriers = new ArrayList<>();
            if (courier.getString("courier_id").equals(courierID)) {
                System.out.println(courier);
                fliteredCouriers.add(courier);
                newDocument.put("couriers.$[].status", "undo");
                updateDocArray(TRANSACTION, query, newDocument);
            }


        }

        Map<String, String> r = new HashMap<>();

        return Json.toJson(r);
    }


    public JsonNode getClientHistory(String id) {
        BasicDBObject query = new BasicDBObject();
        query.put("user_id", id);
        ArrayList<Document> history = getQueryDoc(CLIENT_REQEST_COLLECTION, query);
        return Json.toJson(history);
    }


    public JsonNode getClientRequest(String id) {
        Map<String, Object> clientRequest = new HashMap<>();
        ArrayList<Object> resRequest = new ArrayList<>();
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));
        Document request = getQueryForSingelDoc(CLIENT_REQEST_COLLECTION, query);
        clientRequest.put("id", request.getObjectId("_id").toString());
        clientRequest.put("user_id", request.getString("user_id"));
        clientRequest.put("first_name", request.getString("first_name"));
        clientRequest.put("last_name", request.getString("last_name"));
        List<Document> location = (List<Document>) request.get("current_location");
        clientRequest.put("current_location", location);
        clientRequest.put("current_address", request.getString("current_address"));
        clientRequest.put("current_city", request.getString("current_city"));
        List<Document> items = (List<Document>) request.get("delevery_items");
        clientRequest.put("delevery_items", items);
        List<Document> receiver = (List<Document>) request.get("receiver_details");
        clientRequest.put("receiver_details", receiver);
        clientRequest.put("request_date", request.getString("request_date"));
        clientRequest.put("date_time", request.getLong("date_time"));
        clientRequest.put("contact", request.getString("contact"));
        resRequest.add(clientRequest);
        return Json.toJson(resRequest);
    }


}
