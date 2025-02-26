package edu.purdue.cs.tornado.spouts;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import edu.purdue.cs.tornado.helper.Command;
import edu.purdue.cs.tornado.helper.LatLong;
import edu.purdue.cs.tornado.helper.Point;
import edu.purdue.cs.tornado.helper.QueryType;
import edu.purdue.cs.tornado.helper.RandomGenerator;
import edu.purdue.cs.tornado.helper.Rectangle;
import edu.purdue.cs.tornado.helper.SpatialHelper;
import edu.purdue.cs.tornado.helper.SpatioTextualConstants;
import edu.purdue.cs.tornado.helper.TextHelpers;
import edu.purdue.cs.tornado.helper.TextualPredicate;
import edu.purdue.cs.tornado.messages.JoinQuery;
import edu.purdue.cs.tornado.messages.KNNQuery;
import edu.purdue.cs.tornado.messages.Query;

public class QueriesFileSystemSpout extends FileSpout {
	public QueriesFileSystemSpout(Map spoutConf, Integer initialSleepDuration) {
		super(spoutConf, initialSleepDuration);

	}

	public static String SPATIAL_RANGE = "SPATIAL_RANGE"; //1  [10] 100 500 out of 10000
	public static String KEYWORD_COUNT = "KEYWORD_COUNT"; //1  [5] 10 20  
	public static String TOTAL_QUERY_COUNT = "TOTAL_QUERY_COUNT"; //1000 [100000]  1000000
	public static Double spatialRangeVal = 10.0;
	public static Integer keywordCountVal = 5;
	public static Integer totalQueryCountVal = 100000;
	public static Integer k = 5;
	public static QueryType queryType;
	public static String dataSrc1, dataSrc2;
	public static TextualPredicate textualPredicate1, textualPredicate2, joinTextualPredicate;
	public static Double distance;
	static ArrayList<LatLong> previousLocations;
	static ArrayList<String> previousTextList;
	static LatLong latLong;
	static int prevLocCount = 0;

	public static RandomGenerator r;
	static Integer i = new Integer(0);

	public void ack(Object msgId) {
	}

	public void close() {
	}

	public void fail(Object msgId) {
	}

