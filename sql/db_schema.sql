-- tables
-- Table: Emails
CREATE TABLE Emails (
    idEmail int NOT NULL AUTO_INCREMENT,
    value varchar(50) NOT NULL,
    type varchar(50) NOT NULL,
    `primary` bool NOT NULL,
    userId int NOT NULL,
    CONSTRAINT Emails_pk PRIMARY KEY (idEmail)
);

-- Table: Groups
CREATE TABLE Groups (
    idGroup int NOT NULL AUTO_INCREMENT,
    displayName varchar(255) NOT NULL,
    externalId varchar(255) NULL,
    created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lastModified timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX Groups_ak_1 (displayName),
    CONSTRAINT Groups_pk PRIMARY KEY (idGroup)
);

-- Table: UserAssoc
CREATE TABLE UserAssoc (
    idAssoc int NOT NULL AUTO_INCREMENT,
    userId int NOT NULL,
    groupId int NOT NULL,
    UNIQUE INDEX UserAssoc_ak_1 (userId,groupId),
    CONSTRAINT UserAssoc_pk PRIMARY KEY (idAssoc)
);

-- Table: Users
CREATE TABLE Users (
    idUser int NOT NULL AUTO_INCREMENT,
    externalId varchar(255) NULL,
    userName varchar(255) NOT NULL,
    givenName varchar(255) NOT NULL,
    familyName varchar(255) NOT NULL,
    created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lastModified timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX Users_ak_1 (userName),
    CONSTRAINT Users_pk PRIMARY KEY (idUser)
);

-- foreign keys
-- Reference: Emails_Users (table: Emails)
ALTER TABLE Emails ADD CONSTRAINT Emails_Users FOREIGN KEY Emails_Users (userId)
    REFERENCES Users (idUser)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- Reference: UserAssoc_Groups (table: UserAssoc)
ALTER TABLE UserAssoc ADD CONSTRAINT UserAssoc_Groups FOREIGN KEY UserAssoc_Groups (groupId)
    REFERENCES Groups (idGroup)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- Reference: UserAssoc_Users (table: UserAssoc)
ALTER TABLE UserAssoc ADD CONSTRAINT UserAssoc_Users FOREIGN KEY UserAssoc_Users (userId)
    REFERENCES Users (idUser)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- End of file.

