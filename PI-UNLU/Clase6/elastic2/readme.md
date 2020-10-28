# EFK stack on Docker (vía Docker Compose)

A tutorial to build a complete Docker environment for running an EFK stack on your local machine.

Includes:

- [Elasticsearch][elasticsearch]
- [Fluentd][fluentd]
- [Kibana][kibana]

<p align="left"> <img src="https://miro.medium.com/max/906/1*jr0cpmzt4pzLv_iU53nTdw.jpeg" width="500"/> </p> 

## Introduction

Here we are going to explore how to implement an unified logging system for your Docker containers. As software systems grow and become more and more decoupled, log aggregation is a key aspect to take care of. 

**How is done in the past:**
The old fashion way is to write these messages to a log file (or even worst, not using a Logging framework :/), but that inherits certain problems specifically when:
* We try to perform some searchs and analysis over the registers
* We have more than one instance running (multithread p.e)
* Other cases, even more complex.

**How we must to face:**

The issues to tackle down with logging are:

- Having a centralized overview of all log events
- Normalizing different log types (p.e using json format)
- Automated processing of log messages
- Supporting several sources and integrating with very different scenarios 

While [Elasticsearch][elasticsearch] and [Kibana][kibana] are the reference products *de facto* for log searching and visualization in the open source community. Elasticsearch is a NoSQL database that is based on the Lucene search engine. Kibana is a visualization layer that works on top of Elasticsearch. However, there's no such agreement for **data/log collectors and data aggregation**. A data collector is a pipeline tool that accepts inputs from various sources, executes different transformations, and exports the data to various targets.

The two most-popular data collectors are:

