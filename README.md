# Homework 3


## Run locally

Before running our APIs locally we have to install Docker

Docker is a required dependency from Amazon AWS SAM-Cli

```
sbt clean assembly && sam local start-api
```
## Deploy in production on Amazon AWS

```
sbt clean assembly && sam deploy --guided
```

Add a new policy into the lambda function just created
The policy should allow the access to your S3 bucket

```
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


