package JWT;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {
    private final static String secret = "fj32Jfv02Mq33g0f8ioDkw";
    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    public String genarateJWT(JsonNode UserLogin){
        JsonNode json = UserLogin;
        String result ="";

        try{
            long nowMillis = System.currentTimeMillis();
            Date currentTime = new Date(nowMillis);
            result =  JWT.create()
                    .withIssuer("auth0")
                    .withIssuedAt(currentTime)
                    .withClaim("username",json.findPath("username").textValue())
                    .withClaim("password", json.findPath("password").textValue())
                    .sign(Algorithm.HMAC256(secret));

            logger.info(result);

        } catch (JWTCreationException exception){
            throw new RuntimeException("You need to enable Algorithm.HMAC256");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());

        }
        return result;
    }
    public boolean validateJWT(String token){

        Map<String, Object> error = new HashMap<>();
        try{
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            Map<String, String> user = new HashMap<>();
            user.put("username",jwt.getClaim("username").asString());
            user.put("password",jwt.getClaim("password").asString());
            return true;
        } catch (JWTDecodeException exception){
            error.put("status","unauthorized-user");
            logger.error(exception.toString());
            return false;
        } catch (UnsupportedEncodingException e) {
            error.put("status","unauthorized-user");
            logger.error(e.toString());
            return false;
        }

    }


}
