package software.amazon.logs.logstream;

import com.google.common.collect.Lists;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static CreateLogStreamRequest translateToCreateRequest(final ResourceModel model) {
        return CreateLogStreamRequest.builder()
                .logGroupName(model.getLogGroupName())
                .logStreamName(model.getLogStreamName())
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeLogStreamsRequest translateToReadRequest(final ResourceModel model) {
        return DescribeLogStreamsRequest.builder()
                .logGroupName(model.getLogGroupName())
                .logStreamNamePrefix(model.getLogStreamName())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param response the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeLogStreamsResponse response, final ResourceModel model) {
        if (response.logStreams().size() > 0) {
            // we call DescribeLogStreams using the log stream name as a prefix & receive a list sorted by StreamName,
            // so a perfect match (if present) must be the first item
            if (Objects.equals(model.getLogStreamName(), response.logStreams().get(0).logStreamName())) {
                return ResourceModel.builder()
                        .logGroupName(model.getLogGroupName())
                        .logStreamName(response.logStreams().get(0).logStreamName())
                        .id(response.logStreams().get(0).arn())
                        .build();
            }
        }
        throw new CfnNotFoundException("AWS::Logs::LogStream", model.getId());
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteLogStreamRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteLogStreamRequest.builder()
                .logGroupName(model.getLogGroupName())
                .logStreamName(model.getLogStreamName())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static DescribeLogStreamsRequest translateToListRequest(final String nextToken, final ResourceModel model) {
        return DescribeLogStreamsRequest.builder()
                .logGroupName(model.getLogGroupName())
                .nextToken(nextToken)
                .limit(50)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param awsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final DescribeLogStreamsResponse awsResponse) {
        return streamOfOrEmpty(awsResponse.logStreams())
                .map(resource -> ResourceModel.builder()
                        .logStreamName(resource.logStreamName())
                        .id(resource.arn())
                        .build())
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    /**
     * Translates a resource model into an SDK object
     *
     * @param model the resource model
     * @return SDK model of resource
     */
    static LogStream translateModelToSDK(final ResourceModel model) {
        return LogStream.builder()
                .logStreamName(model.getLogStreamName())
                .arn(model.getId())
                .build();
    }
}
