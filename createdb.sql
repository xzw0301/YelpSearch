CREATE TABLE Business (
    BusinessID VARCHAR2(255) NOT NULL,
    FullAddress VARCHAR2(255),
    Hours VARCHAR2(1000),
    Open CHAR(1) CHECK ( Open IN ('Y', 'N') ), 
    Categories VARCHAR2(255),
    City VARCHAR2(50),
    ReviewCount Number,
    Name VARCHAR2(255),
    Neighborhoods VARCHAR2(1000),
    State VARCHAR2(20),
    Stars Number,
    Attributes VARCHAR2(2000),
    Longitude Number,
    Latitude Number,
    Type VARCHAR2(50),
    PRIMARY KEY(BusinessID)
);

CREATE TABLE Category (
	ID VARCHAR2(50) NOT NULL,
	BusinessID VARCHAR2(255),
	MainCategory VARCHAR2(50),
    SubCategory VARCHAR2(50),
	PRIMARY KEY(ID),
    FOREIGN KEY(BusinessID) REFERENCES Business(BusinessID) ON DELETE CASCADE
);

CREATE INDEX CategoryIndex ON Category (BusinessID, MainCategory, SubCategory);

CREATE TABLE Attribute (
    ID VARCHAR2(50) NOT NULL,
	BusinessID VARCHAR2(255),
    AttributeName VARCHAR2(255),
    PRIMARY KEY(ID),
    FOREIGN KEY(BusinessID) REFERENCES Business(BusinessID) ON DELETE CASCADE
);

CREATE INDEX AttributeIndex ON Attribute (BusinessID, AttributeName);

CREATE TABLE YelpUser (
    UserID VARCHAR2(255) NOT NULL,
    Since DATE,
    Votes VARCHAR2(255),
    VotesNumber Number,
    ReviewCount Number,
    Name VARCHAR2(255),
    Friends VARCHAR2(255),
    FriendsNumber Number,
    Fans Number,
    AverageStars Number,
    Type VARCHAR2(50),
    Compliments VARCHAR2(255),
    Elite VARCHAR2(255),
    PRIMARY KEY(UserID)
);

CREATE INDEX YelpUserIndex ON YelpUser (Since, VotesNumber, FriendsNumber, AverageStars);

CREATE TABLE Review (
    ReviewID VARCHAR2(255) NOT NULL,
    BusinessID VARCHAR2(255),
    UserID VARCHAR2(255),
    Votes VARCHAR2(255),
    VotesNumber Number,
    Stars Number,
    ReviewDate DATE,
    Text VARCHAR2(4000),
    Type VARCHAR2(50),
    PRIMARY KEY(ReviewID),
    FOREIGN KEY(BusinessID) REFERENCES Business(BusinessID) ON DELETE CASCADE,
    FOREIGN KEY(UserID) REFERENCES YelpUser(UserID) ON DELETE CASCADE
);

CREATE INDEX ReviewIndex ON Review (BusinessID, VotesNumber, Stars, ReviewDate);