	@Override
	public void nextTuple() {
			//super.nextTuple();
		if (i >= QueriesFileSystemSpout.totalQueryCountVal) {
			try {
				if (previousLocations != null || br != null) {
					System.out.println("Used previous loc: " + prevLocCount);
					br.close();
					//previousLocations.clear();
					previousLocations = null;
					System.gc();
					System.gc();
					System.runFinalization();
					br = null;
				}
				Thread.sleep(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		String line = "";
		try {

			// Read File Line By Line
			if ((line = br.readLine()) == null) {
				System.out.println("null line 88");
				br.close();
				connectToFS();

			}
			if ("".equals(line)) {
				System.out.println("empty line");
				if ((line = br.readLine()) == null) {
					System.out.println("null line 96");
					br.close();
					connectToFS();

				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;

		}
		if (line == null || line.isEmpty() || "".equals(line)) {
			System.out.println("null line 108");
			return;
		}
		
		//System.out.println(line);

		emitQuery(line, i);

	}

	private void emitQuery(String line, Integer msgId) {
		//System.out.println("Tweet line:"+line);
		Query q = getQuery(line);
		//for (int j = 0; j < 4 && i < this.totalQueryCountVal; j++) {
		//	Query qold = q;
		//	q = new Query(qold);
		if (q == null) {
			System.out.println("null Query");
			return;

		}
		q.setQueryId(i + selfTaskIndex * totalQueryCountVal);
		i = i + 1;
		//		this.collector.emit(new Values(q.getQueryType(), q.getQueryId(), q.getFocalPoint().getX(), q.getFocalPoint().getY(), q.getSpatialRange().getMin().getX(), q.getSpatialRange().getMin().getY(), q.getSpatialRange().getMax().getX(),
		//				q.getSpatialRange().getMax().getY(), q.getK(), TextHelpers.convertArrayListOfStringToText(q.getQueryText()), TextHelpers.convertArrayListOfStringToText(q.getQueryText2()), q.getTimeStamp(), q.getDataSrc(), q.getDataSrc2(),
		//				q.getCommand(), q.getDistance(), q.getTextualPredicate(), q.getTextualPredicate2(), q.getJoinTextualPredicate(),q.getRemoveTime()
		//
		//		));
		this.collector.emit(new Values(q.getQueryType(), q.getQueryId(), q.getSpatialRange().getMin().getX(), q.getSpatialRange().getMin().getY(), q.getSpatialRange().getMax().getX(), q.getSpatialRange().getMax().getY(),
				TextHelpers.convertArrayListOfStringToText(q.getQueryText()), q.getTimeStamp(), q.getDataSrc(), q.getCommand(), q.getTextualPredicate(), q.getRemoveTime(), q.getComplexQueryText()

		));

		//}

	}

	public Query buildQuery(String line, String scId, int keywordCount, String data1, String data2, Double distance, QueryType queryType, Double spatialRangeVal, TextualPredicate textualPredicate1, TextualPredicate textualPredicate2,
			Integer k) {
		if (previousLocations == null || selfTaskId == null || selfTaskIndex == null) {
			previousLocations = new ArrayList<LatLong>();
			r = new RandomGenerator(0);
			selfTaskId = 0;
			selfTaskIndex = 0;
			i = 0;
		}

		keywordCountVal = keywordCount;
		dataSrc1 = data1;
		//dataSrc2 = data2;
		//QueriesFileSystemSpout.distance = distance;
		QueriesFileSystemSpout.queryType = queryType;
		QueriesFileSystemSpout.spatialRangeVal = spatialRangeVal;
		QueriesFileSystemSpout.textualPredicate1 = textualPredicate1;
		//QueriesFileSystemSpout.textualPredicate2 = textualPredicate2;
		Query q = buildQuery(line);
		if (q != null) {
			q.setQueryId(i + selfTaskIndex * totalQueryCountVal);
			i = i + 1;
			q.setSrcId(scId);
		}
		return q;
	}

	public Query buildQueryslow(String line) {
		String[] tweetParts = line.split(",");
		if (tweetParts.length < 5) {
			System.out.println("Improper tweet format <5:" + line);
			return null;
		}

		double lat = 0.0;
		double lon = 0.0;

		try {
			lat = Double.parseDouble(tweetParts[2]);

			lon = Double.parseDouble(tweetParts[3]);
		} catch (Exception e) {
			System.out.println("unable to parse line" + line);
			e.printStackTrace();
			return null;
		}
		if (lat < SpatioTextualConstants.minLat || lat > SpatioTextualConstants.maxLat || lon < SpatioTextualConstants.minLong || lon > SpatioTextualConstants.maxLong || (Double.compare(lat, 0.0) == 0 && Double.compare(lon, 0.0) == 0)) {

			if (previousLocations.size() > 0) {
				latLong = previousLocations.get(r.nextInt(previousLocations.size()));
				prevLocCount++;
			} else {
				System.out.println("out of bounds of entire space lat lon:" + lat + "," + lon + "," + line);
				return null;
			}
		} else {
			latLong = new LatLong(lat, lon);
			previousLocations.add(latLong);
		}

		//		latLong.setLatitude(lat);
		//		latLong.setLongitude(lon);
		String textContent = "";
		int i = 5;
		while (i < tweetParts.length)
			textContent = textContent + tweetParts[i++] + " ";
		ArrayList<String> queryText1 = new ArrayList<String>();
		ArrayList<String> textList = TextHelpers.transformIntoSortedArrayListOfString(textContent);
		if (textList.size() == 0) {
			if (previousTextList == null)
				return null;
			textList = previousTextList;
		} else {
			previousTextList = textList;
		}
		for (int j = 0; j < keywordCountVal; j++) {

			queryText1.add(textList.get(r.nextInt(textList.size())));
			//queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
		}
		Integer id = i + selfTaskIndex * totalQueryCountVal;
		Point xy = SpatialHelper.convertFromLatLonToXYPoint(latLong);
		Date date = new Date();
		ArrayList<String> queryText2 = new ArrayList<String>();
		Query q = new Query();
		q.setQueryId(id);
		q.setCommand(Command.addCommand);
		q.setDataSrc(dataSrc1);
		q.setQueryType(queryType);
		q.setTimeStamp(date.getTime());
		if (queryType.equals(QueryType.queryTextualRange)) {
			q.setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
			q.setTextualPredicate(textualPredicate1);
			q.setQueryText(queryText1);
		} else if (queryType.equals(QueryType.queryTextualKNN)) {
			((KNNQuery) q).setFocalPoint(xy);
			((KNNQuery) q).setTextualPredicate(textualPredicate1);
			((KNNQuery) q).setQueryText(queryText1);
			((KNNQuery) q).setK(k);
		} else if (queryType.equals(QueryType.queryTextualSpatialJoin)) {
			((JoinQuery) q).setDataSrc2(dataSrc2);
			((JoinQuery) q).setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
			((JoinQuery) q).setTextualPredicate(textualPredicate1);
			((JoinQuery) q).setTextualPredicate2(textualPredicate2);
			((JoinQuery) q).setQueryText(queryText1);
			((JoinQuery) q).setQueryText(queryText2);
			((JoinQuery) q).setDistance(distance);
		}
		return q;

	}

	public Query buildQuery(String line) {
		try {
			int from = 0, to = 0;
			to = line.indexOf(',');
			if (from == -1 || to == -1)
				return null;
			String idstring = line.substring(from, to);
			from = to;
			to = line.indexOf(',', from + 1);
			if (to == -1)
				return null;
			String dateString = line.substring(from + 1, to);
			from = to;
			to = line.indexOf(',', from + 1);

			double lat = 0.0;
			double lon = 0.0;

			if (from == -1 || to == -1)
				return null;
			lat = Double.parseDouble(line.substring(from + 1, to));
			from = to;
			to = line.indexOf(',', from + 1);
			if (to == -1)
				return null;
			lon = Double.parseDouble(line.substring(from + 1, to));

			if (lat < SpatioTextualConstants.minLat || lat > SpatioTextualConstants.maxLat || lon < SpatioTextualConstants.minLong || lon > SpatioTextualConstants.maxLong
					|| (Double.compare(lat, 0.0) == 0 && Double.compare(lon, 0.0) == 0)) {

				if (previousLocations.size() > 0) {
					latLong = previousLocations.get(r.nextInt(previousLocations.size()));
					prevLocCount++;
				} else {
					System.out.println("out of bounds of entire space lat lon:" + lat + "," + lon + "," + line);
					return null;
				}
			} else {
				latLong = new LatLong(lat, lon);
				if(previousLocations != null) {
					previousLocations.add(latLong);
				}

			}
			from = to;
			to = line.indexOf(',', from + 1);
			from = to;
			to = line.indexOf(',', from + 1);

			String textContent = line.substring(from + 1);
			ArrayList<String> queryText1 = new ArrayList<String>();
			ArrayList<String> textList = TextHelpers.transformIntoSortedArrayListOfString(textContent);
			int x;
			if (textList.size() < keywordCountVal) {
				//return null;
				if (previousTextList == null)
					return null;
				textList.addAll(previousTextList);
			} else {
				previousTextList = textList;
			}

			for (int j = 0; j < keywordCountVal; j++) {

				queryText1.add(textList.get(r.nextInt(textList.size())));
				//queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
			}
			Integer id = i + selfTaskIndex * totalQueryCountVal;
			Point xy = SpatialHelper.convertFromLatLonToXYPoint(latLong);
			Date date = new Date();
			ArrayList<String> queryText2 = new ArrayList<String>();
			Query q = new Query();
			q.setQueryId(id);
			q.setCommand(Command.addCommand);
			//	q.setContinousQuery(true);
			q.setDataSrc(dataSrc1);
			//	q.setDistance(distance);
			q.setQueryType(queryType);
			q.setTimeStamp(date.getTime());
			q.setRemoveTime(Long.MAX_VALUE);
			if (queryType.equals(QueryType.queryTextualRange)) {
				q.setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
				q.setTextualPredicate(textualPredicate1);
				if (TextualPredicate.BOOLEAN_EXPR.equals(textualPredicate1)) {
					if (keywordCountVal == 1) {
						q.setTextualPredicate(TextualPredicate.OVERlAPS);
						q.setQueryText(queryText1);
					} else if (keywordCountVal == 2) {
						q.setTextualPredicate(TextualPredicate.CONTAINS);
						q.setQueryText(queryText1);
					} else {
						ArrayList<ArrayList<String>> complexKeywords = new ArrayList<ArrayList<String>>();
						int howManySplitsSecontions = r.nextInt(Math.max(keywordCountVal / 2, 2));
						int start = 0;
						int batchSize = keywordCountVal / (howManySplitsSecontions + 1);
						int j = 0;
						for (; j < howManySplitsSecontions; j++) {
							complexKeywords.add(new ArrayList<String>());
							for (int k = 0; k < batchSize; k++) {
								complexKeywords.get(j).add(queryText1.get(start++));
							}
							q.setQueryText(queryText1);
						}
						//add the remaing keywords in the last batch;
						while (complexKeywords.size() <= howManySplitsSecontions) {
							complexKeywords.add(new ArrayList<String>());
						}
						while (start < keywordCountVal)
							complexKeywords.get(j).add(queryText1.get(start++));
						q.setComplexQueryText(complexKeywords);
						q.setQueryText(queryText1);
					}

				} else {
					q.setQueryText(queryText1);
				}
			} else if (queryType.equals(QueryType.queryTextualKNN)) {
				((KNNQuery) q).setFocalPoint(xy);
				((KNNQuery) q).setTextualPredicate(textualPredicate1);
				((KNNQuery) q).setQueryText(queryText1);
				((KNNQuery) q).setK(k);
			} else if (queryType.equals(QueryType.queryTextualSpatialJoin)) {
				((JoinQuery) q).setDataSrc2(dataSrc2);
				((JoinQuery) q).setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
				((JoinQuery) q).setTextualPredicate(textualPredicate1);
				((JoinQuery) q).setTextualPredicate2(textualPredicate2);
				((JoinQuery) q).setQueryText(queryText1);
				((JoinQuery) q).setQueryText(queryText2);
				((JoinQuery) q).setDistance(distance);
			}
			return q;
		} catch (Exception e) {
			System.out.println("unable to parse line" + line);
			e.printStackTrace();
			return null;
		}
	}

	//TODO: Unsure if used.
	private Query buildQueryOld(String line) {
		try {
			int from = 0, to = 0;
			to = line.indexOf(',');

			if (from == -1 || to == -1)
				return null;
			String idstring = line.substring(from, to);
			from = to;
			to = line.indexOf(',', from + 1);
			if (to == -1)
				return null;
			String dateString = line.substring(from + 1, to);
			from = to;
			to = line.indexOf(',', from + 1);

			double lat = 0.0;
			double lon = 0.0;

			if (from == -1 || to == -1)
				return null;
			lat = Double.parseDouble(line.substring(from + 1, to));
			from = to;
			to = line.indexOf(',', from + 1);
			if (to == -1)
				return null;
			lon = Double.parseDouble(line.substring(from + 1, to));

			if (lat < SpatioTextualConstants.minLat || lat > SpatioTextualConstants.maxLat || lon < SpatioTextualConstants.minLong || lon > SpatioTextualConstants.maxLong
					|| (Double.compare(lat, 0.0) == 0 && Double.compare(lon, 0.0) == 0)) {

				if (previousLocations.size() > 0) {
					latLong = previousLocations.get(r.nextInt(previousLocations.size()));
					prevLocCount++;
				} else {
					System.out.println("out of bounds of entire space lat lon:" + lat + "," + lon + "," + line);
					return null;
				}
			} else {
				latLong = new LatLong(lat, lon);
				previousLocations.add(latLong);

			}
			from = to;
			to = line.indexOf(',', from + 1);
			from = to;
			to = line.indexOf(',', from + 1);

			//		latLong.setLatitude(lat);
			//		latLong.setLongitude(lon);
			String textContent = line.substring(from + 1);
			ArrayList<String> queryText1 = new ArrayList<String>();
			ArrayList<String> textList = TextHelpers.transformIntoSortedArrayListOfString(textContent);
			if (textList.size() < keywordCountVal) {
				return null;
				//				if (previousTextList == null)
				//					return null;
				//				textList.addAll(previousTextList);
			} else {
				previousTextList = textList;
			}

			for (int j = 0; j < keywordCountVal; j++) {

				queryText1.add(textList.get(j));
				//queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
			}
			Integer id = i + selfTaskIndex * totalQueryCountVal;
			Point xy = SpatialHelper.convertFromLatLonToXYPoint(latLong);
			Date date = new Date();
			ArrayList<String> queryText2 = new ArrayList<String>();
			Query q = new Query();
			q.setQueryId(id);
			q.setCommand(Command.addCommand);
			//	q.setContinousQuery(true);
			q.setDataSrc(dataSrc1);
			//	q.setDistance(distance);
			q.setQueryType(queryType);
			q.setTimeStamp(date.getTime());
			q.setRemoveTime(Long.MAX_VALUE);
			if (queryType.equals(QueryType.queryTextualRange)) {
				q.setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
				q.setTextualPredicate(textualPredicate1);
				q.setQueryText(queryText1);
			} else if (queryType.equals(QueryType.queryTextualKNN)) {
				((KNNQuery) q).setFocalPoint(xy);
				((KNNQuery) q).setTextualPredicate(textualPredicate1);
				((KNNQuery) q).setQueryText(queryText1);
				((KNNQuery) q).setK(k);
			} else if (queryType.equals(QueryType.queryTextualSpatialJoin)) {
				((JoinQuery) q).setDataSrc2(dataSrc2);
				((JoinQuery) q).setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
				((JoinQuery) q).setTextualPredicate(textualPredicate1);
				((JoinQuery) q).setTextualPredicate2(textualPredicate2);
				((JoinQuery) q).setQueryText(queryText1);
				((JoinQuery) q).setQueryText(queryText2);
				((JoinQuery) q).setDistance(distance);
			}
			return q;
		} catch (Exception e) {
			System.out.println("unable to parse line" + line);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		super.open(conf, context, collector);
		if (spoutConf.containsKey(this.TOTAL_QUERY_COUNT))
			this.totalQueryCountVal = (Integer) spoutConf.get(this.TOTAL_QUERY_COUNT);
		if (spoutConf.containsKey(this.SPATIAL_RANGE))
			this.spatialRangeVal = (Double) spoutConf.get(this.SPATIAL_RANGE);
		if (spoutConf.containsKey(this.KEYWORD_COUNT))
			this.keywordCountVal = (Integer) spoutConf.get(this.KEYWORD_COUNT);

		if (spoutConf.containsKey(SpatioTextualConstants.kField))
			this.k = (Integer) spoutConf.get(SpatioTextualConstants.kField);

		if (spoutConf.containsKey(SpatioTextualConstants.queryTypeField)) {
			this.queryType = ((QueryType) spoutConf.get(SpatioTextualConstants.queryTypeField));

		} else
			this.queryType = QueryType.queryTextualRange;

		if (spoutConf.containsKey(SpatioTextualConstants.dataSrc))
			this.dataSrc1 = (String) spoutConf.get(SpatioTextualConstants.dataSrc);
		else
			this.dataSrc1 = null;
		if (spoutConf.containsKey(SpatioTextualConstants.dataSrc2))
			this.dataSrc2 = (String) spoutConf.get(SpatioTextualConstants.dataSrc2);
		else
			this.dataSrc2 = null;
		if (spoutConf.containsKey(SpatioTextualConstants.textualPredicate))
			this.textualPredicate1 = (TextualPredicate) spoutConf.get(SpatioTextualConstants.textualPredicate);
		else
			this.textualPredicate1 = TextualPredicate.NONE;
		if (spoutConf.containsKey(SpatioTextualConstants.textualPredicate2))
			this.textualPredicate2 = (TextualPredicate) spoutConf.get(SpatioTextualConstants.textualPredicate2);
		else
			this.textualPredicate2 = TextualPredicate.NONE;
		if (spoutConf.containsKey(SpatioTextualConstants.joinTextualPredicate))
			this.joinTextualPredicate = (TextualPredicate) spoutConf.get(SpatioTextualConstants.joinTextualPredicate);
		else
			this.joinTextualPredicate = TextualPredicate.NONE;
		if (spoutConf.containsKey(SpatioTextualConstants.queryDistance))
			this.distance = (Double) spoutConf.get(SpatioTextualConstants.queryDistance);
		else
			this.distance = null;
		r = new RandomGenerator(selfTaskIndex);
		latLong = new LatLong();
		previousLocations = new ArrayList<LatLong>();

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(SpatioTextualConstants.queryTypeField, SpatioTextualConstants.queryIdField, SpatioTextualConstants.queryXMinField, SpatioTextualConstants.queryYMinField, SpatioTextualConstants.queryXMaxField,
				SpatioTextualConstants.queryYMaxField, SpatioTextualConstants.queryTextField, SpatioTextualConstants.queryTimeStampField, SpatioTextualConstants.dataSrc, SpatioTextualConstants.queryCommand,
				SpatioTextualConstants.textualPredicate, SpatioTextualConstants.removeTime, SpatioTextualConstants.queryComplexTextField));
	}
	
	public Query getQuery(String line) {
		return buildQuery(line);
	}

}
