## Jetty 11/12 async servlet stream reset behavior

This is a test case to reproduce https://github.com/jetty/jetty.project/issues/12313.

This is an example project to explore how Jetty invokes async servlet callbacks when a client disconnects.
A servlet is set up to run from an h2c-only server (no tls for easier setup) on port 10000. There are three
commands that can run servers in this way:
 * `./gradlew :jetty-11:run` will run a Jetty 11 (with ee9) server
 * `./gradlew :jetty-12:ee9:run` will run a Jetty 12 (with ee9) server
 * `./gradlew :jetty-12:ee10:run` will run a Jetty 12 (with ee10) server

Each server has slightly different behavior in this test.

Then in a separate terminal, the client can be run with the command `./gradlew :client:run`. With all three
servers, the wire traffic seems to be the same, and the resulting client output is the same, but each has
different handling of the client's RST_STREAM.
