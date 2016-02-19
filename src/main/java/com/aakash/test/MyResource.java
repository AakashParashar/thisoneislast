package com.aakash.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

@Path("/create")
public class MyResource {

	static AmazonEC2 ec2;

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newMachine(@FormParam("user") String user, @Context ServletContext context) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		init();
		try {
			KeyPair keypair = createKeyPair("mySmallTest");
			Thread.currentThread();
			Thread.sleep(1 * 1000);
			String runningInstanceID = createAMInstanceSmall("ami-11ca2d78", "mySmallTest");
			map.put(user, runningInstanceID);

		} catch (AmazonServiceException ase) {
			System.out.println("Hey "+user+"!");
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
			return Response.status(ase.getStatusCode()).entity("Hi "+user+"!\n"+"Caught Exception: "+ase.getErrorMessage()+"\n"+"Response Status Code "+ase.getStatusCode()+"\n"+"RequestID: "+ase.getRequestId()).type(MediaType.APPLICATION_JSON).build();
		}
		
		UriBuilder builder = UriBuilder.fromPath(context.getContextPath());
		builder.path("/newMachine.jsp");
		return Response.seeOther(builder.build()).build();
	// 	return Response.status(200).entity(runningInstanceID).build();
	}

	@GET
	@Path("/listAll")
	public Response getAllAvailableImages(@Context ServletContext context) throws Exception {
		try {
			init();
			getAvailableImages();
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
			return Response.status(ase.getStatusCode()).entity("Hi !\n"+"Caught Exception: "+ase.getErrorMessage()+"\n"+"Response Status Code: "+ase.getStatusCode()+"\n"+"RequestID: "+ase.getRequestId()).type(MediaType.APPLICATION_JSON).build();
		}

		UriBuilder builder = UriBuilder.fromPath(context.getContextPath());
		builder.path("/getInstances.jsp");
		return Response.seeOther(builder.build()).build();

	}

	@POST
	@Path("/listRunning")
	public static String getRunningInstances() throws AmazonServiceException, Exception {

		String ret = "";
		try {
			init();
			DescribeInstancesResult describeInstancesResult = ec2.describeInstances();

			// The list of reservations containing the describes instances.
			List<Reservation> reservations = describeInstancesResult.getReservations();
			Set<Instance> instances = new HashSet<Instance>();

			for (Reservation reservation : reservations) {

				instances.addAll(reservation.getInstances());
			}
			ret += "You have " + instances.size() + " Amazon EC2 instance(s) running.\n";
			System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) running.");

			if (!instances.isEmpty()) {
				Iterator<Instance> instIterator = instances.iterator();
				int count = 0;
				// getting the descriptions of our running instances
				while (instIterator.hasNext()) {
					// the method runningInst.getState().toString() tell you if
					// the machine is really running or is terminated or stopped
					// or stopping or terminating or pending etc..
					Instance runningInst = instIterator.next();
					ret += "Instance " + count + " ImageId " + runningInst.getImageId() + " type: "
							+ runningInst.getInstanceType() + " Started by " + runningInst.getKeyName() + " Status: "
							+ runningInst.getState().toString() + "\n";
					System.out.println("Instance " + count + " ImageId " + runningInst.getImageId() + " type: "
							+ runningInst.getInstanceType() + " Started by " + runningInst.getKeyName() + " Status: "
							+ runningInst.getState().toString());
					count++;
				}
			}
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
			
		}

		return ret;

	}

	@POST
	@Path("/terminate")
	public static void terminateAMI(String instanceId) throws AmazonServiceException, Exception {
		try {
			init();
			TerminateInstancesRequest rq = new TerminateInstancesRequest();

			rq.getInstanceIds().add(instanceId);
			// the method returns when you move from "your previous state" to
			// terminating and not when the machine is actually terminated.
			// You have the same problems if you use asynchronous call too
			TerminateInstancesResult rsp = ec2.terminateInstances(rq);

			System.out.println("InsanceID Terminated: " + rsp.toString());
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}
	}

	public static KeyPair createKeyPair(String keyPairName) throws AmazonServiceException, Exception {

		CreateKeyPairRequest kpReq = new CreateKeyPairRequest();
		kpReq.setKeyName(keyPairName);
		CreateKeyPairResult kpres = ec2.createKeyPair(kpReq);
		KeyPair keyPair = kpres.getKeyPair();
		System.out.println("You havekeyPair.getKeyName = " + keyPair.getKeyName() + "\nkeyPair.getKeyFingerprint()="
				+ keyPair.getKeyFingerprint() + "\nkeyPair.getKeyMaterial()=" + keyPair.getKeyMaterial());
		return keyPair;
	}

	public static void init() {
		AWSCredentials credentials = null;
		try {
			credentials = new PropertiesCredentials(MyResource.class.getResourceAsStream("AwsCredentials.properties"));
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. " + e);
		}
		ec2 = new AmazonEC2Client(credentials);
		System.out.println("Init End");
	}

	public static String createAMInstanceSmall(String amiId, String keyPairName)
			throws AmazonServiceException, Exception {

		String runninginstanceID = "";

		List<String> runninginstanceIDs = createAMInstances(amiId, 1, 1, keyPairName, "m1.small", "us-east-1a");

		// if(runninginstanceIDs.iterator().hasNext())
		runninginstanceID = runninginstanceIDs.iterator().next();

		return runninginstanceID;
	}

	public static List<String> createAMInstances(String AMId, int min, int max, String keyPairName, String insType,

			String availabilityZone) throws AmazonServiceException, Exception {
		List<String> runninginstanceIDs = new ArrayList<String>();
		RunInstancesRequest request = new RunInstancesRequest();

		request.setInstanceType(insType);

		request.setMinCount(min);

		request.setMaxCount(max);

		Placement p = new Placement();
		p.setAvailabilityZone(availabilityZone);
		request.setPlacement(p);
		request.setImageId(AMId);

		request.setKeyName(keyPairName); // assign Keypair name for this request

		RunInstancesResult runInstancesRes = ec2.runInstances(request);
		String reservationId = runInstancesRes.getReservation().getReservationId();

		Reservation reservation = runInstancesRes.getReservation();
		List<Instance> instances = reservation.getInstances();
		if (!instances.isEmpty()) {
			Iterator<Instance> instIterator = instances.iterator();
			int count = 0;

			while (instIterator.hasNext()) {
				Instance runningInst = instIterator.next();
				System.out.println("We just start the Instance " + count + " UniqueID: " + runningInst.getInstanceId()
						+ " ImageId " + runningInst.getImageId() + " type: " + runningInst.getInstanceType()
						+ " Started by " + runningInst.getKeyName() + " Status: " + runningInst.getState().toString());
				// Unique ID of the image that is running
				String uniqueID = runningInst.getInstanceId();
				runninginstanceIDs.add(uniqueID);
				count++;
			}
		}

		System.out.println("reservation ID of the executed transation: " + reservationId);

		return runninginstanceIDs;

	}

	public static String getAvailableImages() throws AmazonServiceException, Exception {
		String ret = "";
		DescribeImagesResult describeImagesResult = ec2.describeImages();
		List<Image> listOfImages = describeImagesResult.getImages();
		Iterator<Image> listOfImagesIterator = listOfImages.iterator();
		int count = 0;
		while (listOfImagesIterator.hasNext()) {
			Image img = listOfImagesIterator.next();
			// un-comment this if you want to filter to a given id. The default
			// list of available images is long
			// if (img.getImageId().contains("11ca2d78"))
			// {
			ret += "Image " + count + " Name: " + img.getName() + " Description: " + img.getDescription() + " Id: "
					+ img.getImageId() + "\n";
			System.out.println("Image " + count + " Name: " + img.getName() + " Description: " + img.getDescription()
					+ " Id: " + img.getImageId());

			// }

			count++;
		}

		return ret;

	}

}
