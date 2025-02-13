#!/usr/bin/env bash
set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_REGION=eu-west-2
export PAGER=

aws configure set region $AWS_REGION
aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
# Create the bucket
aws s3 --endpoint-url=http://localhost:4566 --region eu-west-2 ls s3://testbucket || aws --endpoint-url=http://localhost:4566 --region=eu-west-2 s3 mb s3://testbucket

echo "S3 Configured"