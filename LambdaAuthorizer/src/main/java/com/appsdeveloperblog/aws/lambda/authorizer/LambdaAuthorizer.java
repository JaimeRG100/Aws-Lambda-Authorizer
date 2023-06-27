package com.appsdeveloperblog.aws.lambda.authorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.appsdeveloperblog.aws.lambda.authorizer.model.AuthorizerOutput;
import com.appsdeveloperblog.aws.lambda.authorizer.model.PolicyDocument;
import com.appsdeveloperblog.aws.lambda.authorizer.model.Statement;

import java.util.Arrays;

/**
 * Will tell ApiGateway if the client's request should be allowed or denied
 */
public class LambdaAuthorizer implements RequestHandler<APIGatewayProxyRequestEvent, AuthorizerOutput> {

    @Override
    public AuthorizerOutput handleRequest(final APIGatewayProxyRequestEvent request, final Context context) {
        System.out.println("AuthorizerOutput.handleRequest() request:" + request);
        String userName = request.getPathParameters().get("userName");
        String effect = "Allow";

        if (userName.equalsIgnoreCase("123")) {
            effect = "Deny";
        }

        APIGatewayProxyRequestEvent.ProxyRequestContext proxyRequestContext = request.getRequestContext();

        String arn = String.format("arn:aws:execute-api:%s:%s:%s:/%s/%s/%s",
                System.getenv("AWS_REGION"),
                proxyRequestContext.getAccountId(),
                proxyRequestContext.getApiId(),
                proxyRequestContext.getStage(),
                proxyRequestContext.getHttpMethod(),
                "*");

        Statement statement = Statement.builder()
                .action("execute-api:Invoke")
                .effect(effect)
                .resource(arn)
                .build();

        PolicyDocument policyDocument = PolicyDocument.builder()
                .version("2012-10-17")
                .statements(Arrays.asList(statement))
                .build();

        AuthorizerOutput authorizerOutput = AuthorizerOutput.builder()
                .principalId(userName)
                .policyDocument(policyDocument)
                .build();

        System.out.println("AuthorizerOutput.handleRequest() authorizerOutput:" + authorizerOutput);
        return authorizerOutput;
    }

}

/*
{
  "principalId": "user",
  "policyDocument": {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Action": "execute-api:Invoke",
        "Effect": "Deny",
        "Resource": "arn:aws:execute-api:us-west-2:123456789012:ymy8tbxw7b/dev/GET/"
      }
    ]
  }
}
 */