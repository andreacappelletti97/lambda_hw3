# Homework 3
### The goal of this homework is for students to gain experience with solving a distributed computational problem using cloud computing technologies by designing and implementing a RESTful service and a lambda function that are accessed from clients using gRPC.
### Grade: 8%

## Author
Andrea Cappelletti  
UIN: 674197701   
acappe2@uic.edu

This repository is organized into three different subprojects.

- logGenerator
- lambda_functions
- Akka gRpc

The following sections describe the functionalities implemented in all of them.

# logGenerator

The first project, logGenerator, provides an extension of the log generator coded by Professor Mark.

The goal of this extension is to periodically upload the logs into a S3 bucket on AWS S3.
In order to do so, an async thread is started at the beginning of the generation.

The thread takes all the logs into the directory <code>/log</code> and upload them.

We can modify the timePeriod and all the parameters from the <code>application.conf</code> file.

Please refer to that file in order to discover more information about the overall functionalities and change the parameters accordling to your needs.

To run the logGenerator on a EC2 instance follow the steps described below.

First thing first, log into your AWS console and start a Linux EC2 instance.

In order to do that, select launch instance and select

<code>Amazon Linux 2 AMI (HVM), SSD Volume Type - ami-03ab7423a204da002 (64-bit x86) / ami-0fb4cfafeead46a44 (64-bit Arm)</code>

Select <code>64-bit (x86)</code> and then <code>t2.micro</code>.

Make sure that SSH is enabled under security groups <code>SSH TCP 22 0.0.0.0/0 </code> and add your keypair when asked.

Now you should be able to login into your EC2 instance via SSH

In order to do so, run the command

```shell
ssh -i "linux.pem" ec2-user@ec2-54-241-68-63.us-west-1.compute.amazonaws.com
```

Where <code>linux.pem</code> is the name of your key and

<code>ec2-user@ec2-54-241-68-63.us-west-1.compute.amazonaws.com</code>

is the address of your instance.

Once you log in into your instance, in order to run the logGenerator you have to install
- Java SDK 8
- Scala
- SBT

### Install Java

To install Java, run the following command

```shell
sudo yum install java-1.8.0-openjdk
```
You may encounter the following error while running yum
```shell
File contains no section headers. file: file:///etc/yum.repos.d/bintray-sbt-rpm.repo
```
If you encounter that error, run
Solution
```shell
rm /etc/yum.repos.d/bintray-sbt-rpm.repo
```
In order to solve it, the go ahed and install Scala and SBT

### Install Scala

```shell
wget http://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.rpm
yum install scala-2.11.8.rpm
```
### Install sbt
```shell
curl -L https://www.scala-sbt.org/sbt-rpm.repo > sbt-rpm.repo
sudo mv sbt-rpm.repo /etc/yum.repos.d/
sudo yum install sbt
```

Now that we installed all the requirements to run the logGenerator, we have to configure our Amazon AWS credentials in order to access our Amazon account from the SDK.

### Configure AWS credentials

From terminal run
```shell
aws configure
```
Configure the credentials accordling to your IAM roles
```shell
AWS Access Key ID [None]: Your key
AWS Secret Access Key [None]: Your key
Default region name [None]: us-west-1
Default output format [None]: json
```
Please refer to: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-getting-started-set-up-credentials.html

Your IAM user should have access to the following permissions
- AWSCloudFormationFullAccess

- IAMFullAccess

- AWSLambda_FullAccess

- AmazonAPIGatewayAdministrator

- AmazonS3FullAccess

- AmazonEC2ContainerRegistryFullAccess

Refer to https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/sam-permissions.html

Moreover, you should generate a policy for your user in order to access and upload files on your
S3 bucket.

