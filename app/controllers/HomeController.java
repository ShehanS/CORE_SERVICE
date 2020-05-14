package controllers;
import JWT.JWTUtils;
import JobManagers.RequestProcess;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.*;
import service.Clock;
import users.TaskDAO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import AESEncryptionDecryption.ASE;

/**
 *Created by Shehan Shalinda. This is my final project backed service.
 * 2019/12/10
 */
@Singleton
public class HomeController extends Controller {
private TaskDAO taskDAO;
private JWTUtils jwtUtils;
    final String secretKey = "citypack";
    private ASE ase;
    private RequestProcess requestProcess;
    private Clock clock;

    /**
     * Injected class
     *
     * @param requestProcess this class doing find courier each client it's bases on location.
     * @param taskDAO        this class is working as data interface. between ExternalAPIDAO and other class. Other class did't access databases directly.
     * @param jwtUtils       this class doing authorization. Generate and Validate,  JWT access token are provide each users when login
     * @param ase
     */

    @Inject
    public HomeController(Clock clock, RequestProcess requestProcess, TaskDAO taskDAO, JWTUtils jwtUtils, ASE ase) {
        this.taskDAO = taskDAO;
        this.jwtUtils = jwtUtils;
        this.ase = ase;
        this.requestProcess = requestProcess;
        this.clock = clock;

    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> requestProcess with a path of <code>/</code>.
     */

    /**
     *getAllUsers
     * @param request this request prams is comming from routes module,
     *                checking user and return access token and session
     *
     * @return
     */
    public Result login(Http.Request request) {
        ase.setKey(secretKey);
        JsonNode userLogin = request.body().asJson();
        JsonNode user = taskDAO.findLoginUser(userLogin);
        requestProcess.requestProcess();
        taskDAO.getCurrentRequestQue();

        return ok(user);

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

    //create a user
    public Result createUser(Http.Request request){
        Map<String, String> res = new HashMap<>();
        Map<String, String> existingUserMessge = new HashMap<>();
        existingUserMessge.put("status", "error");
        existingUserMessge.put("error-code", "404");
        existingUserMessge.put("message", "existing user found");
        res.put("status","failed");
        res.put("message","authorization-failed");

        JsonNode user = request.body().asJson();
        return ok(taskDAO.createUser(user));

    }

    //get all users
    public Result getAllUsers(Http.Request request){
        Map<String, String> res = new HashMap<>();
        res.put("status","failed");
        res.put("message","authorization-failed");
        if (verfyJWTAccess(request) == true) {
            return ok((Json.toJson(taskDAO.getAllUsers())));
        }
        return ok(Json.toJson(res));
    }

    public Result delete(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.RemoveUser(id));
        }
        return ok(Json.toJson(res));

    }

    //update user using by id
    public Result updateActivation(String id) {
        Map<String, String> res = new HashMap<>();
        JsonNode u = request().body().asJson();
        res.put("status","failed");
        res.put("message","authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.UpdateActivation(id, u));
        }
        return ok(Json.toJson(res));
    }

    //update user using by id
    public Result update(String id) {
        Map<String, String> res = new HashMap<>();
        JsonNode u = request().body().asJson();
        res.put("status","failed");
        res.put("message","authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.Update(id, u));
        }
        return ok(Json.toJson(res));
    }

    //jwt authorization and authentication
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


