# Yelp-Dataset-Challenge-Mongo
A desktop based application built using Java Swing and MongoDB for the Yelp Dataset Challenge. Features:
- SQL and NoSQL (MongoDB) support
- Java Swing
- Location based filters
- Spatial queries

## Install Instructions

1. Import the dataset from Yelp's website (https://www.yelp.com/dataset_challenge/dataset)
2. Download and import the categores.json file (https://www.yelp.com/developers/documentation/v2/all_category_list/categories.json)
3. Add indexes to business, users, reviews
4. Alter business collection structure for 2d indexing:

        db.business.find().forEach(function(doc) {
            doc.loc = new Array();
            doc.loc[0] = doc.longitude;
            doc.loc[1] = doc.latitude;

            delete doc.longitude;
            delete doc.latitude;
            db.business.save(doc);
        });
        db.business.createIndex({ "loc": "2d" });
        db.business.createIndex({ "loc": "2dsphere" });
        
5. Run YelpMongoUI.java as a Java Application in Eclipse

## Screenshot

<p align="center"><img src="/images/UI.png" width="80%" align="middle"></p>