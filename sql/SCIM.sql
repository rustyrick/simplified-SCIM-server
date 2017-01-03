/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `SCIM`
--

-- --------------------------------------------------------

--
-- Table structure for table `Emails`
--

CREATE TABLE `Emails` (
  `idEmail` int(11) NOT NULL,
  `value` varchar(50) NOT NULL,
  `type` varchar(50) NOT NULL,
  `primary` tinyint(1) NOT NULL,
  `userId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `Emails`
--

INSERT INTO `Emails` (`idEmail`, `value`, `type`, `primary`, `userId`) VALUES
(1, 'shaoxinjiang@gmail.com', 'work', 1, 1),
(8, 'james.smith@gmail.com', 'personal', 0, 2),
(9, 'j.smith@company.com', 'work', 1, 2),
(16, 'francesco@novellis.it', 'personal', 1, 13),
(17, 'francesco.novellis@bioentech.eu', 'work', 0, 13);

-- --------------------------------------------------------

--
-- Table structure for table `Groups`
--

CREATE TABLE `Groups` (
  `idGroup` int(11) NOT NULL,
  `displayName` varchar(255) NOT NULL,
  `externalId` varchar(255) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `Groups`
--

INSERT INTO `Groups` (`idGroup`, `displayName`, `externalId`, `created`, `lastModified`) VALUES
(1, 'Developer', NULL, '2016-12-28 21:16:38', '2016-12-28 21:16:38'),
(2, 'Manager', NULL, '2016-12-28 21:16:38', '2016-12-28 21:16:38'),
(3, 'Directors', NULL, '2016-12-28 21:16:38', '2016-12-28 21:16:38');

-- --------------------------------------------------------

--
-- Table structure for table `UserAssoc`
--

CREATE TABLE `UserAssoc` (
  `idAssoc` int(11) NOT NULL,
  `userId` int(11) NOT NULL,
  `groupId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `UserAssoc`
--

INSERT INTO `UserAssoc` (`idAssoc`, `userId`, `groupId`) VALUES
(1, 1, 1),
(2, 2, 2);

-- --------------------------------------------------------

--
-- Table structure for table `Users`
--

CREATE TABLE `Users` (
  `idUser` int(11) NOT NULL,
  `externalId` varchar(255) DEFAULT NULL,
  `userName` varchar(255) NOT NULL,
  `givenName` varchar(255) NOT NULL,
  `familyName` varchar(255) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `Users`
--

INSERT INTO `Users` (`idUser`, `externalId`, `userName`, `givenName`, `familyName`, `created`, `lastModified`) VALUES
(1, 'soleo', 'soleoshao', 'Xinjiang', 'Shao', '2016-12-28 21:16:37', '2016-12-28 21:16:37'),
(2, 'jamessmith', 'jamessmith', 'James', 'Smith', '2016-12-28 21:16:37', '2017-01-03 01:19:57'),
(9, 'bjensen', 'bjensen', 'Barbara', 'Jensen', '2017-01-02 12:30:28', '2017-01-02 12:30:28'),
(13, 'fnovelyyy2', 'rustyrick', 'Francesco', 'Novellis', '2017-01-03 14:22:32', '2017-01-03 17:24:06');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Emails`
--
ALTER TABLE `Emails`
  ADD PRIMARY KEY (`idEmail`),
  ADD KEY `Emails_Users` (`userId`);

--
-- Indexes for table `Groups`
--
ALTER TABLE `Groups`
  ADD PRIMARY KEY (`idGroup`),
  ADD UNIQUE KEY `Groups_ak_1` (`displayName`);

--
-- Indexes for table `UserAssoc`
--
ALTER TABLE `UserAssoc`
  ADD PRIMARY KEY (`idAssoc`),
  ADD UNIQUE KEY `userId` (`userId`,`groupId`),
  ADD KEY `UserAssoc_Groups` (`groupId`);

--
-- Indexes for table `Users`
--
ALTER TABLE `Users`
  ADD PRIMARY KEY (`idUser`),
  ADD UNIQUE KEY `Users_ak_1` (`userName`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `Emails`
--
ALTER TABLE `Emails`
  MODIFY `idEmail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;
--
-- AUTO_INCREMENT for table `Groups`
--
ALTER TABLE `Groups`
  MODIFY `idGroup` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT for table `UserAssoc`
--
ALTER TABLE `UserAssoc`
  MODIFY `idAssoc` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;
--
-- AUTO_INCREMENT for table `Users`
--
ALTER TABLE `Users`
  MODIFY `idUser` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `Emails`
--
ALTER TABLE `Emails`
  ADD CONSTRAINT `Emails_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUser`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `UserAssoc`
--
ALTER TABLE `UserAssoc`
  ADD CONSTRAINT `UserAssoc_Groups` FOREIGN KEY (`groupId`) REFERENCES `Groups` (`idGroup`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `UserAssoc_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUser`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
