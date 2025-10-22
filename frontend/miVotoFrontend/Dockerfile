# Build stage
FROM node:20-alpine AS builder
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL=http://localhost:8080
ARG VITE_OAUTH_AUTHORIZE_URL=http://localhost:9999/oauth/authorize
ARG VITE_MIARG_CLIENT_ID=stub-client
ARG VITE_MIARG_REDIRECT_URI=http://localhost:8080/auth/miargentina/callback
ENV VITE_API_URL=${VITE_API_URL}
ENV VITE_OAUTH_AUTHORIZE_URL=${VITE_OAUTH_AUTHORIZE_URL}
ENV VITE_MIARG_CLIENT_ID=${VITE_MIARG_CLIENT_ID}
ENV VITE_MIARG_REDIRECT_URI=${VITE_MIARG_REDIRECT_URI}
RUN npm run build

# Runtime stage
FROM nginx:1.27-alpine
COPY --from=builder /usr/src/app/dist /usr/share/nginx/html
# Basic nginx config ensures SPA routes resolve to index.html
RUN printf 'server {\n  listen 80;\n  server_name _;\n  root /usr/share/nginx/html;\n  include /etc/nginx/mime.types;\n  location / {\n    try_files $uri /index.html;\n  }\n}\n' > /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
