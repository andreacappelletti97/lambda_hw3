# Homework 3


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