The policy will look like the following and in resource you should insert your S3 bucket ARN.
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ExampleStmt",
      "Action": [
        "s3:PutObject"
      ],
      "Effect": "Allow",
      "Resource": [
        "YOUR_S3_BUCKET_ARN/*"
      ]
    }
  ]
}
```

Attach the policy to your user.

We are all set and ready to run our logGenerator

```shell
cd logGenerator/
sbt clean compile
sbt run
```
## YouTube Video

A video explanation is available at this url: https://youtu.be/UY3w5uYv1BY

# lambda_functions

YouTube video: 

This project contains the lambda functions.

It uses SAM stacks to deploy them on CloudFormation.

In order to test them locally you may want to install Docket and setup the container.

The API specification is written into the file <code>template.yml</code> in order to improve the scalability
and maintainability over the time.

There is an API Gateway Proxy that caputes the request and handle them base on their nature (GET/POST...).

In this scenario I provide two endpoints, the first check the log timestamp and delta existance, the second
once return the log messages in MD5 format if they do exist.

In order to search into the log files, I use a binary search strategy, because they are ordered.

In this way the results are computed with a time complexity of O(logN).

The following sections explain how to test them locally and how to deploy them online and test them.

The lambda functions are online and available at this address.

<code>https://i89ssvhs3d.execute-api.us-west-1.amazonaws.com/Prod </code>

To make requests follow the sections below.
## Run locally

Install SAM-CLI

Follow the official tutorial based on your computer OS

https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html

Install and configure AWS-CLI as explained in the logGenerator section.

Before running our APIs locally we have to install Docker

https://docs.docker.com/get-docker/

Docker is a required dependency from Amazon AWS SAM-Cli

Once you have set up the entire system, you can now run the API locally

From the project root type

```shell
sbt clean assembly && sam local start-api
```

You will get all the endpoints

```shell
[success] Total time: 30 s, completed Nov 2, 2021 12:53:27 PM
Mounting CheckLogPresencePostFunction at http://127.0.0.1:3000/checkLogPresence [POST]
Mounting GetLogMessagesPostFunction at http://127.0.0.1:3000/getLogMessages [POST]
Mounting CheckLogPresenceFunction at http://127.0.0.1:3000/checkLogPresence/{time}/{delta} [GET]
Mounting GetLogMessagesFunction at http://127.0.0.1:3000/getLogMessages/{time}/{delta} [GET]
You can now browse to the above endpoints to invoke your functions. You do not need to restart/reload SAM CLI while working on your functions, changes will be reflected instantly/automatically. You only need to restart SAM CLI if you update your AWS SAM template
2021-11-02 12:53:29  * Running on http://127.0.0.1:3000/ (Press CTRL+C to quit)
```

Now we can test our APIs locally
## Testing locally



## CheckLogPresencePostFunction
REST request
```shell
curl -d "time=01:10:23.342&delta=00:00:02.000" -H  "Content-Type: application/x-www-form-urlencoded"   -X POST  http://127.0.0.1:3000/checkLogPresence
```

Response
```shell
"POST /checkLogPresence HTTP/1.1" 200  
{"found":true}
```

## GetLogMessagesPostFunction
REST request
```shell
curl -d "time=01:10:23.342&delta=00:00:02.000" -H  "Content-Type: application/x-www-form-urlencoded"   -X POST  http://127.0.0.1:3000/getLogMessage
```

Response
```shell
 "POST /getLogMessages HTTP/1.1" 200  
{"found":true,"messages":["bb7dfc4aa6ae90646b7b94646252cf4"]}
```


## CheckLogPresence function

REST request
```shell
curl  http://127.0.0.1:3000/checkLogPresence/01:10:23.342/00:00:02.000
```
Response
```shell
"GET /checkLogPresence/01:10:23.342/00:00:02.000 HTTP/1.1" 200
{"found":true}
```

## GetLogMessages function

REST request
```shell
curl  http://127.0.0.1:3000/getLogMessages/15:55:27.596/00:01:00.000
```
Response
```shell
"GET /getLogMessages/15:55:27.596/00:01:00.000 HTTP/1.1" 200 - 
e7b222fdba96b61f7c8acabaf531a8ca
```

## Deploy in production on Amazon AWS


```shell
sbt clean assembly && sam deploy --guided
```
Follow the current configuration
```shell
Configuring SAM deploy
======================

        Looking for config file [samconfig.toml] :  Found
        Reading default arguments  :  Success

        Setting default arguments for 'sam deploy'
        =========================================
        Stack Name [sam-app]: 
        AWS Region [us-west-1]: 
        #Shows you resources changes to be deployed and require a 'Y' to initiate deploy
        Confirm changes before deploy [y/N]: 
        #SAM needs permission to be able to create roles to connect to the resources in your template
        Allow SAM CLI IAM role creation [Y/n]:  
        CheckLogPresenceFunction may not have authorization defined, Is this okay? [y/N]: Y
        GetLogMessagesFunction may not have authorization defined, Is this okay? [y/N]: Y
        GetLogMessagesPostFunction may not have authorization defined, Is this okay? [y/N]: Y
        CheckLogPresencePostFunction may not have authorization defined, Is this okay? [y/N]: Y
        Save arguments to configuration file [Y/n]: 
        SAM configuration file [samconfig.toml]: 
        SAM configuration environment [default]: 

