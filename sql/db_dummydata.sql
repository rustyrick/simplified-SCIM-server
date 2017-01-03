INSERT IGNORE INTO `Users` 
(`idUser`, `externalId`, `username`,`familyName`, `givenName`)
VALUES
('1', 'soleo', 'soleoshao', 'Shao', 'Xinjiang'),
('2', 'jamessmith', 'james', 'Smith', 'James');

INSERT IGNORE INTO `Emails` 
(`userId`, `value`, `type`, `primary`)
VALUES
('1', 'shaoxinjiang@gmail.com' ,'work'),
('2', 'xinjiang.shao@gmail.com' ,'personal');

INSERT IGNORE INTO `Groups` 
(`idGroup`, `displayName`)
VALUES
('1', 'Developer');

INSERT IGNORE INTO `UserAssoc` 
(`userId`, `groupId`)
VALUES(
    '1', '1'
);

