package controllers;

import JWT.JWTUtils;
import analytic.Analytic;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.Document;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import users.TaskDAO;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import JobManagers.RequestProcess;

public class WebApplicationController extends Controller {
    private TaskDAO taskDAO;
    private JWTUtils jwtUtils;
    private Analytic analytic;


    @Inject
    public WebApplicationController(TaskDAO taskDAO, JWTUtils jwtUtils, Analytic analytic) {
        this.taskDAO = taskDAO;
        this.jwtUtils = jwtUtils;
        this.analytic = analytic;
    }

    public Result getAllJobs(Http.Request request) {
        JsonNode appRequest = request.body().asJson();
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request) == true) {
            return ok(Json.toJson(taskDAO.getCurrentRequestQueDateWise(appRequest)));
        }
        return ok(Json.toJson(res));
    }


    public Result getAllPendingJobs(Http.Request request) {
        JsonNode appRequest = request.body().asJson();
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request) == true) {
            return ok(Json.toJson(taskDAO.getCurrentPendingRequestQueDateWise(appRequest)));
        }
        return ok(Json.toJson(res));
    }


    public Result getRequestUserWise(Http.Request request) {
        JsonNode appRequest = request.body().asJson();
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request) == true) {
            return ok(Json.toJson(taskDAO.getCurrentRequestQueDateWise(appRequest)));
        }
        return ok(Json.toJson(res));
    }


    public Result getAllCouriers() {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.getAllCouriers());
        }
        return ok(Json.toJson(res));
    }


    public Result getUserAndCourierDetails(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.getUserAndQueryDetails(id));
        }
        return ok(Json.toJson(res));
    }


    public Result getClientRequest(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.getClientRequest(id));
        }
        return ok(Json.toJson(res));
    }


    public Result assignCourierByManual(Http.Request request) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request) == true) {
            JsonNode req = request.body().asJson();
            String courierID = req.findPath("courier_id").textValue();
            String responseID = req.findPath("response_id").textValue();

            return ok(this.taskDAO.assignCourierById(courierID, responseID));
        }

        return ok(Json.toJson(res));
    }


    public Result getCourierStatus(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.checkCourierStatus(id));
        }
        return ok(Json.toJson(res));
    }


    public Result getJobStatus(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.gettingJobStatus(id));
        }
        return ok(Json.toJson(res));
    }


    public Result getInventoryStatus(Http.Request request) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(analytic.getInventoryDetails());
        }
        return ok(Json.toJson(res));
    }















    //jwt authorization and authentication
    public boolean verfyJWTAccess(Http.Request request) {
        boolean auth = false;
        Optional verifier = request.getHeaders().get("Access-Token");
        Map<String, Object> obj = new HashMap<>();
        obj.put("access_token", Json.toJson(verifier));
        JsonNode accessToken = Json.toJson(obj);
        if (accessToken == null) {
            return auth;
        }
        String token = accessToken.findPath("access_token").textValue();
        if (token == null) {
            return auth;
        }
        auth = (jwtUtils.validateJWT(token));
        return auth;
    }


}
