package com.example;

import com.oracle.bmc.core.model.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.core.ComputeAsyncClient;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.ComputeWaiters;
import com.oracle.bmc.core.model.LaunchInstanceDetails;
import com.oracle.bmc.core.requests.GetInstanceRequest;
import com.oracle.bmc.core.requests.LaunchInstanceRequest;
import com.oracle.bmc.core.responses.GetInstanceResponse;
import com.oracle.bmc.core.responses.LaunchInstanceResponse;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.responses.AsyncHandler;
import com.oracle.bmc.workrequests.WorkRequestClient;

public class Test {
    private static final String PROFILE_DEFAULT = "DEFAULT";

    private static List<String> synchronousClient(int numberOfInstances) throws Exception {
        long startTime = System.nanoTime();

        ArrayList<String> instanceIds = new ArrayList<>();
        // Custom client config
        ClientConfiguration clientConfig = ClientConfiguration.builder()
                .connectionTimeoutMillis(3000)
                .readTimeoutMillis(60000)
                .build();
        // Auth provider
        AuthenticationDetailsProvider authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(
                PROFILE_DEFAULT);

        // Compute client
        ComputeClient computeClient = ComputeClient.builder()
                .configuration(clientConfig)
                .build(authenticationDetailsProvider);
        computeClient.setRegion(Region.UK_LONDON_1);

        // Default values
        String availabilityDomain = "sbnF:UK-LONDON-1-AD-1";
        String compartmentId = "ocid1.compartment.oc1..aaaaaaaapbatjdpgcbfpvwxmfkav5ijagbf7aepp5ln7xxlpl5ba347xukja";
        String imageId = "ocid1.image.oc1.uk-london-1.aaaaaaaagr3ajohdc3fzfjyxy6hqhqwsupudqkhz776taz57ib5dhcbct64q";
        String subnetId = "ocid1.subnet.oc1.uk-london-1.aaaaaaaam5hdpn7ncpwvqeqctfnmrpgfixcp7ar7fpoextaa36vn7yre7waq";
        String shape = "VM.Standard.E2.1";
        String instanceName = "test-";

        for (int i = 0; i < numberOfInstances; i++) {
            long startTime1 = System.nanoTime();
            // Set instance details
            LaunchInstanceDetails launchInstanceDetails = LaunchInstanceDetails.builder()
                    .availabilityDomain(availabilityDomain)
                    .compartmentId(compartmentId)
                    .displayName(instanceName + (i + 1))
                    .imageId(imageId)
                    .subnetId(subnetId)
                    .shape(shape)
                    .build();

            // Create request
            LaunchInstanceRequest launchInstanceRequest = LaunchInstanceRequest.builder()
                    .launchInstanceDetails(launchInstanceDetails)
                    .build();

            // Send request
            LaunchInstanceResponse launchInstanceResponse = computeClient.launchInstance(launchInstanceRequest);
            String instanceId = launchInstanceResponse.getInstance().getId();
            instanceIds.add(instanceId);
            long elapsedTime1 = System.nanoTime() - startTime1;
            System.out.println(instanceId + " created in " + (elapsedTime1 / 1000000) + " ms");
        }

        long elapsedTime = System.nanoTime() - startTime;
        System.out
                .println("Total execution time in seconds: " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime / 1000000));
        return instanceIds;
    }

    private static ArrayList<String> asynchronousClient(int numberOfInstances) throws Exception {
        long startTime = System.nanoTime();

        ArrayList<String> instanceIds = new ArrayList<>();
        // Futures for async handlers
        List<Future<LaunchInstanceResponse>> futures = new CopyOnWriteArrayList<>();

        AsyncHandler<LaunchInstanceRequest, LaunchInstanceResponse> handler = new AsyncHandler<LaunchInstanceRequest, LaunchInstanceResponse>() {
            @Override
            public void onSuccess(LaunchInstanceRequest request, LaunchInstanceResponse response) {
                String requestId = response.getOpcRequestId();
                Instance instance = response.getInstance();
                // System.out.println(requestId);
                // System.out.println(requestId + " - " + instance.getDisplayName() + " - " +
                // instance.getId());
            }

            @Override
            public void onError(LaunchInstanceRequest request, Throwable error) {
                error.printStackTrace();
            }
        };

        // Custom client config
        ClientConfiguration clientConfig = ClientConfiguration.builder()
                .connectionTimeoutMillis(3000)
                .readTimeoutMillis(60000)
                .build();
        // Auth provider
        AuthenticationDetailsProvider authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(
                PROFILE_DEFAULT);

        // Compute client
        ComputeAsyncClient computeClient = ComputeAsyncClient.builder()
                .configuration(clientConfig)
                .build(authenticationDetailsProvider);
        computeClient.setRegion(Region.UK_LONDON_1);

        // Default values
        String availabilityDomain = "sbnF:UK-LONDON-1-AD-1";
        String compartmentId = "ocid1.compartment.oc1..aaaaaaaapbatjdpgcbfpvwxmfkav5ijagbf7aepp5ln7xxlpl5ba347xukja";
        String imageId = "ocid1.image.oc1.uk-london-1.aaaaaaaagr3ajohdc3fzfjyxy6hqhqwsupudqkhz776taz57ib5dhcbct64q";
        String subnetId = "ocid1.subnet.oc1.uk-london-1.aaaaaaaam5hdpn7ncpwvqeqctfnmrpgfixcp7ar7fpoextaa36vn7yre7waq";
        String shape = "VM.Standard.E2.1";
        String instanceName = "test-";

        for (int i = 0; i < numberOfInstances; i++) {
            long startTime1 = System.nanoTime();
            // Set instance details
            LaunchInstanceDetails launchInstanceDetails = LaunchInstanceDetails.builder()
                    .availabilityDomain(availabilityDomain)
                    .compartmentId(compartmentId)
                    .displayName(instanceName + (i + 1))
                    .imageId(imageId)
                    .subnetId(subnetId)
                    .shape(shape)
                    .build();

            // Create request
            LaunchInstanceRequest launchInstanceRequest = LaunchInstanceRequest.builder()
                    .launchInstanceDetails(launchInstanceDetails)
                    .build();

            // Send asynch request
            Future<LaunchInstanceResponse> future = computeClient.launchInstance(launchInstanceRequest, handler);
            futures.add(future);

            long elapsedTime1 = System.nanoTime() - startTime1;
            System.out.println("work requested in " + (elapsedTime1 / 1000000) + " ms");
        }

        // wait for the threads to complete
        int completed = 0;
        while (completed < numberOfInstances) {
            for (Future<LaunchInstanceResponse> f : futures) {
                if (f.isDone()) {
                    LaunchInstanceResponse response = (LaunchInstanceResponse) f.get();
                    String requestId = response.getOpcRequestId();
                    Instance instance = response.getInstance();
                    // System.out.println(requestId);
                    System.out.println(requestId + " - " + instance.getDisplayName() + " - " + instance.getId());
                    instanceIds.add(instance.getId());
                    completed++;
                    futures.remove(f);
                }
            }
        }

        long elapsedTime = System.nanoTime() - startTime;
        System.out
                .println("Total execution time in seconds: " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime / 1000000));
        return instanceIds;
    }

    public static ArrayList<String> clientWithWaiters(int numberOfInstances) throws Exception {
        long startTime = System.nanoTime();
        ArrayList<String> instanceIds = new ArrayList<>();

        ClientConfiguration clientConfig = ClientConfiguration.builder()
                .connectionTimeoutMillis(3000)
                .readTimeoutMillis(60000)
                .build();

        AuthenticationDetailsProvider authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(
                PROFILE_DEFAULT);
        System.out.println("authenticationDetailsProvider:" + authenticationDetailsProvider);

        IdentityClient identityClient = IdentityClient.builder().build(authenticationDetailsProvider);
        identityClient.setRegion(Region.UK_LONDON_1);
        System.out.println("identityClient:" + identityClient);

        ComputeClient computeClient = ComputeClient.builder()
                .configuration(clientConfig)
                .build(authenticationDetailsProvider);
        computeClient.setRegion(Region.UK_LONDON_1);
        System.out.println("computeClient:" + computeClient);

        WorkRequestClient workRequestClient = WorkRequestClient.builder().build(authenticationDetailsProvider);
        workRequestClient.setRegion(Region.UK_LONDON_1);
        System.out.println("workRequestClient:" + workRequestClient);

        ComputeWaiters computeWaiters = computeClient.newWaiters(workRequestClient);

        // oci compute instance launch --display-name "test-$i" --availability-domain
        // sbnF:UK-LONDON-1-AD-1 --compartment-id
        // ocid1.compartment.oc1..aaaaaaaapbatjdpgcbfpvwxmfkav5ijagbf7aepp5ln7xxlpl5ba347xukja
        // --shape VM.Standard.E2.1 --subnet-id
        // ocid1.subnet.oc1.uk-london-1.aaaaaaaam5hdpn7ncpwvqeqctfnmrpgfixcp7ar7fpoextaa36vn7yre7waq
        // --image-id
        // ocid1.image.oc1.uk-london-1.aaaaaaaagr3ajohdc3fzfjyxy6hqhqwsupudqkhz776taz57ib5dhcbct64q
        // --raw-output --query 'data.id'
        String availabilityDomain = "sbnF:UK-LONDON-1-AD-1";
        String compartmentId = "ocid1.compartment.oc1..aaaaaaaapbatjdpgcbfpvwxmfkav5ijagbf7aepp5ln7xxlpl5ba347xukja";
        String imageId = "ocid1.image.oc1.uk-london-1.aaaaaaaagr3ajohdc3fzfjyxy6hqhqwsupudqkhz776taz57ib5dhcbct64q";
        String subnetId = "ocid1.subnet.oc1.uk-london-1.aaaaaaaam5hdpn7ncpwvqeqctfnmrpgfixcp7ar7fpoextaa36vn7yre7waq";
        String shape = "VM.Standard.E2.1";
        String instanceName = "test-";

        for (int i = 0; i < numberOfInstances; i++) {
            long startTime1 = System.nanoTime();
            // Set instance details

            LaunchInstanceDetails launchInstanceDetails = LaunchInstanceDetails.builder()
                    .availabilityDomain(availabilityDomain)
                    .compartmentId(compartmentId)
                    .displayName(instanceName)
                    .imageId(imageId)
                    .subnetId(subnetId)
                    .shape(shape)
                    .build();
            // System.out.println("launchInstanceDetails:" + launchInstanceDetails);

            LaunchInstanceRequest launchInstanceRequest = LaunchInstanceRequest.builder()
                    .launchInstanceDetails(launchInstanceDetails)
                    .build();
            // System.out.println("launchInstanceRequest:" + launchInstanceRequest);

            LaunchInstanceResponse launchInstanceResponse = computeWaiters.forLaunchInstance(launchInstanceRequest)
                    .execute();
            // System.out.println("launchInstanceResponse:" + launchInstanceResponse);
            System.out.println("Instance-id:" + launchInstanceResponse.getInstance().getId());

            GetInstanceRequest getInstanceRequest = GetInstanceRequest.builder()
                    .instanceId(launchInstanceResponse.getInstance().getId())
                    .build();
            System.out.println("getInstanceRequest:" + getInstanceRequest);
            System.out.println("getInstanceRequest.getInstanceId():" +
                    getInstanceRequest.getInstanceId());

            GetInstanceResponse getInstanceResponse = computeWaiters
                    .forInstance(getInstanceRequest, Instance.LifecycleState.Running)
                    .execute();
            Instance instance = getInstanceResponse.getInstance();
            String instanceId = instance.getId();
            instanceIds.add(instanceId);

            long elapsedTime1 = System.nanoTime() - startTime1;
            System.out.println(instanceId + " created in " + (elapsedTime1 / 1000000) + " ms");
        }

        long elapsedTime = System.nanoTime() - startTime;
        System.out
                .println("Total execution time in seconds: " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime / 1000000));
        return instanceIds;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("ddd");
        // clientWithWaiters(5);
        //List<String> instanceIds = synchronousClient(10);
        List<String> instanceIds = asynchronousClient(10);
        System.out.println(instanceIds.size() + " instances created");
    }
}
