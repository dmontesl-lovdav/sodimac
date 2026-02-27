FROM node:22

WORKDIR /usr/src/app
COPY package.json ./
# Bundle app source
COPY . .
RUN rm .env
EXPOSE 8080
ENTRYPOINT ["npm", "run", "start"]