```
Once deployed we have to add a new policy into the lambda function just created.

The policy should allow the access to your S3 bucket

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ExampleStmt",
      "Action": [
        "s3:GetObject"
      ],
      "Effect": "Allow",
      "Resource": [
        "arn:aws:s3:::AWSDOC-EXAMPLE-BUCKET/*"
      ]
    }
  ]
}
```

In the YouTube video I detailed explain how to do that.

You can test the same lambda functions API using the curl commands in the previous section and
with the endpoint 

<code>https://i89ssvhs3d.execute-api.us-west-1.amazonaws.com/Prod </code>

I coded a bash script in order to execute all the API requests and test them.

The code is in the lambda_functions project directory, in order to execute it run

```shell
chmod +x testApi.sh
./testApi.sh
```

The code is the following

```bash
#Define base API URL
BASE_URL="https://i89ssvhs3d.execute-api.us-west-1.amazonaws.com/Prod"

#Define endpoints
ENDPOINT1="getLogMessages"
ENDPOINT2="checkLogPresence"

#Define params
TIME="01:10:23.342"
DELTA="00:00:02.000"

#Run CURL to test the endpoints
printf "Running ${ENDPOINT1} with GET request\n"
curl  $BASE_URL/$ENDPOINT1/$TIME/$DELTA
printf "\nRunning ${ENDPOINT2} with GET request\n"
curl  $BASE_URL/$ENDPOINT2/$TIME/$DELTA
printf "\nRunning ${ENDPOINT1} with POST request\n"
curl -d "time=${TIME}&delta=${DELTA}" -H  "Content-Type: application/x-www-form-urlencoded"   -X POST  $BASE_URL/$ENDPOINT1
printf "\nRunning ${ENDPOINT2} with POST request\n"
curl -d "time=${TIME}&delta=${DELTA}" -H  "Content-Type: application/x-www-form-urlencoded"   -X POST  $BASE_URL/$ENDPOINT2

```


# Akka gRpc



Compile the project

```shell
sbt clean compile
```

## Start the gRPC server

```shell
sbt "runMain com.lambda.grpc.Server"
```

Response


```shell
[2021-11-01 18:18:13,188] [INFO] [akka.event.slf4j.Slf4jLogger] [Server-akka.actor.default-dispatcher-3] [] - Slf4jLogger started
(gRPC server bound to {}:{},127.0.0.1,8080)
```

## Start the gRPC client and perform the request

```shell
sbt "runMain com.lambda.grpc.Client"
```

Response

```shell
Performing request: 01:10:40.134 and 00:00:03.000
LogMessageReply({"found":true},UnknownFieldSet(Map()))
```
