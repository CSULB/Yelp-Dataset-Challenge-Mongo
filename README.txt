CECS 521 - Database Architecture
Spring 16
Gaurav Bhor

The project is created using Eclpise.

Additional queries needed to setup the database:
1. Alter business collection structure for 2d indexing:

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

2. Add indexes to business, users, reviews.