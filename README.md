#About

Apus is a high-performance distributed XMPP server for the JVM platform, based on Scala and Akka.

Features:
- high performance: the system is implemented with complete async fashion, using actor model (akka) for concurrency and NIO (netty) for network IO.
- no SPOF (single point of failure): message routing algorithm is designed based on consistent hashing, no master node in the cluster.

#Experiment results

Server cluster: 2 virtual machines with a quad-core CPU and 4 GB RAM.
Client: 10k simultaneous users simulated by Tsung, each of which keeps sending messages to random targets at the speed of one message per second.
Average message delivery latency: Apus: 1080ms, Tigase: 16376ms.