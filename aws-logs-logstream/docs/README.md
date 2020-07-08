# AWS::Logs::LogStream

Resource Type definition for AWS::Logs::LogStream

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Logs::LogStream",
    "Properties" : {
        "<a href="#loggroupname" title="LogGroupName">LogGroupName</a>" : <i>String</i>,
        "<a href="#logstreamname" title="LogStreamName">LogStreamName</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Logs::LogStream
Properties:
    <a href="#loggroupname" title="LogGroupName">LogGroupName</a>: <i>String</i>
    <a href="#logstreamname" title="LogStreamName">LogStreamName</a>: <i>String</i>
</pre>

## Properties

#### LogGroupName

The name of the log group where the log stream is created.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>512</code>

_Pattern_: <code>^[\.\-_/#A-Za-z0-9]+$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### LogStreamName

The name of the log stream. The name must be unique within the log group.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>512</code>

_Pattern_: <code>^[^:*]*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

The name of the log stream.

