FROM clojure
COPY . /usr/src/heuristomancer
COPY ./docker/profiles.clj /root/.lein/profiles.clj
WORKDIR /usr/src/heuristomancer
RUN lein deps
CMD ["lein", "test"]
