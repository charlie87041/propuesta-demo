FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

ARG GRADLE_VERSION=8.7

# Remove Yarn apt repo (breaks apt-get update due to missing GPG key)
RUN rm -f /etc/apt/sources.list.d/yarn.list /etc/apt/sources.list.d/yarn*.list || true

RUN apt-get update && apt-get install -y --no-install-recommends \
    wget unzip ca-certificates gh \
  && rm -rf /var/lib/apt/lists/*

RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O /tmp/gradle.zip \
  && unzip -q /tmp/gradle.zip -d /opt/gradle \
  && ln -sf /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle \
  && rm /tmp/gradle.zip

# Fix Java symlink issue
RUN mkdir -p /usr/local/sdkman/candidates/java/ \
    && ln -sf /usr/lib/jvm/msopenjdk-21 /usr/local/sdkman/candidates/java/current

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/msopenjdk-current
ENV PATH=$JAVA_HOME/bin:$PATH

# Add to bashrc for persistence
RUN echo "export JAVA_HOME=/usr/lib/jvm/msopenjdk-current" >> /etc/profile.d/java.sh \
    && echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile.d/java.sh \
    && chmod +x /etc/profile.d/java.sh  
