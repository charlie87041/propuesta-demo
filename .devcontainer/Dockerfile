FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

ARG GRADLE_VERSION=8.7

# 1. Limpieza de repos y unificación de instalaciones (Java utils + Python)
RUN rm -f /etc/apt/sources.list.d/yarn.list /etc/apt/sources.list.d/yarn*.list || true \
    && apt-get update && apt-get install -y --no-install-recommends \
    wget unzip ca-certificates gh git python3-pip python3-venv wl-clipboard \
    && rm -rf /var/lib/apt/lists/*

# 2. Instalación de Gradle
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O /tmp/gradle.zip \
    && mkdir -p /opt/gradle \
    && unzip -q /tmp/gradle.zip -d /opt/gradle \
    && ln -sf /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle \
    && rm /tmp/gradle.zip

# 3. Fix Java symlink y variables de entorno
# Nota: Ajustado para usar la ruta que creas con el symlink
RUN mkdir -p /usr/local/sdkman/candidates/java/ \
    && ln -sf /usr/lib/jvm/msopenjdk-21 /usr/local/sdkman/candidates/java/current

ENV JAVA_HOME=/usr/local/sdkman/candidates/java/current
ENV PATH=$JAVA_HOME/bin:$PATH

# 4. Persistencia de variables (Corregido syntax error)
RUN echo "export JAVA_HOME=${JAVA_HOME}" > /etc/profile.d/java.sh \
    && echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile.d/java.sh \
    && chmod +x /etc/profile.d/java.sh \
    && echo "source /etc/profile.d/java.sh" >> ~/.bashrc \
    && echo "source /etc/profile.d/java.sh" >> ~/.zshrc

# 5. Instalación de Aider
# Usamos --break-system-packages de forma segura para el contenedor
RUN pip3 install --no-cache-dir aider-chat --break-system-packages