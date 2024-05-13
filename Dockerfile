FROM opensearchproject/opensearch:latest

# Switch to root user
USER root

# Install AWS CLI
RUN yum update -y && \
    yum install -y aws-cli && \
    yum clean all

# Install OpenSearch S3 plugin
RUN bin/opensearch-plugin install --batch repository-s3

# Switch back to non-root user if desired
USER opensearch
