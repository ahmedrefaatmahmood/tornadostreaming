//This code is adopted  from this URL 
//http://jayatiatblogs.blogspot.com/2013/05/extracting-storm-web-ui-parameter-values.html

package edu.purdue.cs.tornado.performance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.thrift7.TException;
import org.apache.thrift7.protocol.TBinaryProtocol;
import org.apache.thrift7.transport.TFramedTransport;
import org.apache.thrift7.transport.TSocket;
import org.apache.thrift7.transport.TTransportException;

import backtype.storm.generated.BoltStats;
import backtype.storm.generated.ClusterSummary;
import backtype.storm.generated.ErrorInfo;
import backtype.storm.generated.ExecutorSpecificStats;
import backtype.storm.generated.ExecutorStats;
import backtype.storm.generated.ExecutorSummary;
import backtype.storm.generated.GlobalStreamId;
import backtype.storm.generated.Nimbus.Client;
import backtype.storm.generated.SpoutStats;
import backtype.storm.generated.TopologyInfo;
import backtype.storm.generated.TopologySummary;

/*
 * Library to extract Storm Web UI Parameter Values
 */
public class ClusterInformationExtractor {
	public static void main(String[] args) {
		String uiHost="";
		Integer uiPort=0;
		if(args.length<2)
		{
			System.err.println("Missing UI host and port");
			return;
		}
		else{
			uiHost = args[0];
			uiPort = Integer.parseInt(args[1]);
		}
		TSocket socket = new TSocket(uiHost, uiPort);
		TFramedTransport transport = new TFramedTransport(socket);
		TBinaryProtocol protocol = new TBinaryProtocol(transport);
		Client client = new Client(protocol);
		try {
			transport.open();
			ClusterSummary summary = client.getClusterInfo();

			// Cluster Details
			/*
			 * System.out.println("**** Storm UI Home Page ****");
			 * System.out.println(" ****Cluster Summary**** "); int nimbusUpTime
			 * = summary.get_nimbus_uptime_secs();// getNimbus_uptime_secs();
			 * System.out.println("Nimbus Up Time: " + nimbusUpTime);
			 * System.out.println("Number of Supervisors: " +
			 * summary.get_supervisors_size());// getSupervisorsSize());
			 * System.out.println("Number of Topologies: " +
			 * summary.get_topologies_size());// gettopologiesSize());
			 */
			// Topology stats
			System.out.println(" ****Topology summary**** ");
			Map<String, String> topologyConfigurationParamValues = new HashMap<String, String>();
			List<TopologySummary> topologies = summary.get_topologies(); // getTopologies();
			Iterator<TopologySummary> topologiesIterator = summary
					.get_topologies_iterator(); // getTopologiesIterator();
			while (topologiesIterator.hasNext()) {
				TopologySummary topology = topologiesIterator.next();
				System.out.println("Topology ID: " + topology.get_id());// getId());
				System.out.println("Topology Name: " + topology.get_name());
				System.out.println("Number of Executors: "
						+ topology.get_num_executors());
				System.out.println("Number of Tasks: "
						+ topology.get_num_tasks());
				System.out.println("Number of Workers: "
						+ topology.get_num_workers());
				System.out.println("Status : " + topology.get_status());
				System.out.println("UpTime in Seconds: "
						+ topology.get_uptime_secs());
			}

			// Supervisor stats
			/*
			 * System.out.println("**** Supervisor summary ****");
			 * List<SupervisorSummary> supervisors = summary.get_supervisors();
			 * Iterator<SupervisorSummary> supervisorsIterator = summary
			 * .get_supervisors_iterator(); while
			 * (supervisorsIterator.hasNext()) { SupervisorSummary supervisor =
			 * supervisorsIterator.next(); System.out.println("Supervisor ID: "
			 * + supervisor.get_supervisor_id()); System.out.println("Host: " +
			 * supervisor.get_host());
			 * System.out.println("Number of used workers: " +
			 * supervisor.get_num_used_workers());
			 * System.out.println("Number of workers: " +
			 * supervisor.get_num_workers());
			 * System.out.println("Supervisor uptime: " +
			 * supervisor.get_uptime_secs()); }
			 */
			// Nimbus config parameter-values
			/*
			 * System.out.println("****Nimbus Configuration****"); Map<String,
			 * String> nimbusConfigurationParamValues = new HashMap<String,
			 * String>(); String nimbusConfigString = client.getNimbusConf();
			 * nimbusConfigString = nimbusConfigString.substring(1,
			 * nimbusConfigString.length() - 1); String[] nimbusConfParameters =
			 * nimbusConfigString.split(",\""); for (String nimbusConfParamValue
			 * : nimbusConfParameters) { String[] paramValue =
			 * nimbusConfParamValue.split(":"); String parameter =
			 * paramValue[0].substring(0, paramValue[0].length() - 1); String
			 * parameterValue = paramValue[1]; if
			 * (paramValue[1].startsWith("\"")) { parameterValue =
			 * paramValue[1].substring(1, paramValue[1].length() - 1); }
			 * nimbusConfigurationParamValues.put(parameter, parameterValue); }
			 * 
			 * Set<String> nimbusConfigurationParameters =
			 * nimbusConfigurationParamValues .keySet(); Iterator<String>
			 * parameters = nimbusConfigurationParameters .iterator(); while
			 * (parameters.hasNext()) { String key = parameters.next();
			 * System.out.println("Parameter : " + key + " Value : " +
			 * nimbusConfigurationParamValues.get(key)); }
			 * 
			 * System.out.println(" **** End of Storm UI Home Page Details**** ")
			 * ;
			 */
			// Topology stats
			System.out.println(" **** Topology Home Page Details **** ");
			topologiesIterator = summary.get_topologies_iterator();
			while (topologiesIterator.hasNext()) {
				TopologySummary topology = topologiesIterator.next();
				System.out.println("**** Topology summary ****");
				System.out.print("Topology Id: " + topology.get_id() + "\t");
				System.out
						.print("Topology Name: " + topology.get_name() + "\t");
				System.out.print("Number of Executors: "
						+ topology.get_num_executors() + "\t");
				System.out.print("Number of Tasks: " + topology.get_num_tasks()
						+ "\t");
				System.out.print("Number of Workers: "
						+ topology.get_num_workers() + "\t");
				System.out.print("Status: " + topology.get_status() + "\t");
				System.out.println("UpTime in Seconds: "
						+ topology.get_uptime_secs() + "\t");

				// Spouts (All time)
				System.out.println("**** Spouts (All time) ****");
				TopologyInfo topology_info = client.getTopologyInfo(topology
						.get_id());
				Iterator<ExecutorSummary> executorStatusItr = topology_info
						.get_executors_iterator();
				HashMap<String, SpoutInfoAggrecate> spoutStateAggreactes = new HashMap<String, SpoutInfoAggrecate>();
				while (executorStatusItr.hasNext()) {
					// get the executor
					ExecutorSummary executor_summary = executorStatusItr.next();
					ExecutorStats execStats = executor_summary.get_stats();
					if (execStats != null) {
						ExecutorSpecificStats execSpecStats = execStats
								.get_specific();
						String componentId = executor_summary
								.get_component_id();
						// if the executor is a spout
						if (execSpecStats.is_set_spout()) {
							SpoutStats spoutStats = execSpecStats.get_spout();
							//System.out.println("Spout Id: " + componentId);
							if (!spoutStateAggreactes.containsKey(componentId))
								spoutStateAggreactes.put(componentId,
										new SpoutInfoAggrecate(componentId));

							SpoutInfoAggrecate overAllSpoutStats = spoutStateAggreactes
									.get(componentId);
							overAllSpoutStats.addSpoutStats(
									getStatValueFromMap(
											execStats.get_transferred(),
											":all-time"),
									getStatValueFromMap(
											execStats.get_emitted(),
											":all-time"),
									getStatValueFromMap(spoutStats.get_acked(),
											":all-time"),
									getStatValueFromMap(
											spoutStats.get_failed(),
											":all-time"),
									getStatValueFromMapDouble(
											spoutStats.get_complete_ms_avg(),
											":all-time"));
							// System.out.print("Transferred: "
							// + getStatValueFromMap(
							// execStats.get_transferred(),
							// ":all-time") + "\t");
							// System.out.print("Emitted: "
							// + getStatValueFromMap(
							// execStats.get_emitted(),
							// ":all-time") + "\t");
							// System.out
							// .print("Acked: "
							// + getStatValueFromMap(
							// spoutStats.get_acked(),
							// ":all-time") + "\t");
							// System.out.println("Failed: "
							// + getStatValueFromMap(
							// spoutStats.get_failed(),
							// ":all-time") + "\t");
						}
					}
				}
				Iterator<Entry<String, SpoutInfoAggrecate>> it = spoutStateAggreactes
						.entrySet().iterator();
				while (it.hasNext()) {
					System.out.println( it.next().getValue().toString());
				}

				// Bolts (All time)
				System.out.println("****Bolts (All time)****");
				HashMap<String, BoltInfoAggrecate> boltStateAggreactes = new HashMap<String, BoltInfoAggrecate>();
				executorStatusItr = topology_info.get_executors_iterator();
				while (executorStatusItr.hasNext()) {
					// get the executor
					ExecutorSummary executor_summary = executorStatusItr.next();
					ExecutorStats execStats = executor_summary.get_stats();
					ExecutorSpecificStats execSpecStats = execStats
							.get_specific();
					String componentId = executor_summary.get_component_id();
					if (execSpecStats.is_set_bolt()) {
						BoltStats boltStats = execSpecStats.get_bolt();

						if (!boltStateAggreactes.containsKey(componentId))
							boltStateAggreactes.put(componentId,
									new BoltInfoAggrecate(componentId));

						BoltInfoAggrecate overAllboltStats = boltStateAggreactes
								.get(componentId);
						overAllboltStats.addBoltStats(
								getStatValueFromMap(execStats.get_transferred(),":all-time"),
								getStatValueFromMap(execStats.get_emitted(),":all-time"),
								getBoltStatLongValueFromMap(boltStats.get_acked(), ":all-time"),
								getBoltStatLongValueFromMap(boltStats.get_failed(), ":all-time"),
								getBoltStatLongValueFromMap(boltStats.get_executed(), ":all-time"),
								getBoltStatDoubleValueFromMap(boltStats.get_execute_ms_avg(),":all-time"),
								getBoltStatDoubleValueFromMap(boltStats.get_process_ms_avg(),":all-time"));
						
						

//						System.out.println("Bolt Id: " + componentId);
//						System.out.print("Transferred: "
//								+ getStatValueFromMap(
//										execStats.get_transferred(),
//										":all-time") + "\t");
//						System.out.print("Emitted: "
//								+ getStatValueFromMap(execStats.get_emitted(),
//										":all-time") + "\t");
//						System.out.print("Acked: "
//								+ getBoltStatLongValueFromMap(
//										boltStats.get_acked(), ":all-time")
//								+ "\t");
//						System.out.print("Failed: "
//								+ getBoltStatLongValueFromMap(
//										boltStats.get_failed(), ":all-time")
//								+ "\t");
//						System.out.print("Executed : "
//								+ getBoltStatLongValueFromMap(
//										boltStats.get_executed(), ":all-time")
//								+ "\t");
//						System.out.print("Execute Latency (ms): "
//								+ getBoltStatDoubleValueFromMap(
//										boltStats.get_execute_ms_avg(),
//										":all-time") + "\t");
//						System.out.println("Process Latency (ms): "
//								+ getBoltStatDoubleValueFromMap(
//										boltStats.get_process_ms_avg(),
//										":all-time") + "\t");
					}
				}
				Iterator<Entry<String, BoltInfoAggrecate>> it2 = boltStateAggreactes
						.entrySet().iterator();
				while (it2.hasNext()) {
					System.out.println(  it2.next().getValue().toString());
				}
				// Topology Configuration
				/*
				 * System.out.println("**** Topology Configuration ****");
				 * String topologyConfigString = client.getTopologyConf(topology
				 * .get_id()); topologyConfigString =
				 * topologyConfigString.substring(1,
				 * topologyConfigString.length() - 1); String[]
				 * topologyConfParameters = topologyConfigString .split(",\"");
				 * 
				 * for (String topologyConfParamValue : topologyConfParameters)
				 * { String[] paramValue = topologyConfParamValue.split(":");
				 * String parameter = paramValue[0].substring(0,
				 * paramValue[0].length() - 1); String parameterValue =
				 * paramValue[1]; if (paramValue[1].startsWith("\"")) {
				 * parameterValue = paramValue[1].substring(1,
				 * paramValue[1].length() - 1); }
				 * topologyConfigurationParamValues.put(parameter,
				 * parameterValue); } Set<String>
				 * topologyConfigurationParameters =
				 * topologyConfigurationParamValues .keySet(); Iterator<String>
				 * topologyParameters = topologyConfigurationParameters
				 * .iterator(); while (topologyParameters.hasNext()) { String
				 * key = topologyParameters.next();
				 * System.out.println("Parameter: " + key + " Value : " +
				 * topologyConfigurationParamValues.get(key)); }
				 */
			}
			System.out.println(" ****  End of Topology Home Page Details ****");

			// Spout Home Page Details
			/*
			 * System.out.println(" **** Spout Home Page Details ****");
			 * topologiesIterator = summary.get_topologies_iterator(); while
			 * (topologiesIterator.hasNext()) { TopologySummary topology =
			 * topologiesIterator.next(); TopologyInfo topology_info =
			 * client.getTopologyInfo(topology .get_id());
			 * Iterator<ExecutorSummary> executorStatusItr = topology_info
			 * .get_executors_iterator(); while (executorStatusItr.hasNext()) {
			 * // get the executor ExecutorSummary executor_summary =
			 * executorStatusItr.next(); ExecutorStats execStats =
			 * executor_summary.get_stats(); ExecutorSpecificStats execSpecStats
			 * = execStats .get_specific(); String componentId =
			 * executor_summary.get_component_id(); // if the executor is a
			 * spout if (execSpecStats.is_set_spout()) {
			 * spoutSpecificStats(topology_info, topology, executor_summary,
			 * componentId); } } }
			 * System.out.println(" **** End of Spout Home Page Details**** ");
			 */
			// Bolt Home Page Details
			/*
			 * System.out.println(" **** Bolt Home Page Details ****");
			 * topologiesIterator = summary.get_topologies_iterator(); while
			 * (topologiesIterator.hasNext()) { TopologySummary topology =
			 * topologiesIterator.next(); TopologyInfo topology_info =
			 * client.getTopologyInfo(topology .get_id());
			 * Iterator<ExecutorSummary> executorStatusItr = topology_info
			 * .get_executors_iterator(); while (executorStatusItr.hasNext()) {
			 * // get the executor ExecutorSummary executor_summary =
			 * executorStatusItr.next(); ExecutorStats execStats =
			 * executor_summary.get_stats(); ExecutorSpecificStats execSpecStats
			 * = execStats .get_specific(); String componentId =
			 * executor_summary.get_component_id(); // if the executor is a bolt
			 * if (execSpecStats.is_set_bolt()) {
			 * boltSpecificStats(topology_info, topology, executor_summary,
			 * componentId); } } }
			 * System.out.println(" **** End of Bolt Home Page Details **** ");
			 */
			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		} 
	}

