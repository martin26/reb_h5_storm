DROP TABLE IF EXISTS `clan_petitions`;
CREATE TABLE `clan_petitions` (
  `sender_id` int(11) NOT NULL,
  `clan_id` int(11) NOT NULL,
  `answer1` varchar(1024) NOT NULL,
  `answer2` varchar(1024) NOT NULL,
  `answer3` varchar(1024) NOT NULL,
  `answer4` varchar(1024) NOT NULL,
  `answer5` varchar(1024) NOT NULL,
  `answer6` varchar(1024) NOT NULL,
  `answer7` varchar(1024) NOT NULL,
  `answer8` varchar(1024) NOT NULL,
  `comment` varchar(1024) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;