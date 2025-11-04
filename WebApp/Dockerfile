# My Spy - Django Backend Dockerfile
# Python 3.11 - Optimized Multi-stage Build
# @author Michael KOJDL
# @version 2.0.0

# ===== Stage 1: Builder =====
FROM python:3.11-slim-bookworm as builder

# Set environment variables
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    g++ \
    libpq-dev \
    libmariadb-dev \
    libmariadb-dev-compat \
    python3-dev \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Create virtual environment
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Copy requirements and install Python dependencies
COPY requirements.txt /tmp/requirements.txt
RUN pip install --upgrade pip setuptools wheel && \
    pip install -r /tmp/requirements.txt


# ===== Stage 2: Runtime =====
FROM python:3.11-slim-bookworm

# Set environment variables
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PATH="/opt/venv/bin:$PATH" \
    DJANGO_SETTINGS_MODULE=config.settings.production

# Install runtime dependencies only
RUN apt-get update && apt-get install -y --no-install-recommends \
    libpq5 \
    libmariadb3 \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create app user (non-root)
RUN groupadd -r myspy && useradd -r -g myspy -u 1000 myspy

# Create app directories
RUN mkdir -p /app /app/staticfiles /app/mediafiles /app/logs && \
    chown -R myspy:myspy /app

# Copy virtual environment from builder
COPY --from=builder /opt/venv /opt/venv

# Set working directory
WORKDIR /app

# Copy application code
COPY --chown=myspy:myspy . /app/

# Create empty local_settings.py if it doesn't exist
RUN touch /app/config/settings/local_settings.py && \
    chown myspy:myspy /app/config/settings/local_settings.py

# Copy entrypoint script
COPY --chown=myspy:myspy docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Switch to non-root user
USER myspy

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8000/api/v1/status/ || exit 1

# Set entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]

# Default command (can be overridden)
CMD ["gunicorn", "config.wsgi:application", "--bind", "0.0.0.0:8000", "--workers", "4", "--timeout", "120"]
