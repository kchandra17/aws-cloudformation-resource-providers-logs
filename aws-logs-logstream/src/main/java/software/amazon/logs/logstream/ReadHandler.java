package software.amazon.logs.logstream;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        return proxy.initiate("AWS-Logs-LogStream::Read", proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall((logStreamRequest, cwProxyClient) -> {
                try {
                    return cwProxyClient.injectCredentialsAndInvokeV2(logStreamRequest, cwProxyClient.client()::describeLogStreams);
                } catch (final ResourceNotFoundException e) {
                    throw new CfnNotFoundException(e);
                } catch (final InvalidParameterException e) {
                    throw new CfnInvalidRequestException(e);
                } catch (final ServiceUnavailableException e) {
                    throw new CfnServiceInternalErrorException(e);
		}
            })
            .done(response -> {
                final ResourceModel responseModel = Translator.translateFromReadResponse(response, model);
                return ProgressEvent.defaultSuccessHandler(responseModel);
            });
    }
}