	/*
	 * Calculate spout specific stats
	 */
	private static void spoutSpecificStats(TopologyInfo topologyInfo,
			TopologySummary topology, ExecutorSummary executorSummary,
			String componentId) {
		ExecutorStats execStats = executorSummary.get_stats();
		ExecutorSpecificStats execSpecStats = execStats.get_specific();
		SpoutStats spoutStats = execSpecStats.get_spout();
		System.out.println("**** Component summary ****");
		System.out.println("Id : " + componentId);
		System.out.println("Topology Name  : " + topology.get_name());
		System.out.println("Executors : " + "1");
		System.out.println("Tasks : " + "1");
		System.out.println("**** Spout stats ****");
		System.out.println("**** Window Size ****  " + "600");
		System.out.println("Transferred: "
				+ getStatValueFromMap(execStats.get_transferred(), "600"));
		System.out.println("Emitted: "
				+ getStatValueFromMap(execStats.get_emitted(), "600"));
		System.out.println("Acked: "
				+ getStatValueFromMap(spoutStats.get_acked(), "600"));
		System.out.println("Failed: "
				+ getStatValueFromMap(spoutStats.get_failed(), "600"));
		System.out.println("**** Window Size ****  " + "10800");
		System.out.println("Transferred : "
				+ getStatValueFromMap(execStats.get_transferred(), "10800"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), "10800"));
		System.out.println("Acked : "
				+ getStatValueFromMap(spoutStats.get_acked(), "10800"));
		System.out.println("Failed : "
				+ getStatValueFromMap(spoutStats.get_failed(), "10800"));
		System.out.println("**** Window Size ****  " + "86400");
		System.out.println("Transferred : "
				+ getStatValueFromMap(execStats.get_transferred(), "86400"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), "86400"));
		System.out.println("Acked : "
				+ getStatValueFromMap(spoutStats.get_acked(), "86400"));
		System.out.println("Failed : "
				+ getStatValueFromMap(spoutStats.get_failed(), "86400"));
		System.out.println("**** Window Size ****  " + "all-time");
		System.out
				.println("Transferred : "
						+ getStatValueFromMap(execStats.get_transferred(),
								":all-time"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), ":all-time"));
		System.out.println("Acked : "
				+ getStatValueFromMap(spoutStats.get_acked(), ":all-time"));
		System.out.println("Failed : "
				+ getStatValueFromMap(spoutStats.get_failed(), ":all-time"));

		System.out.println("**** Output stats (All time) ****");
		System.out.println("Stream : " + "default");
		System.out
				.println("Transferred : "
						+ getStatValueFromMap(execStats.get_transferred(),
								":all-time"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), ":all-time"));
		System.out.println("Acked : "
				+ getStatValueFromMap(spoutStats.get_acked(), ":all-time"));
		System.out.println("Failed : "
				+ getStatValueFromMap(spoutStats.get_failed(), ":all-time"));

		System.out.println("**** Executors (All time) ****");
		System.out.println("Host : " + executorSummary.get_host());
		System.out.println("Port : " + executorSummary.get_port());
		System.out.println("Up-Time : " + executorSummary.get_uptime_secs());
		System.out
				.println("Transferred : "
						+ getStatValueFromMap(execStats.get_transferred(),
								":all-time"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), ":all-time"));
		System.out.println("Acked : "
				+ getStatValueFromMap(spoutStats.get_acked(), ":all-time"));
		System.out.println("Failed : "
				+ getStatValueFromMap(spoutStats.get_failed(), ":all-time"));

		System.out.println("**** Errors ****");
		Map<String, List<ErrorInfo>> errors = topologyInfo.get_errors();
		List<ErrorInfo> spoutErrors = errors.get(componentId);
		for (ErrorInfo errorInfo : spoutErrors) {
			System.out.println("Spout Error : " + errorInfo.get_error());
		}
	}

	/*
	 * Calculate bolt specific stats
	 */
	private static void boltSpecificStats(TopologyInfo topologyInfo,
			TopologySummary topology, ExecutorSummary executorSummary,
			String componentId) {
		ExecutorStats execStats = executorSummary.get_stats();
		ExecutorSpecificStats execSpecStats = execStats.get_specific();
		BoltStats boltStats = execSpecStats.get_bolt();
		System.out.println(":::::::::: Component summary ::::::::::");
		System.out.println("Id : " + componentId);
		System.out.println("Topology Name  : " + topology.get_name());
		System.out.println("Executors : " + "1");
		System.out.println("Tasks : " + "1");
		System.out.println(":::::::::: Bolt stats ::::::::::");
		System.out.println("::::::::::: Window Size :::::::::::  " + "600");
		System.out.println("Transferred : "
				+ getStatValueFromMap(execStats.get_transferred(), "600"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), "600"));
		System.out.println("Acked : "
				+ getBoltStatLongValueFromMap(boltStats.get_acked(), "600"));
		System.out.println("Failed : "
				+ getBoltStatLongValueFromMap(boltStats.get_failed(), "600"));
		System.out.println("Executed : "
				+ getBoltStatLongValueFromMap(boltStats.get_executed(), "600"));
		System.out.println("Execute Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_execute_ms_avg(),
						"600"));
		System.out.println("Process Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_process_ms_avg(),
						"600"));
		System.out.println("::::::::::: Window Size :::::::::::  " + "10800");
		System.out.println("Transferred : "
				+ getStatValueFromMap(execStats.get_transferred(), "10800"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), "10800"));
		System.out.println("Acked : "
				+ getBoltStatLongValueFromMap(boltStats.get_acked(), "10800"));
		System.out.println("Failed : "
				+ getBoltStatLongValueFromMap(boltStats.get_failed(), "10800"));
		System.out
				.println("Executed : "
						+ getBoltStatLongValueFromMap(boltStats.get_executed(),
								"10800"));
		System.out.println("Execute Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_execute_ms_avg(),
						"10800"));
		System.out.println("Process Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_process_ms_avg(),
						"10800"));
		System.out.println("::::::::::: Window Size :::::::::::  " + "86400");
		System.out.println("Transferred : "
				+ getStatValueFromMap(execStats.get_transferred(), "86400"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), "86400"));
		System.out.println("Acked : "
				+ getBoltStatLongValueFromMap(boltStats.get_acked(), "86400"));
		System.out.println("Failed : "
				+ getBoltStatLongValueFromMap(boltStats.get_failed(), "86400"));
		System.out
				.println("Executed : "
						+ getBoltStatLongValueFromMap(boltStats.get_executed(),
								"86400"));
		System.out.println("Execute Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_execute_ms_avg(),
						"86400"));
		System.out.println("Process Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_process_ms_avg(),
						"86400"));
		System.out
				.println("::::::::::: Window Size :::::::::::  " + "all-time");
		System.out
				.println("Transferred : "
						+ getStatValueFromMap(execStats.get_transferred(),
								":all-time"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), ":all-time"));
		System.out.println("Acked : "
				+ getBoltStatLongValueFromMap(boltStats.get_acked(),
						":all-time"));
		System.out.println("Failed : "
				+ getBoltStatLongValueFromMap(boltStats.get_failed(),
						":all-time"));
		System.out.println("Executed : "
				+ getBoltStatLongValueFromMap(boltStats.get_executed(),
						":all-time"));
		System.out.println("Execute Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_execute_ms_avg(),
						":all-time"));
		System.out.println("Process Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_process_ms_avg(),
						":all-time"));

		System.out.println(":::::::::: Output stats (All time) ::::::::::");
		System.out.println("Stream : " + "default");
		System.out
				.println("Transferred : "
						+ getStatValueFromMap(execStats.get_transferred(),
								":all-time"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), ":all-time"));
		System.out.println("Acked : "
				+ getBoltStatLongValueFromMap(boltStats.get_acked(),
						":all-time"));
		System.out.println("Failed : "
				+ getBoltStatLongValueFromMap(boltStats.get_failed(),
						":all-time"));
		System.out.println("Executed : "
				+ getBoltStatLongValueFromMap(boltStats.get_executed(),
						":all-time"));
		System.out.println("Execute Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_execute_ms_avg(),
						":all-time"));
		System.out.println("Process Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_process_ms_avg(),
						":all-time"));

		System.out.println(":::::::::: Executors (All time) ::::::::::");
		System.out.println("Host : " + executorSummary.get_host());
		System.out.println("Port : " + executorSummary.get_port());
		System.out.println("Up-Time : " + executorSummary.get_uptime_secs());
		System.out
				.println("Transferred : "
						+ getStatValueFromMap(execStats.get_transferred(),
								":all-time"));
		System.out.println("Emitted : "
				+ getStatValueFromMap(execStats.get_emitted(), ":all-time"));
		System.out.println("Acked : "
				+ getBoltStatLongValueFromMap(boltStats.get_acked(),
						":all-time"));
		System.out.println("Failed : "
				+ getBoltStatLongValueFromMap(boltStats.get_failed(),
						":all-time"));
		System.out.println("Executed : "
				+ getBoltStatLongValueFromMap(boltStats.get_executed(),
						":all-time"));
		System.out.println("Execute Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_execute_ms_avg(),
						":all-time"));
		System.out.println("Process Latency (ms) : "
				+ getBoltStatDoubleValueFromMap(boltStats.get_process_ms_avg(),
						":all-time"));

		System.out.println(":::::::::: Errors ::::::::::");
		Map<String, List<ErrorInfo>> errors = topologyInfo.get_errors();
		System.out.println(errors.keySet());
		List<ErrorInfo> boltErrors = errors.get(componentId);
		for (ErrorInfo errorInfo : boltErrors) {
			System.out.println("Bolt Error : " + errorInfo.get_error());
		}
	}

	/*
	 * Utility method to parse a Map<>
	 */
	public static Long getStatValueFromMap(Map<String, Map<String, Long>> map,
			String statName) {
		Long statValue = null;
		Map<String, Long> intermediateMap = map.get(statName);
		statValue = intermediateMap.get("default");
		return statValue;
	}
	public static Double getStatValueFromMapDouble(Map<String, Map<String, Double>> map,
			String statName) {
		Double statValue = null;
		Map<String, Double> intermediateMap = map.get(statName);
		statValue = intermediateMap.get("default");
		return statValue;
	}

	/*
	 * Utility method to parse a Map<> as a special case for Bolts
	 */
	public static Double getBoltStatDoubleValueFromMap(
			Map<String, Map<GlobalStreamId, Double>> map, String statName) {
		Double statValue = 0.0;
		Map<GlobalStreamId, Double> intermediateMap = map.get(statName);
		Set<GlobalStreamId> key = intermediateMap.keySet();
		if (key.size() > 0) {
			Iterator<GlobalStreamId> itr = key.iterator();
			statValue = intermediateMap.get(itr.next());
		}
		return statValue;
	}

	/*
	 * Utility method for Bolts
	 */
	public static Long getBoltStatLongValueFromMap(
			Map<String, Map<GlobalStreamId, Long>> map, String statName) {
		Long statValue = null;
		Map<GlobalStreamId, Long> intermediateMap = map.get(statName);
		Set<GlobalStreamId> key = intermediateMap.keySet();
		if (key.size() > 0) {
			Iterator<GlobalStreamId> itr = key.iterator();
			statValue = intermediateMap.get(itr.next());
		}
		return statValue;
	}
}