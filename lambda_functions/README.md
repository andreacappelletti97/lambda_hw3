# Homework 3

## Install Java

# remove old Bintray repo file
```shell
File contains no section headers. file: file:///etc/yum.repos.d/bintray-sbt-rpm.repo
```

Solution
```shell
rm /etc/yum.repos.d/bintray-sbt-rpm.repo
```
Install Java

```shell
sudo yum install java-1.8.0-openjdk
```
Install Scala

```shell
wget http://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.rpm
yum install scala-2.11.8.rpm
```
Install sbt
```shell
curl -L https://www.scala-sbt.org/sbt-rpm.repo > sbt-rpm.repo
sudo mv sbt-rpm.repo /etc/yum.repos.d/
sudo yum install sbt
```

Configure AWS credentials
```shell
aws configure
```
```shell
AWS Access Key ID [None]: Your key
AWS Secret Access Key [None]: Your key
Default region name [None]: us-west-1
Default output format [None]: json
```
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



