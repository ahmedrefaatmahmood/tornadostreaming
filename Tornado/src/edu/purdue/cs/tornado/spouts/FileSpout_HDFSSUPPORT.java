package edu.purdue.cs.tornado.spouts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.storm.Config;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;

public class FileSpout_HDFSSUPPORT extends BaseRichSpout {
	public static final String FILE_PATH = "FILE_PATH";
	public static final String FILE_SYS_TYPE = "FILE_SYS_TYPE";
	public static final String HDFS = "HDFS";
	public static final String LFS = "LFS";
	public static final String CORE_FILE_PATH = "CORE_FILE_PATH";
	public static final String EMIT_SLEEP_DURATION_NANOSEC = "EMIT_SLEEP_DURATION_NANOSEC";
	public static final long serialVersionUID = 1L;
	public Integer initialSleepDuration;
	public SpoutOutputCollector collector;
	 Integer selfTaskId;
	 Integer selfTaskIndex;
	 Boolean reliable;
	 int count;

	//public Map conf;
	public  BufferedReader br;
	public  Configuration hdfsconf;
	public  FileInputStream fstream;
	public  String filePath;
	public  String corePath;
	public Path pt;
	public String fileSystemType;
	public Integer sleepDurationMicroSec;
	public Map spoutConf;

	public FileSpout_HDFSSUPPORT(Map spoutConf,Integer initialSleepDuration) {
		this.spoutConf = spoutConf;
		this.initialSleepDuration = initialSleepDuration;
	}

	@Override
	public void ack(Object msgId) {
	}

	@Override
	public void close() {
	}

	@Override
	public void fail(Object msgId) {
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {

		//this.conf = conf;
		this.collector = collector;
		this.selfTaskId = context.getThisTaskId();
		this.selfTaskIndex = context.getThisTaskIndex();
		if (conf != null)
			this.reliable = ((Long) conf.get(Config.TOPOLOGY_ACKER_EXECUTORS)) > 0;
		else
			this.reliable = false;
		this.filePath = (String) spoutConf.get(FILE_PATH);
		this.fileSystemType = (String) spoutConf.get(FILE_SYS_TYPE);
		this.sleepDurationMicroSec = (Integer) spoutConf.get(EMIT_SLEEP_DURATION_NANOSEC);
		if (fileSystemType.equals(HDFS)) {
			this.corePath = (String) spoutConf.get(CORE_FILE_PATH);
			hdfsconf = new Configuration();
			hdfsconf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
			hdfsconf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
			hdfsconf.addResource(new Path(corePath));
			pt = new Path(filePath);
		}
		count=0;
		connectToFS();
		try {
			Thread.sleep(initialSleepDuration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	public void connectToFS() {
		if (fileSystemType.equals(HDFS)) {
			connectToHDFS();
		} else {
			connectToLFS();

		}
	}

	public void sleep() {
		if (sleepDurationMicroSec != 0) {
			try {
				//TimeUnit.NANOSECONDS.sleep(sleepDurationNanoSec);
				Thread.sleep(0,sleepDurationMicroSec);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public void connectToLFS() {
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(filePath);
			br = new BufferedReader(new InputStreamReader(fstream));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void connectToHDFS() {
		try {
			FileSystem fs = FileSystem.get(hdfsconf);
			br = new BufferedReader(new InputStreamReader(fs.open(pt)));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void nextTuple() {
		if(count>=100000){
			count=0;
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		count++;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

}