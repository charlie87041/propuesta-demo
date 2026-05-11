package com.cookiesstore.infra.web;

import com.cookiesstore.infra.service.Ec2InfrastructureService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

@RestController
@RequestMapping("/api/infra/aws")
public class InfrastructureController {

    private final Ec2InfrastructureService ec2InfrastructureService;
    private final StsClient stsClient;

    public InfrastructureController(Ec2InfrastructureService ec2InfrastructureService, StsClient stsClient) {
        this.ec2InfrastructureService = ec2InfrastructureService;
        this.stsClient = stsClient;
    }

    @GetMapping("/whoami")
    public Map<String, String> whoAmI() {
        GetCallerIdentityResponse identity = stsClient.getCallerIdentity();
        return Map.of(
            "account", identity.account(),
            "arn", identity.arn(),
            "userId", identity.userId()
        );
    }

    @GetMapping("/ec2/instances")
    public Object listEc2Instances() {
        return ec2InfrastructureService.listInstances();
    }
}
