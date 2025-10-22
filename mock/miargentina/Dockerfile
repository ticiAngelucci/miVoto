FROM node:20-alpine
WORKDIR /app
COPY package.json ./
RUN npm install --production
COPY server.js ./
ENV PORT=9999
EXPOSE 9999
CMD ["npm", "start"]
