FROM node:16
WORKDIR /work
RUN apt-get update && apt-get install -y curl
RUN npm install -g artillery
#RUN npm install artillery-plugin-metrics-by-endpoint -g
RUN npm install -g https://github.com/preguica/artillery-plugin-metrics-by-endpoint.git
RUN npm install faker
COPY images/ /work/images
COPY test-utils.js .
COPY create-users.yml .
COPY create-messages.yml .
COPY create-channels.yml .
COPY workload1.yml .
COPY testing.sh .
CMD ./testing.sh
#CMD ["artillery"]
