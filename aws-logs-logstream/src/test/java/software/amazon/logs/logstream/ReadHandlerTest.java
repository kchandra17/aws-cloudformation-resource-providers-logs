package software.amazon.logs.logstream;

import java.time.Duration;
import java.util.Collections;

import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = defaultModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(Collections.singleton(Translator.translateModelToSDK(model)))
                .build();
        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(describeResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        assertThat(response.getResourceModel().getLogGroupName()).isEqualTo(GROUP_NAME);
        assertThat(response.getResourceModel().getLogStreamName()).isEqualTo(STREAM_NAME);
    }

    @Test
    public void handleRequest_Success_MultipleStreamsFound() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = defaultModel();
        final ResourceModel model2 = newModel(GROUP_NAME, STREAM_NAME + "2");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(ImmutableList.of(Translator.translateModelToSDK(model), Translator.translateModelToSDK(model2)))
                .build();
        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(describeResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        assertThat(response.getResourceModel().getLogGroupName()).isEqualTo(GROUP_NAME);
        assertThat(response.getResourceModel().getLogStreamName()).isEqualTo(STREAM_NAME);
    }

    @Test
    public void handleRequest_Fail_LogGroupNotFound() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = defaultModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        } catch (final BaseHandlerException e) {
            assertThat(e.getClass()).isEqualTo(CfnNotFoundException.class);
        }
    }

    @Test
    public void handleRequest_Fail_LogStreamNotFound() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = defaultModel();
        final ResourceModel model2 = newModel(GROUP_NAME, STREAM_NAME + "2");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(ImmutableList.of(Translator.translateModelToSDK(model2)))
                .build();
        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(describeResponse);

        try {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        } catch (final BaseHandlerException e) {
            assertThat(e.getClass()).isEqualTo(CfnNotFoundException.class);
        }
    }
}
