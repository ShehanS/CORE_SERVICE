package controllers;

import JWT.JWTUtils;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import users.TaskDAO;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebApplicationController extends Controller {
    private TaskDAO taskDAO;
    private JWTUtils jwtUtils;

    @Inject
    public WebApplicationController(TaskDAO taskDAO, JWTUtils jwtUtils) {
        this.taskDAO = taskDAO;
        this.jwtUtils = jwtUtils;
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


    public Result getUserAndCourierDetails(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.getUserAndQueryDetails(id));
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
