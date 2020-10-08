FROM alpine:latest
RUN apk add curl
CMD ["/usr/bin/curl", "-fL", "https://get.k3s.io | INSTALL_K3S_CHANNEL=latest K3S_TOKEN='mysecrettoken2' sh -s - server --server https://192.168.1.150:6443"]