    //get all the requestProcess
    public Result getAllRequests(Http.Request request) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request) == true) {
            JsonNode clientRequest = request.body().asJson();
            return ok(taskDAO.getClientAllRequests(clientRequest));
        }

        return ok(Json.toJson(res));
    }

    //find User by id
    public Result selectUser(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.findUserById(id));
        }

        return ok(Json.toJson(res));
    }

    //attendance-mark
    public Result addAttendance(Http.Request request) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request) == true) {
            JsonNode attendance = request.body().asJson();
            return ok(taskDAO.addAttendance(attendance));
        }
        return ok(Json.toJson(res));
    }


    //get-attendance
    public Result getAttendance(Http.Request request) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request) == true) {
            JsonNode attendance = request.body().asJson();
            return ok(taskDAO.getEmployeeAttendance(attendance));
        }
        return ok(Json.toJson(res));
    }

    public Result getFactoryConfig(Http.Request request) {
        JsonNode appRequest = request.body().asJson();
        return ok(taskDAO.getFactoryConfig(appRequest.findPath("token").asText(), appRequest.findPath("config_type").asText(), appRequest.findPath("list_name").asText()));
    }

    public Result clientRequest(Http.Request request) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request) == true) {
            JsonNode clientRequest = request.body().asJson();
            return ok(this.requestProcess.manageRequest(clientRequest));
        }

        return ok(Json.toJson(res));
    }


    public Result requestStatus(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(this.requestProcess.requestStatus(id));

        }
        return ok(Json.toJson(res));
    }

    public Result updateProfile(String id) {
        Map<String, String> res = new HashMap<>();
        JsonNode updateStatus = request().body().asJson();
        res.put("status", "failed");
        res.put("message", "authorization-failed");

        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.updateProfileStatus(id, updateStatus));

        }
        return ok(Json.toJson(res));

    }


    public Result updateLocation(Http.Request request) {
        Map<String, String> res = new HashMap<>();
        JsonNode updateLocation = request.body().asJson();
        res.put("status", "failed");
        res.put("message", "authorization-failed");

        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.clientLocation(updateLocation));

        }
        return ok(Json.toJson(res));

    }


    public Result getCourierJobs(String id) {
        Map<String, String> res = new HashMap<>();
        JsonNode updateLocation = request().body().asJson();
        res.put("status", "failed");
        res.put("message", "authorization-failed");

        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.getUserAndQueryDetails(id));

        }
        return ok(Json.toJson(res));

    }


    public Result getJobsDetails(String id) {
        Map<String, String> res = new HashMap<>();

        res.put("status", "failed");
        res.put("message", "authorization-failed");

        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.getAllRequestDetails(id));

        }
        return ok(Json.toJson(res));

    }


    public Result getPackgeDetail(String id) {
        Map<String, String> res = new HashMap<>();

        res.put("status", "failed");
        res.put("message", "authorization-failed");

        if (verfyJWTAccess(request()) == true) {

            return ok(taskDAO.getPackgeDetail(id));

        }
        return ok(Json.toJson(res));

    }


    public Result courierUpdateJobs(String id) {
        Map<String, String> res = new HashMap<>();
        JsonNode update = request().body().asJson();
        res.put("status", "failed");
        res.put("message", "authorization-failed");

        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.courierUpdateJob(id, update));

        }
        return ok(Json.toJson(res));

    }


    public Result getJobHistory(String id) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        res.put("message", "authorization-failed");

        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.getJobHistoryByQrId(id));

        }
        return ok(Json.toJson(res));

    }


    public Result courierJobTransfer(String id) {
        Map<String, String> res = new HashMap<>();
        JsonNode update = request().body().asJson();
        res.put("status", "failed");
        res.put("message", "authorization-failed");

        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.jobTransfer(id, update));

        }
        return ok(Json.toJson(res));

    }


    public Result undoCourierJob(String qrID) {
        Map<String, String> res = new HashMap<>();
        JsonNode update = request().body().asJson();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.undoJobTranser(qrID, update));

        }
        return ok(Json.toJson(res));
    }


    public Result getClientHistory(String userID) {
        Map<String, String> res = new HashMap<>();
        JsonNode update = request().body().asJson();
        res.put("status", "failed");
        res.put("message", "authorization-failed");
        if (verfyJWTAccess(request()) == true) {
            return ok(taskDAO.getClientHistory(userID));

        }
        return ok(Json.toJson(res));
    }



}
