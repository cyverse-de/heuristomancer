FROM clojure
COPY ./docker/profiles.clj /root/.lein/profiles.clj
WORKDIR /usr/src/heuristomancer

COPY project.clj /usr/src/heuristomancer/
RUN lein deps

COPY . /usr/src/heuristomancer
CMD ["lein", "test"]
