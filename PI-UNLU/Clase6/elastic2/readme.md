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
<p align="left"> <img src="https://i.imgur.com/wYqAzw0.png" width="500"/> </p> 

- Go to the "Discover" web page
<p align="left"> <img src="https://i.imgur.com/GVnaAR7.png" width="500"/> </p> 

- Look for our action=test Json message
<p align="left"> <img src="https://i.imgur.com/4JmBzOT.png" width="500"/> </p> 


## Reference

- [Quora - What is the ELK stack](https://www.quora.com/What-is-the-ELK-stack)
- [Kubernetes Logging: Comparing Fluentd vs. Logstash](https://bit.ly/3dTrovb)
- [Fluentd vs Logstash, An unbiased comparison](https://techstricks.com/fluentd-vs-logstash/)
- [Fluentd vs. Logstash: A Comparison of Log Collectors | Logz.io](https://logz.io/blog/fluentd-logstash/)
- [Logstash vs Fluentd — Which one is better !](https://medium.com/techmanyu/logstash-vs-fluentd-which-one-is-better-adaaba45021b)
- [FluentD vs. Logstash: How to Decide for Your Organization](https://bit.ly/2J43KRp)
- [Stackshare - Fluentd vs Logtash current state](https://stackshare.io/stackups/fluentd-vs-logstash)

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
