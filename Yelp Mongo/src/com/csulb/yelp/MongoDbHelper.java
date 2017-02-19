package com.csulb.yelp;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class MongoDbHelper {

	private static MongoClient mongoClient;
	private static MongoDatabase db;
	private static final String delimiter = "=>";

	public static boolean open() {
		try {
			mongoClient = new MongoClient("localhost", 27017);
			db = mongoClient.getDatabase("test");
			System.out.println("Connect to database successfully");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void close() {
		try {
			mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getAttributes(Set<String> selectedMainCategories, String cond) {

		BasicDBList mainCategories = new BasicDBList();
		for (String string : selectedMainCategories) {
			mainCategories.add(new Document("categories", string));
		}

		Document matchCriteria = new Document("$" + cond, mainCategories);
		Document match = new Document("$match", matchCriteria);

		Document projectCriteria = new Document("attributes", "$attributes");
		Document project = new Document("$project", projectCriteria);

		AggregateIterable<Document> iterable = db.getCollection("business").aggregate(asList(match, project));

		ArrayList<String> attributes = new ArrayList<>();

		iterable.forEach(new Block<Document>() {

			@Override
			public void apply(final Document document) {
				try {
					JSONObject doc = new JSONObject(document.toJson()).getJSONObject("attributes");
					Iterator<String> keys = doc.keys();
					while (keys.hasNext()) {
						String nextAttributeKey = keys.next();
						Object nextAttributeValue = doc.get(nextAttributeKey);
						if (nextAttributeValue instanceof JSONObject) {
							// Inner JSON
							JSONObject nextAttributeValueJson = (JSONObject) nextAttributeValue;
							Iterator<String> innerKeys = nextAttributeValueJson.keys();
							while (innerKeys.hasNext()) {
								String innerAttributeKey = innerKeys.next();
								Object innerAttributeValue = nextAttributeValueJson.get(innerAttributeKey);
								if (!attributes.contains(nextAttributeKey + delimiter + innerAttributeKey + delimiter + innerAttributeValue)) {
									attributes.add(nextAttributeKey + delimiter + innerAttributeKey + delimiter + innerAttributeValue);
								}
							}
						} else {
							if (!attributes.contains(nextAttributeKey + delimiter + nextAttributeValue)) {
								attributes.add(nextAttributeKey + delimiter + nextAttributeValue);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return attributes;
	}

	public static AggregateIterable<Document> getBusinesses(Set<String> selectedMainCategories, List<String> selectedAttributes, String cond1, String cond2) {

		BasicDBList mainCategories = new BasicDBList();
		for (String string : selectedMainCategories) {
			mainCategories.add(new Document("categories", string));
		}

		BasicDBList attributes = new BasicDBList();
		for (String string : selectedAttributes) {
			String[] split = string.split(delimiter);
			if (split.length == 2) {
				if (split[1].equalsIgnoreCase("true") || split[1].equalsIgnoreCase("false")) {
					attributes.add(new Document("attributes." + split[0], Boolean.valueOf(split[1])));
				} else if (Helper.isInteger(split[1])) {
					attributes.add(new Document("attributes." + split[0], Integer.parseInt(split[1])));
				} else {
					attributes.add(new Document("attributes." + split[0], split[1]));
				}
			} else if (split.length == 3) {
				if (split[2].equalsIgnoreCase("true") || split[2].equalsIgnoreCase("false")) {
					attributes.add(new Document("attributes." + split[0] + "." + split[1], Boolean.valueOf(split[2])));
				} else if (Helper.isInteger(split[2])) {
					attributes.add(new Document("attributes." + split[0] + "." + split[1], Integer.parseInt(split[2])));
				} else {
					attributes.add(new Document("attributes." + split[0] + "." + split[1], split[2]));
				}
			}
		}

		Document matchMainCriteria = new Document("$" + cond1, mainCategories);
		Document matchMain = new Document("$match", matchMainCriteria);

		Document matchAttrCriteria = new Document("$" + cond2, attributes);
		Document matchAttr = new Document("$match", matchAttrCriteria);

		Document sort = new Document("$sort", new Document("name", 1));

		return db.getCollection("business").aggregate(asList(matchMain, matchAttr, sort));
	}

	public static AggregateIterable<Document> getReviews(String business_id) {
		// db.reviews.aggregate([ { $match: { "business_id":
		// "GVg4GHHRblwY1MFlV1ubAw" } }, { $lookup: { from: "users", localField:
		// "user_id", foreignField: "user_id", as: "user_data" } } ]);

		Document matchCriteria = new Document("business_id", business_id);
		Document match = new Document("$match", matchCriteria);

		Document lookupCriteria = new Document();
		lookupCriteria.append("from", "users");
		lookupCriteria.append("localField", "user_id");
		lookupCriteria.append("foreignField", "user_id");
		lookupCriteria.append("as", "user_data");
		Document lookup = new Document("$lookup", lookupCriteria);

		Document sort = new Document("$sort", new Document("stars", -1));

		return db.getCollection("reviews").aggregate(asList(match, lookup, sort));
	}

	public static FindIterable<Document> getLocalBusinesses(Set<String> selectedMainCategories, List<String> selectedAttributes, String cond1, String cond2,
			String longitude, String lattitude, int proximity) {

		Document filterCriteria = new Document();

		if (selectedMainCategories != null) {
			BasicDBList mainCategories = new BasicDBList();
			for (String string : selectedMainCategories) {
				mainCategories.add(string);
			}
			filterCriteria.append("categories", new Document("$in", mainCategories));
		}

		if (selectedAttributes != null) {
			BasicDBList attributes = new BasicDBList();
			for (String string : selectedAttributes) {
				String[] split = string.split(delimiter);
				if (split.length == 2) {
					if (split[1].equalsIgnoreCase("true") || split[1].equalsIgnoreCase("false")) {
						attributes.add(new Document("attributes." + split[0], Boolean.valueOf(split[1])));
					} else if (Helper.isInteger(split[1])) {
						attributes.add(new Document("attributes." + split[0], Integer.parseInt(split[1])));
					} else {
						attributes.add(new Document("attributes." + split[0], split[1]));
					}
				} else if (split.length == 3) {
					if (split[2].equalsIgnoreCase("true") || split[2].equalsIgnoreCase("false")) {
						attributes.add(new Document("attributes." + split[0] + "." + split[1], Boolean.valueOf(split[2])));
					} else if (Helper.isInteger(split[2])) {
						attributes.add(new Document("attributes." + split[0] + "." + split[1], Integer.parseInt(split[2])));
					} else {
						attributes.add(new Document("attributes." + split[0] + "." + split[1], split[2]));
					}
				}
			}
			filterCriteria.append("$" + cond2, attributes);
		}

		/**
		 * db.business.find({ categories: { $in: ["Food", "Restaurants"] }, loc:
		 * { $near: [-115.16488579999999, 36.131443900000001], $maxDistance: 5 /
		 * 3969 } });
		 */

		BasicDBList coordinates = new BasicDBList();
		coordinates.add(Float.parseFloat(longitude));
		coordinates.add(Float.parseFloat(lattitude));

		System.out.println("proximity / 3959" + proximity / 3959);

		Document nearOptions = new Document();
		nearOptions.append("$near", coordinates);
		nearOptions.append("$maxDistance", (double) proximity / 3959);

		filterCriteria.append("loc", nearOptions);

		System.out.println("filterCriteria" + filterCriteria.toJson());

		return db.getCollection("business").find(filterCriteria);

		/**
		 * db.zips.aggregate([{ $geoNear: { near: { coordinates: [-72, 42] },
		 * distanceField: "calculated", $maxDistance: 2 } }]);
		 */

		// BasicDBList coordinateValues = new BasicDBList();
		// coordinateValues.add(Float.parseFloat(longitude));
		// coordinateValues.add(Float.parseFloat(lattitude));
		// {$geoNear:{near:{coordinates:[-72,42]},distanceField:"calculated",$maxDistance:2}}

		// String geoNearValues = "{near:{coordinates:[" + longitude + "," +
		// lattitude + "]}, distanceField:'calculated', maxDistance: "
		// + Helper.milesToRadian(proximity) + ", spherical: true}";

		// System.out.println(geoNearValues);
		// Document near = new Document();
		// near.append("type", "Point");
		// near.append("coordinates", coordinateValues);
		//
		// Document geoNearValues = new Document();
		// geoNearValues.append("near", near);
		// geoNearValues.append("distanceField", "calculated_distance");
		// geoNearValues.append("$maxDistance", 100);

		// Document geoNear = new Document("$geoNear",
		// JSON.parse(geoNearValues));

		// Document sort = new Document("$sort", new Document("name", 1));

		// if (matchMain != null && matchAttr != null) {
		// return db.getCollection("business").aggregate(asList(geoNear,
		// matchMain, matchAttr, sort));
		// } else {
		// return db.getCollection("business").aggregate(asList(geoNear, sort));
		// }
	};

}