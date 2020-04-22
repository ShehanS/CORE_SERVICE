package JobManagers;

import com.fasterxml.jackson.databind.JsonNode;

import model.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import play.libs.Json;
import users.TaskDAO;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Shehan Shalinda. This is my final project backed service.
 * 2020/04/01
 */

public class RequestProcess {
    private static final Logger log = LogManager.getLogger(TaskDAO.class);
    private int requestID = 0;
    private TaskDAO taskDAO;
    private ArrayList<Response> responseList;
    private ArrayList<JsonNode> assigenedList;
    private Map<String, String> message;
    private JsonNode assignCourier;
    private ArrayList<JsonNode> couriers;

    @Inject
    public RequestProcess(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }


    public JsonNode manageRequest(JsonNode request) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> responseMessage = new HashMap<>();
        requestID = requestID + 1;
        JsonNode saveRequest = taskDAO.clientRequest(request);
        response.put("request_id", String.valueOf(requestID));
        response.put("client_request", saveRequest.findPath("id").textValue());
        response.put("request_date", request.findPath("request_date").textValue());
        response.put("user_id", request.findPath("user_id").textValue());
        response.put("date_time", request.findPath("date_time").asLong());
        response.put("courier_id", "");
        response.put("first_name", "");
        response.put("last_name", "");
        response.put("current_city", request.findPath("current_city").textValue());
        response.put("contact", "");
        response.put("status", "pending");
        response.put("courier_status", "pending");//cancel, collect, pending default value is pending
        response.put("comment", "");              //special comments for if courier cancel jobs
        JsonNode saveResponse = taskDAO.addResponse(Json.toJson(response));

        if (saveResponse.findPath("status").textValue() == "success") {
            responseMessage.put("message", "RequestProcess complected");
            responseMessage.put("request_id", saveResponse.findPath("id").textValue());
            responseMessage.put("status", saveResponse.findPath("status").textValue());
            return Json.toJson(responseMessage);
        } else {
            responseMessage.put("message", "RequestProcess not complected");
            responseMessage.put("status", "failed");
            return Json.toJson(responseMessage);
        }

    }


    public JsonNode requestStatus(String id) {
        Map<String, String> response = new HashMap<>();
        try {

            JsonNode assigner = taskDAO.getRequestUpdate(id);

            if ((assigner.findPath("status").textValue().equals("working") || (assigner.findPath("status").textValue().equals("complete")))) {
                response.put("args1", assigner.findPath("first_name").textValue());
                response.put("args2", assigner.findPath("last_name").textValue());
                response.put("args3", assigner.findPath("contact").textValue());
                response.put("args4", assigner.findPath("save_id").textValue());
                response.put("args5", assigner.findPath("status").textValue());
                response.put("args6", "message");
                return Json.toJson(response);
            } else if (assigner.findPath("status").textValue().equals("pending")) {
                response.put("args1", "Couriers are not available in your area.");
                response.put("args5", "pending");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Cannot proceed now");
            errorResponse.put("code", e.getMessage());
            return Json.toJson(errorResponse);
        } finally {

            return Json.toJson(response);
        }
    }


    private ArrayList<Response> getAllRequest() {
        responseList = taskDAO.getCurrentRequestQue();
        return responseList;
    }


    public void requestProcess() {
        log.info("Runnable process start: Request process\n");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getAllRequest();
                List<Response> pendingList = responseList
                        .stream()
                        .filter(responseList -> responseList.getStatus().equals("pending"))
                        .collect(Collectors.toList());
                for (Response response : pendingList) {
                    log.info("Passing for findCourierResponse : " + Json.toJson(response) + "\n");
                    findCourierResponse(response);

                }

            }
        };
        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
    }


    private void findCourierResponse(Response response) {
        log.info("{{method-findCourierResponse -response : " + Json.toJson(response) + "}}");
        if (taskDAO.findLocationBaseCourier(response.getCurrent_city()) == null) {
            log.info("Cannot find courier");
        } else {
            couriers = taskDAO.findLocationBaseCourier(response.getCurrent_city());
            List<JsonNode> availableList = couriers.stream()
                    .filter(c -> c.findPath("status").textValue().equals("working"))
                    .filter(c -> c.findPath("job_running").booleanValue() == false)
                    .collect(Collectors.toList());
            for (JsonNode courier : availableList) {
                //updating
                log.info("Updating response db");
                taskDAO.updateResponse(courier.findPath("current_city").textValue(), courier);
            }

        }

    }


}
