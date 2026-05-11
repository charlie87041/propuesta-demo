package com.cookiesstore.infra.service;

import java.util.List;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;

@Service
public class Ec2InfrastructureService {

    private final Ec2Client ec2Client;

    public Ec2InfrastructureService(Ec2Client ec2Client) {
        this.ec2Client = ec2Client;
    }

    public List<Ec2InstanceView> listInstances() {
        return ec2Client.describeInstances(DescribeInstancesRequest.builder().build())
            .reservations()
            .stream()
            .flatMap(reservation -> reservation.instances().stream())
            .map(instance -> new Ec2InstanceView(
                instance.instanceId(),
                instance.state() != null ? instance.state().nameAsString() : "unknown",
                instance.instanceTypeAsString(),
                instance.privateIpAddress(),
                instance.publicIpAddress()
            ))
            .toList();
    }

    public record Ec2InstanceView(
        String instanceId,
        String state,
        String instanceType,
        String privateIp,
        String publicIp
    ) {
    }
}
