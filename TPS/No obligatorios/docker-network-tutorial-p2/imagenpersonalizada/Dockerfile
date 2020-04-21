FROM haproxy:latest
RUN apt update -y
RUN apt install keepalived -y
COPY keepalived.conf /etc/keepalived/keepalived.conf
COPY haproxy.cfg /usr/local/etc/haproxy/haproxy.cfg
#RUN /etc/init.d/keepalived start
