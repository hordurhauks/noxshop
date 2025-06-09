# frontend/Dockerfile

# Build Stage
FROM node:20 AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production Stage
FROM nginx:stable-alpine AS production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Optional: Custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf
