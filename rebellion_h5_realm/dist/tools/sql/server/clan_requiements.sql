DROP TABLE IF EXISTS `clan_requiements`;
CREATE TABLE `clan_requiements` (
  `clan_id` int(11) NOT NULL,
  `recruting` int(11) NOT NULL,
  `classes` varchar(1024) NOT NULL,
  `question1` varchar(1024) NOT NULL,
  `question2` varchar(1024) NOT NULL,
  `question3` varchar(1024) NOT NULL,
  `question4` varchar(1024) NOT NULL,
  `question5` varchar(1024) NOT NULL,
  `question6` varchar(1024) NOT NULL,
  `question7` varchar(1024) NOT NULL,
  `question8` varchar(1024) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;