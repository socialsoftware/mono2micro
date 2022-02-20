FROM node:16-alpine3.14

WORKDIR /app

COPY package.json ./

RUN npm install --legacy-peer-deps

RUN mkdir -p node_modules/.cache && chmod -R 777 node_modules/.cache

COPY . .

CMD ["npm", "start"]
