[
    {
        "Name": "bridge",
        "Id": "fe3356568f5f89e154a8fbb369dd3c538d39ead53f14ed7e34ef5367fceb992b",
        "Created": "2020-04-19T16:22:35.61544901-03:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": null,
            "Config": [
                {
                    "Subnet": "172.17.0.0/16",
                    "Gateway": "172.17.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {
            "1d85509c5331b169f5658e80af819a4d41f65d64341cd66730e7948ba8660ee6": {
                "Name": "nginx1",
                "EndpointID": "b0cbd7a85fa1067a28d3490a493d56c91b9bc60b1e56282bc217cc78bb0632b9",
                "MacAddress": "02:42:ac:11:00:02",
                "IPv4Address": "172.17.0.2/16",
                "IPv6Address": ""
            },
            "894c074e70c45d5aa6711f84b9abc52ddd82d24936e3eaf44caea8fc99ce0773": {
                "Name": "nginx-second",
                "EndpointID": "643596852ae964443d598eed525b88e7400a7c087b5cb3fa76fe7a8e878fd853",
                "MacAddress": "02:42:ac:11:00:03",
                "IPv4Address": "172.17.0.3/16",
                "IPv6Address": ""
            }
        },
        "Options": {
            "com.docker.network.bridge.default_bridge": "true",
            "com.docker.network.bridge.enable_icc": "true",
            "com.docker.network.bridge.enable_ip_masquerade": "true",
            "com.docker.network.bridge.host_binding_ipv4": "0.0.0.0",
            "com.docker.network.bridge.name": "docker0",
            "com.docker.network.driver.mtu": "1500"
        },
        "Labels": {}
    }
]
