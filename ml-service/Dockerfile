FROM python:3.10-slim AS base

# ---------- build ----------
WORKDIR /srv
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip \
 && pip install --no-cache-dir -r requirements.txt

# ---------- runtime ----------
ENV PYTHONUNBUFFERED=1
COPY . .
EXPOSE 5000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "5000"]
