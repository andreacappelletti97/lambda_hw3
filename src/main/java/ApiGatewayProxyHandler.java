package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Java entrypoint for the API Gateway Lambda function
 */
public class ApiGatewayProxyHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    /**
     * Handle a Lambda request via the API Gateway
     * @param requestEvent the HTTP request
     * @param context info about this lambda function & invocation
     * @return an HTTP response
     */
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent
                                                              requestEvent, Context context) {

        //Detect gRPC request from the content type of the headers
           /*
            if (requestEvent.getHeaders().get("Content-Type").equals("application/grpc+proto")) {
                System.out.println("hey there!!!");
                return null;
            }
            */

        if (requestEvent.getPath().contains("checkLogPresence")) {
            System.out.println("Check log presence function is called");
            CheckLogPresenceFunction.Response response = CheckLogPresenceFunction.handle(requestEvent, context);
            return new APIGatewayProxyResponseEvent()
                    .withBody(response.body())
                    .withStatusCode(response.statusCode())
                    .withHeaders(response.javaHeaders());
        }

        Map<String, String> headersMap  = new HashMap<String, String>() {{
            put("Content-Type", "text/plain");
        }};
        return new APIGatewayProxyResponseEvent().withBody("request error").withStatusCode(400).withHeaders(headersMap);

    }
}
