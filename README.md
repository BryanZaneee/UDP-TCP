# UDP vs TCP image transfer

Two Java client and server pairs that transfer the same ten JPEG images, one
over TCP and one over UDP, and time each round trip. The point is the
comparison: same payload, same request pattern, two transport protocols.

Written for a networking course. `docs/PA2.pdf` is the assignment and
`docs/report.pdf` is the write-up of the measured results.

## How it works

Each client requests images 1 through 10 in order. For every request it
records `System.nanoTime()` before sending and after the last byte arrives,
writes the image to `received_meme_<n>.jpg`, and prints the minimum, maximum,
mean, and standard deviation of the ten round trips at the end.

The TCP server threads each connection and frames the response as filename,
byte count, then bytes. The UDP server replies with a single datagram per
request, which is why large images are where the two protocols diverge.

Both servers read from `memes/meme<n>.jpg`. On `bye` the UDP server replies
`disconnected`; the TCP server just closes the connection.

## Installation

Requires a JDK. No build tool, no dependencies.

```bash
git clone https://github.com/BryanZaneee/UDP-TCP.git
cd UDP-TCP
javac TCPserver.java TCPclient.java UDPserver.java UDPclient.java
```

Compiled `.class` files are checked in, so this step only matters if you edit
the source.

## Usage

Run each server from the repo root, so the relative `memes/` path resolves.

TCP, in two terminals:

```bash
java TCPserver 5927
java TCPclient localhost 5927
```

UDP, in two terminals:

```bash
java UDPserver 5927
java UDPclient localhost 5927
```

Both servers take the port as their only argument; both clients take the
server address and port.

## Contributing

This is archived coursework and is not taking contributions.

## License

No license file is included, so this is "all rights reserved" by default.
