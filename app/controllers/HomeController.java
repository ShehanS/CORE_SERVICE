package controllers;
import JWT.JWTUtils;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.*;
import users.TaskDAO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.rmi.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Singleton
public class HomeController extends Controller {

private TaskDAO taskDAO;
private JWTUtils jwtUtils;

    @Inject
    public HomeController(TaskDAO taskDAO, JWTUtils jwtUtils) {
        this.taskDAO = taskDAO;
        this.jwtUtils = jwtUtils;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result login(Http.Request request) {
        JsonNode userLogin = request.body().asJson();
        String JWTToken = jwtUtils.genarateJWT(userLogin);
        return ok(JWTToken);
    }

    public Result index(Http.Request request) {
        Map<String, String> res = new HashMap<>();
        res.put("status","failed");
        res.put("message","authorization-failed");
        if (verfyJWTAccess(request)==true) {
            return ok("OK");
        }
        return ok(Json.toJson(res));
    }

    public Result createUser(Http.Request request){
        Map<String, String> res = new HashMap<>();
        res.put("status","failed");
        res.put("message","authorization-failed");
        if (verfyJWTAccess(request)==true) {
            JsonNode user = request.body().asJson();
            return ok(taskDAO.createSystemUser(user));
        }

        return ok(Json.toJson(res));
    }


    public Result getAllUsers(Http.Request request){
        Map<String, String> res = new HashMap<>();
        res.put("status","failed");
        res.put("message","authorization-failed");
        if (verfyJWTAccess(request)==true) {
            return ok(taskDAO.getAllUsers());
        }

        return ok(Json.toJson(res));
    }



    public Result delete(Http.Request request){
        Map<String, String> res = new HashMap<>();
        res.put("status","failed");
        res.put("message","authorization-failed");
        if (verfyJWTAccess(request)==true) {
            JsonNode deleteUser = request.body().asJson();
            String userID = deleteUser.path("id").asText();
            return ok(taskDAO.RemoveUser(userID));
        }

        return ok(Json.toJson(res));

    }

    public Result update(Http.Request request){
        Map<String, String> res = new HashMap<>();
        res.put("status","failed");
        res.put("message","authorization-failed");
        if (verfyJWTAccess(request)==true) {
            JsonNode updateUser = request.body().asJson();
            String userID = updateUser.path("id").asText();
            return ok(taskDAO.Update(userID));
        }
        return ok(Json.toJson(res));
    }


    public boolean verfyJWTAccess(Http.Request request){
        boolean auth =false;
        Optional verifier = request.getHeaders().get("Access-Token");
        Map<String,Object> obj = new HashMap<>();
        obj.put("access_token",Json.toJson(verifier));
        JsonNode accessToken = Json.toJson(obj);
        if (accessToken == null) {
            return auth;
        }
        String token = accessToken.findPath("access_token").textValue();
        if (token==null){
        return auth;
        }
        auth = (jwtUtils.validateJWT(token));
        return auth;
    }

}
