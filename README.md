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
- gRpc

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
In order to do that, select launch instance and select <code>Amazon Linux 2 AMI (HVM), SSD Volume Type - ami-03ab7423a204da002 (64-bit x86) / ami-0fb4cfafeead46a44 (64-bit Arm)</code>
Select <code>64-bit (x86)</code> and then <code>t2.micro</code>.
Make sure that SSH is enabled under security groups <code>SSH TCP 22 0.0.0.0/0 </code> and add your keypair when asked.
Now you should be able to login into your EC2 instance via SSH

In order to do so, run the command

```shell
ssh -i "linux.pem" ec2-user@ec2-54-241-68-63.us-west-1.compute.amazonaws.com
```

Where <code>linux.pem</code> is the name of your key and <code>ec2-user@ec2-54-241-68-63.us-west-1.compute.amazonaws.com</code> is the address of your instance.

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

We are all set and ready to run our logGenerator

```shell
cd logGenerator/
sbt clean compile
sbt run
```

# lambda_functions

## Run locally

Before running our APIs locally we have to install Docker

Docker is a required dependency from Amazon AWS SAM-Cli

```shell
sbt clean assembly && sam local start-api
```

## Testing locally

## CheckLogPresence function

REST request
```shell
curl  http://127.0.0.1:3000/checkLogPresence/01:10:40.134/00:00:03.000
```
Response
```shell
"GET /checkLogPresence/01:10:40.134/00:00:03.000 HTTP/1.1" 200 
true
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

Add a new policy into the lambda function just created
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



