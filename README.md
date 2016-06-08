# The DataLock Client

This intends to be a Datastore v1beta3 compatible client.

* Based on Java 8 and CompletableFuture's.
* The client is fully asynchronous.
* All models are immutable.
* Dynamic field values are encapsulated to allow for type-safe and efficient
  processing using the [visitor pattern](https://en.wikipedia.org/wiki/Visitor_pattern).
* Flexible reflection-based databinding, that also works with immutable types.
  This is based on [tiny-ogt](https://github.com/udoprog/tiny-ogt).

This is currently an **Alpha** project.

# Building

**Alpha Notice:** You have to download and install [Tiny-OGT](https://github.com/udoprog/tiny-ogt) to build this project.

This project is built using Maven:

```
$> mvn package
```

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
