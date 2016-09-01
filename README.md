# The DataLock Client

This intends to be a Datastore gRPC V1 compatible client.

* Based on Java 8 and CompletableFuture's.
* The client is fully asynchronous.
* All models are immutable.
* Dynamic field values are encapsulated to allow for type-safe and efficient
  processing using the [visitor pattern](https://en.wikipedia.org/wiki/Visitor_pattern).
* Flexible reflection-based databinding, that also works with immutable types.
  This is based on [scribe](https://github.com/udoprog/scribe).
* Does not export problematic transitive dependencies (i.e. protobuf and gRPC)
  instead, these are bundled in a separate package.

This is currently an **Alpha** project.

# Building

This project currently depends on an un-published version of
[googleapis](googleapis). This must be prepared using the `prepare` command.

```
$> ./prepare
```

After that, this project is built using Maven.

```
$> mvn package
```

[googleapis]: https://github.com/googleapis/googleapis

# Testing

To run integration tests, make sure to run the datastore emulator in non-legacy
mode:

```
$> gcloud beta emulators datastore start --no-legacy --data-dir ./temp/datastore --host-port localhost:8080 --project test --consistency 1.0
```

While this is runnig, you can run the integration tests with Maven:

```
$> mvn -D environment=test verify
```

# Usage

For now you can see how to use the project in the
[DataLockIT Test](/core/src/test/java/eu/toolchain/datalock/DataLockIT.java)

# Hacking

This project requires Lombok to be configured for your IDE.

* [IDEA Code Style](/idea/code-style.xml)
