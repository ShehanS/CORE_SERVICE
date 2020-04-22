package JobManagers;

import com.fasterxml.jackson.databind.JsonNode;
import model.Response;
import org.bson.types.ObjectId;
import play.libs.Json;
import users.TaskDAO;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Request {
    private int requestID=0;
    private TaskDAO taskDAO;
    private ArrayList<Response> responseList;
    @Inject
    public Request(TaskDAO taskDAO) {
     this.taskDAO = taskDAO;
    }


    public JsonNode ManageRequest(JsonNode request){
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> responseMessage = new HashMap<>();



        requestID = requestID +1;
        JsonNode saveRequest = taskDAO.clientRequest(request);
        JsonNode courierInfo = taskDAO.findLocationBaseCourier(request.findPath("current_city").textValue());
        response.put("request_id", String.valueOf(requestID));
        response.put("client_request", saveRequest.findPath("id").textValue());
        response.put("request_date", request.findPath("request_date").textValue());
        response.put("user_id", request.findPath("user_id").textValue());
        response.put("date_time", request.findPath("date_time").asLong());
        response.put("courier_id", "");
        response.put("first_name", "");
        response.put("last_name", "");
        response.put("current_city", "");
        response.put("contact", "");
        response.put("status", "Pending");

        JsonNode saveResponse = taskDAO.addResponse(Json.toJson(response));
       if (saveResponse.findPath("status").textValue()=="success") {
         responseMessage.put("message","Request complected");
         responseMessage.put("request_id", saveResponse.findPath("id").textValue());
         responseMessage.put("status", saveResponse.findPath("status").textValue());
         return Json.toJson(responseMessage);
       }else{
           responseMessage.put("message","Request not complected");
           responseMessage.put("status", "failed");
           return Json.toJson(responseMessage);
       }

    }


    public JsonNode responseStatus(JsonNode request){
        String RequestID = request.findPath("id").textValue();
        JsonNode status = taskDAO.getRequestUpdate(RequestID);
        RequestProcess();

        return status;
    }


    private ArrayList<Response> getAllRequest(){
        responseList  = taskDAO.getCurrentRequestQue();
        return responseList;
    }

    public void RequestProcess() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            getAllRequest();
            List<Response> pendingList= responseList
                    .stream()
                    .filter(responseList -> responseList.getStatus().equals("Pending"))
                    .collect(Collectors.toList());
            

            }
        };
        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);


    }


    private void selectCouire(Response response){
         System.out.println(Json.toJson(response));
    }









}
