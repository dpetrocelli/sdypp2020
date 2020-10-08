#FROM nginx:latest
#COPY default.conf /etc/nginx/conf.d/default.conf
FROM node:latest
COPY server.js /usr/src/server.js
WORKDIR /usr/src/
EXPOSE 80
CMD ["node", "server.js"]