- [Logstash][logstash], or their lite version [Elastic Beats][filebeat] are most known for being part of the [ELK Stack][elk]. This version is commonly used in "traditional environments", p.e for monolithic applications on traditional VMs
- [Fluentd][fluentd], or their lite version [Fluent-bit][fluent-bit] are usually referenced as [EFK stack][efk]. This version is the most used by communities of users of [Cloud Native software][cncf], or microservices  hosted on [Docker][docker-fluentd]/[Kubernetes][kubernetes]
If you need more detail about this two data/log collectors (Logtash - Fluentd), you can go to the [reference](#reference) part of this repository. 

Together, these three different open source products (ELK or EFK) are most commonly used in log analysis in IT environments (though there are many more use cases for the ELK Stack starting including business intelligence, security and compliance, and web analytics).


**Our aim is to show and explain how to run a simple EFK stack on your local machine using docker-compose.**

### Why I choose Fluentd (EFK Stack)?
- Docker "native" support
- Performance and high-volume logging
- FluentD provides both active-active and active-passive deployment patterns for both availability and scale.
- FluentD can forward log and event data to any number of additional processing nodes
- Tagging and Dynamic Routing with FluentD
- Larger Plugin Library with FluentD

<p align="left"> <img src="https://www.fluentd.org/assets/img/recipes/fluentd_docker.png" width="500"/> </p> 

## Launching the EFK stack

### 1. Requirements

On your machine, make sure you have installed:

- [Docker][docker]
- [Docker Compose][docker-compose]

### 2. Clone the repository!

```bash
git clone https://github.com/dpetrocelli/sdypp2020/tree/master/PI-UNLU/Clase6/elastic2
```

### 3. Run the Docker Compose file!
```bash
docker-compose up
```

Please note: 
* In this example Fluentd will be running as a container on port `9999` instead of the default `24224`.
This settings has been changed to show how to magen Fluentd configurations.

* Kibana container (Web GUI) is exposed on port `5601`.

### 4. Test Fluentd configuration and Kibana Web GUI

Have you installed curl client? If not install it :D 

Then, you can generate a simple JSON HTTP request to be managed by fluentD and routed to ElasticSearch container:

```bash
curl -X POST -d 'json={"action":"test","userId":"dpetrocelli"}' http://localhost:9999/dpetrocelli/testSite
```

### 5. Open your Kibana Web GUI

- Open your web browser on http://localhost:5601
<p align="left"> <img src="https://i.imgur.com/wYqAzw0.png" width="1000"/> </p> 

- Go to the "Discover" web page
<p align="left"> <img src="https://i.imgur.com/GVnaAR7.png" width="700"/> </p> 

- Look for our action=test Json message
<p align="left"> <img src="https://i.imgur.com/4JmBzOT.png" width="1000"/> </p> 

Please note: 
* In this example you have used **"my fluent.conf"** file configuration and we haven't explained how it must be configured for your own necesities. In the following part [Configure Docker Logging Driver](#configure-docker-logging-driver), we will explain it in detail.

## Reference

- [Quora - What is the ELK stack](https://www.quora.com/What-is-the-ELK-stack)
- [Kubernetes Logging: Comparing Fluentd vs. Logstash](https://bit.ly/3dTrovb)
- [Fluentd vs Logstash, An unbiased comparison](https://techstricks.com/fluentd-vs-logstash/)
- [Fluentd vs. Logstash: A Comparison of Log Collectors | Logz.io](https://logz.io/blog/fluentd-logstash/)
- [Logstash vs Fluentd — Which one is better !](https://medium.com/techmanyu/logstash-vs-fluentd-which-one-is-better-adaaba45021b)
- [FluentD vs. Logstash: How to Decide for Your Organization](https://bit.ly/2J43KRp)
- [Stackshare - Fluentd vs Logtash current state](https://stackshare.io/stackups/fluentd-vs-logstash)
- [Fluentd - Config File Syntax](https://docs.fluentd.org/configuration/config-file)
- [Fluentd logging driver](https://test-dockerrr.readthedocs.io/en/latest/admin/logging/fluentd/)

[elasticsearch]: https://www.elastic.co/products/elasticsearch
[fluentd]: https://www.fluentd.org/
[kibana]: https://www.elastic.co/products/kibana
[logstash]: https://www.elastic.co/products/logstash
[elk]: https://www.elastic.co/videos/introduction-to-the-elk-stack
[docker-fluentd]: https://docs.docker.com/reference/logging/fluentd/
[efk]: https://docs.openshift.com/enterprise/3.1/install_config/aggregate_logging.html#overview
[docker]: https://www.docker.com/
[docker-compose]: https://docs.docker.com/compose/
[rested]: https://itunes.apple.com/au/app/rested-simple-http-requests/id421879749?mt=12
[kubernetes]: https://bit.ly/2TlLl4v
[filebeat]: https://www.elastic.co/es/beats/filebeat
[fluent-bit]: https://fluentbit.io/
[cncf]: https://www.cncf.io/

## Configure Docker Logging Driver

The configuration file consists of the following directives:
- **source**    directives determine the input sources
- **match**     directives determine the output destinations
- **filter**    directives determine the event processing pipelines
- **system**    directives set system-wide configuration
- **label**     directives group the output and filter for internal routing
- **@include**  directives include other files

### 1. Configure Source
Fluentd input sources are enabled by selecting and configuring the desired input plugins using source directives. Fluentd standard input plugins include http and forward. The http provides an HTTP endpoint to accept incoming HTTP messages whereas forward provides a TCP endpoint to accept TCP packets. Of course, it can be both at the same time. You may add multiple source configurations as required.
```yaml
# Receive events from 24224/tcp
# This is used by log forwarding and the fluent-cat command
<source>
  @type forward
  port 24224
</source>

# Set Fluentd to listen via http on port 9999, listening on all hosts
<source>
  @type http
  port 9999
  bind 0.0.0.0
</source>
# Events having prefix 'dpetrocelli.**' will be stored both on Elasticsearch and files.
<match dpetrocelli.**>
  @type copy
  <store>
    # Elastic search method 
    @type elasticsearch
    host elasticsearch
    port 9200
    index_name fluentd
    type_name fluentd
    logstash_format true
    logstash_prefix fluentd
    logstash_dateformat %Y%m%d
    include_tag_key true
    tag_key @log_name
    flush_interval 1s
  </store>
  #<store>
    # Store to a file (if needed)
    #@type file
    #path /logs/dpetrocelli
    #flush_interval 15s
  #</store>
  <store>
  # All will be also printed to stdout
    @type stdout
  </store>
</match>
```
### 2. Configure "match", Tell fluentd what to do!

### Configure FluentD vía fluent.conf file

### How do Docker Logs work?
The default configuration of Docker supplies a view of the logs emitted from containers (and the applications within) in two forms: the console or standard output (also known as stdout) and JSON-formatted files stored on the hard disk.
<p align="left"> <img src="https://coralogix.com/wp-content/uploads/2020/07/1.How-does-logging-work-in-Docker.png" width="700"/> </p> 

On Docker v1.6, the concept of logging drivers was introduced. The Docker engine is aware of the output interfaces that manage the application messages.

Since Docker v1.8, it was implemented a native Fluentd Docker logging driver. With that, you are able to have a unified and structured logging system with the simplicity and high performance of Fluentd.
<p align="left"> <img src="http://dondocker.com/wp-content/uploads/2016/10/dondocker-fluentd-logs.png" width="700"/> </p> 

The Fluentd Logging Driver supports following options through the --log-opt Docker command-line argument:
- fluentd-address
- tag

The fluentd-address specifies the optional address (<ip>:<port>) for Fluentd.
Log tags are a major requirement for Fluentd as they allow for **identifying the source** of incoming data and **take routing decisions**. By default, the Fluentd logging driver uses the container_id as a tag (64 character ID). You can change its value with the tag

```yaml
   web:
    container_name: nodejs-ws
    build: ./node-web-server
    ports:
      - "8080:8080"
    logging:
      driver: fluentd
      options:
        fluentd-address: localhost:9999
        tag: ws-logs
```
