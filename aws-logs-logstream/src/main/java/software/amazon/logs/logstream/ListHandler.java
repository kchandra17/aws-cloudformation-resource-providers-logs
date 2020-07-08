package software.amazon.logs.logstream;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel requestedModel = request.getDesiredResourceState();
        final String nextToken = request.getNextToken();
        return proxy.initiate("AWS-Logs-LogStream::List", proxyClient, requestedModel, callbackContext)
                .translateToServiceRequest(model -> Translator.translateToListRequest(nextToken, model))
                .makeServiceCall((logStreamRequest, cwProxyClient) -> {
                    try {
                        return cwProxyClient.injectCredentialsAndInvokeV2(logStreamRequest, cwProxyClient.client()::describeLogStreams);
                    } catch (final ResourceNotFoundException e) {
                        throw new CfnNotFoundException(e);
                    }
                })
                .done(response -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModels(Translator.translateFromListResponse(response))
                            .nextToken(response.nextToken())
                            .status(OperationStatus.SUCCESS)
                            .build()
                );
    }
}
